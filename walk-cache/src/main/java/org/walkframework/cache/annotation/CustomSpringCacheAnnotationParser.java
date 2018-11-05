package org.walkframework.cache.annotation;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.cache.annotation.CacheAnnotationParser;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.interceptor.CacheEvictOperation;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CachePutOperation;
import org.springframework.cache.interceptor.CacheableOperation;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 重写org.springframework.cache.annotation.SpringCacheAnnotationParser
 * 
 * 支持在类或方法上仅仅标注@ICacheable、@ICacheEvict、@ICachePut而不写value等值，默认缓存名以DEFAULT_CACHE_NAME+类名
 * 
 * @author shf675
 * 
 */
@SuppressWarnings("serial")
public class CustomSpringCacheAnnotationParser implements CacheAnnotationParser, Serializable {
	
	@Override
	public Collection<CacheOperation> parseCacheAnnotations(Class<?> type) {
		DefaultCacheConfig defaultConfig = getDefaultCacheConfig(type);
		return parseCacheAnnotations(defaultConfig, type);
	}

	@Override
	public Collection<CacheOperation> parseCacheAnnotations(Method method) {
		DefaultCacheConfig defaultConfig = getDefaultCacheConfig(method.getDeclaringClass());
		return parseCacheAnnotations(defaultConfig, method);
	}

	protected Collection<CacheOperation> parseCacheAnnotations(DefaultCacheConfig cachingConfig, AnnotatedElement ae) {
		Collection<CacheOperation> ops = null;

		Collection<ICacheable> cacheables = getAnnotations(ae, ICacheable.class);
		if (cacheables != null) {
			ops = lazyInit(ops);
			for (ICacheable cacheable : cacheables) {
				ops.add(parseCacheableAnnotation(ae, cachingConfig, cacheable));
			}
		}
		Collection<ICacheEvict> evicts = getAnnotations(ae, ICacheEvict.class);
		if (evicts != null) {
			ops = lazyInit(ops);
			for (ICacheEvict evict : evicts) {
				ops.add(parseEvictAnnotation(ae, cachingConfig, evict));
			}
		}
		Collection<ICachePut> puts = getAnnotations(ae, ICachePut.class);
		if (puts != null) {
			ops = lazyInit(ops);
			for (ICachePut put : puts) {
				ops.add(parsePutAnnotation(ae, cachingConfig, put));
			}
		}
		Collection<ICaching> cachings = getAnnotations(ae, ICaching.class);
		if (cachings != null) {
			ops = lazyInit(ops);
			for (ICaching caching : cachings) {
				ops.addAll(parseCachingAnnotation(ae, cachingConfig, caching));
			}
		}

		return ops;
	}

	private <T extends Annotation> Collection<CacheOperation> lazyInit(Collection<CacheOperation> ops) {
		return (ops != null ? ops : new ArrayList<CacheOperation>(1));
	}

	CacheableOperation parseCacheableAnnotation(AnnotatedElement ae, DefaultCacheConfig defaultConfig, ICacheable cacheable) {
		CustomCacheableOperation.Builder builder = new CustomCacheableOperation.Builder();
		builder.setCacheNames(getDefaultCacheName(ae, cacheable.cacheNames()));// 修改
		builder.setCondition(cacheable.condition());
		builder.setUnless(cacheable.unless());
		builder.setKey(cacheable.key());
		builder.setKeyGenerator(cacheable.keyGenerator());
		builder.setCacheManager(cacheable.cacheManager());
		builder.setCacheResolver(cacheable.cacheResolver());
		builder.setName(ae.toString());
		builder.setCacheSeconds(cacheable.cacheSeconds());
		defaultConfig.applyDefault(builder);
		CacheableOperation op = builder.build();
		validateCacheOperation(ae, op);

		return op;
	}

	CacheEvictOperation parseEvictAnnotation(AnnotatedElement ae, DefaultCacheConfig defaultConfig, ICacheEvict cacheEvict) {
		CacheEvictOperation.Builder builder = new CacheEvictOperation.Builder();

		builder.setCacheNames(getDefaultCacheName(ae, cacheEvict.cacheNames()));
		builder.setCondition(cacheEvict.condition());
		builder.setKey(cacheEvict.key());
		builder.setKeyGenerator(cacheEvict.keyGenerator());
		builder.setCacheManager(cacheEvict.cacheManager());
		builder.setCacheResolver(cacheEvict.cacheResolver());
		builder.setCacheWide(cacheEvict.allEntries());
		builder.setBeforeInvocation(cacheEvict.beforeInvocation());
		builder.setName(ae.toString());

		defaultConfig.applyDefault(builder);
		CacheEvictOperation op = builder.build();
		validateCacheOperation(ae, op);

		return op;
	}

	CacheOperation parsePutAnnotation(AnnotatedElement ae, DefaultCacheConfig defaultConfig, ICachePut cachePut) {
		CustomCachePutOperation.Builder builder = new CustomCachePutOperation.Builder();

		builder.setCacheNames(getDefaultCacheName(ae, cachePut.cacheNames()));// 修改
		builder.setCondition(cachePut.condition());
		builder.setUnless(cachePut.unless());
		builder.setKey(cachePut.key());
		builder.setKeyGenerator(cachePut.keyGenerator());
		builder.setCacheManager(cachePut.cacheManager());
		builder.setCacheResolver(cachePut.cacheResolver());
		builder.setName(ae.toString());
		builder.setCacheSeconds(cachePut.cacheSeconds());
		defaultConfig.applyDefault(builder);
		CachePutOperation op = builder.build();
		validateCacheOperation(ae, op);

		return op;
	}

	Collection<CacheOperation> parseCachingAnnotation(AnnotatedElement ae, DefaultCacheConfig defaultConfig, ICaching caching) {
		Collection<CacheOperation> ops = null;

		ICacheable[] cacheables = caching.cacheable();
		if (!ObjectUtils.isEmpty(cacheables)) {
			ops = lazyInit(ops);
			for (ICacheable cacheable : cacheables) {
				ops.add(parseCacheableAnnotation(ae, defaultConfig, cacheable));
			}
		}
		ICacheEvict[] cacheEvicts = caching.evict();
		if (!ObjectUtils.isEmpty(cacheEvicts)) {
			ops = lazyInit(ops);
			for (ICacheEvict cacheEvict : cacheEvicts) {
				ops.add(parseEvictAnnotation(ae, defaultConfig, cacheEvict));
			}
		}
		ICachePut[] cachePuts = caching.put();
		if (!ObjectUtils.isEmpty(cachePuts)) {
			ops = lazyInit(ops);
			for (ICachePut cachePut : cachePuts) {
				ops.add(parsePutAnnotation(ae, defaultConfig, cachePut));
			}
		}

		return ops;
	}

	/**
	 * Provides the {@link DefaultCacheConfig} instance for the specified
	 * {@link Class}.
	 * 
	 * @param target
	 *            the class-level to handle
	 * @return the default config (never {@code null})
	 */
	DefaultCacheConfig getDefaultCacheConfig(Class<?> target) {
		CacheConfig annotation = AnnotationUtils.getAnnotation(target, CacheConfig.class);
		if (annotation != null) {
			return new DefaultCacheConfig(annotation.cacheNames(), annotation.keyGenerator(), annotation.cacheManager(), annotation.cacheResolver());
		}
		return new DefaultCacheConfig();
	}

	private <A extends Annotation> Collection<A> getAnnotations(AnnotatedElement ae, Class<A> annotationType) {
		Collection<A> anns = new ArrayList<A>(2);

		// look at raw annotation
		A ann = ae.getAnnotation(annotationType);
		if (ann != null) {
			anns.add(AnnotationUtils.synthesizeAnnotation(ann, ae));
		}

		// scan meta-annotations
		for (Annotation metaAnn : ae.getAnnotations()) {
			ann = metaAnn.annotationType().getAnnotation(annotationType);
			if (ann != null) {
				anns.add(AnnotationUtils.synthesizeAnnotation(ann, ae));
			}
		}

		return (anns.isEmpty() ? null : anns);
	}

	/**
	 * Validates the specified {@link CacheOperation}.
	 * <p>
	 * Throws an {@link IllegalStateException} if the state of the operation is
	 * invalid. As there might be multiple sources for default values, this
	 * ensure that the operation is in a proper state before being returned.
	 * 
	 * @param ae
	 *            the annotated element of the cache operation
	 * @param operation
	 *            the {@link CacheOperation} to validate
	 */
	private void validateCacheOperation(AnnotatedElement ae, CacheOperation operation) {
		if (StringUtils.hasText(operation.getKey()) && StringUtils.hasText(operation.getKeyGenerator())) {
			throw new IllegalStateException("Invalid cache annotation configuration on '" + ae.toString() + "'. Both 'key' and 'keyGenerator' attributes have been set. " + "These attributes are mutually exclusive: either set the SpEL expression used to" + "compute the key at runtime or set the name of the KeyGenerator bean to use.");
		}
		if (StringUtils.hasText(operation.getCacheManager()) && StringUtils.hasText(operation.getCacheResolver())) {
			throw new IllegalStateException("Invalid cache annotation configuration on '" + ae.toString() + "'. Both 'cacheManager' and 'cacheResolver' attributes have been set. " + "These attributes are mutually exclusive: the cache manager is used to configure a" + "default cache resolver if none is set. If a cache resolver is set, the cache manager" + "won't be used.");
		}
	}

	@Override
	public boolean equals(Object other) {
		return (this == other || other instanceof CustomSpringCacheAnnotationParser);
	}

	@Override
	public int hashCode() {
		return CustomSpringCacheAnnotationParser.class.hashCode();
	}

	/**
	 * 获取默认的缓存名称
	 * 
	 * @param ae
	 * @return
	 */
	protected String[] getDefaultCacheName(AnnotatedElement ae, String[] cacheNames) {
		if (cacheNames != null && cacheNames.length > 0) {
			for (int i = 0; i < cacheNames.length; i++) {
				cacheNames[i] = CacheAnnotationConstant.SPRING_CACHE_ANNOTATION_SET_NAME_PREFIX + cacheNames[i];
			}
			return cacheNames;
		}
		if (ae.getClass().equals(Class.class)) {
			Class<?> type = (Class<?>) ae;
			cacheNames = new String[] { CacheAnnotationConstant.SPRING_CACHE_ANNOTATION_DEFAULT_NAME_PREFIX + type.getName() };
		} else if (ae.getClass().equals(Method.class)) {
			Method method = (Method) ae;
			cacheNames = new String[] { CacheAnnotationConstant.SPRING_CACHE_ANNOTATION_DEFAULT_NAME_PREFIX + method.getDeclaringClass().getName() };
		}
		return cacheNames;
	}

	/**
	 * Provides default settings for a given set of cache operations.
	 */
	static class DefaultCacheConfig {

		private final String[] cacheNames;

		private final String keyGenerator;

		private final String cacheManager;

		private final String cacheResolver;

		public DefaultCacheConfig() {
			this(null, null, null, null);
		}

		private DefaultCacheConfig(String[] cacheNames, String keyGenerator, String cacheManager, String cacheResolver) {
			this.cacheNames = cacheNames;
			this.keyGenerator = keyGenerator;
			this.cacheManager = cacheManager;
			this.cacheResolver = cacheResolver;
		}

		/**
		 * Apply the defaults to the specified {@link CacheOperation.Builder}.
		 * 
		 * @param builder
		 *            the operation builder to update
		 */
		public void applyDefault(CacheOperation.Builder builder) {
			if (builder.getCacheNames().isEmpty() && this.cacheNames != null) {
				builder.setCacheNames(this.cacheNames);
			}
			if (!StringUtils.hasText(builder.getKey()) && !StringUtils.hasText(builder.getKeyGenerator()) && StringUtils.hasText(this.keyGenerator)) {
				builder.setKeyGenerator(this.keyGenerator);
			}

			if (StringUtils.hasText(builder.getCacheManager()) || StringUtils.hasText(builder.getCacheResolver())) {
				// One of these is set so we should not inherit anything
			} else if (StringUtils.hasText(this.cacheResolver)) {
				builder.setCacheResolver(this.cacheResolver);
			} else if (StringUtils.hasText(this.cacheManager)) {
				builder.setCacheManager(this.cacheManager);
			}
		}

	}
}
