package org.walkframework.cache.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.interceptor.CacheAspectSupport;
import org.springframework.cache.interceptor.CacheEvictOperation;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CacheOperationInvoker;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.expression.EvaluationException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;
import org.walkframework.cache.ICache;
import org.walkframework.cache.util.ReflectHelper;

/**
 * 自定义缓存拦截器
 * 
 * @author shf675
 * 
 */
@SuppressWarnings("serial")
public class CustomCacheInterceptor extends CacheInterceptor {

	private boolean initialized = false;
	
	private StringValueResolver embeddedValueResolver;
	
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		super.setBeanFactory(beanFactory);
		embeddedValueResolver = new EmbeddedValueResolver((ConfigurableBeanFactory) beanFactory);
	}
	
	@Override
	public void afterSingletonsInstantiated() {
		super.afterSingletonsInstantiated();
		this.initialized = true;
	}

	@Override
	protected Object execute(CacheOperationInvoker invoker, Object target, Method method, Object[] args) {
		// check whether aspect is enabled
		// to cope with cases where the AJ is pulled in automatically
		if (this.initialized) {
			Class<?> targetClass = getTargetClass(target);
			Collection<CacheOperation> operations = getCacheOperationSource().getCacheOperations(method, targetClass);
			if (!CollectionUtils.isEmpty(operations)) {
				return execute(invoker, new CacheOperationContexts(operations, method, args, target, targetClass));
			}
		}
		return invoker.invoke();
	}

	/**
	 * 支持根据注解设置缓存时间
	 * 
	 * @return
	 */

	protected void doPut(CacheOperation cacheOperation, Cache cache, Object key, Object result) {
		Integer cacheSeconds = getCacheSeconds(cacheOperation);
		//当cacheSeconds设置为0时表示不缓存
		if(cacheSeconds != null && cacheSeconds == 0){
			return ;
		}
		
		//置入缓存
		super.doPut(cache, getKey(key), result);
		
		//缓存时间设置
		if (cache instanceof ICache) {
			//当cacheSeconds未设置时不设置过期时间，将默认使用全局缓存过期时间
			if (cacheSeconds == null || cacheSeconds.intValue() == Integer.parseInt(CacheAnnotationConstant.noSetCacheSecondsDefaultValue)) {
				return;
			}
			((ICache) cache).expire(getKey(key), cacheSeconds.longValue());
		}
	}

	/**
	 * 获取缓存秒数
	 * 
	 * @param cacheOperation
	 * @return
	 */
	protected Integer getCacheSeconds(CacheOperation cacheOperation) {
		String cacheSeconds = null;
		if (cacheOperation instanceof CustomCacheableOperation) {
			cacheSeconds = ((CustomCacheableOperation) cacheOperation).getCacheSeconds();
		} else if (cacheOperation instanceof CustomCachePutOperation) {
			cacheSeconds = ((CustomCachePutOperation) cacheOperation).getCacheSeconds();
		}
		//可能是表达式
		String cacheSecondsValue = StringUtils.trimAllWhitespace(embeddedValueResolver.resolveStringValue(cacheSeconds));
		return StringUtils.hasText(cacheSecondsValue) ? Integer.valueOf(cacheSecondsValue) : null;
	}

	@Override
	protected ValueWrapper doGet(Cache cache, Object key) {
		return super.doGet(cache, getKey(key));
	}

	@Override
	protected void doEvict(Cache cache, Object key) {
		super.doEvict(cache, getKey(key));
	}

	@Override
	protected void doClear(Cache cache) {
		super.doClear(cache);
	}

	protected Object execute(CacheOperationInvoker invoker, CacheOperationContexts contexts) {
		// Process any early evictions
		//processCacheEvicts(contexts.get(CacheEvictOperation.class), true, new Object());
		ReflectHelper.invokeMethod(this, CacheAspectSupport.class, "processCacheEvicts", 
				new Object[] { contexts.get(CacheEvictOperation.class), true, new Object() }, 
				new Class[] { Collection.class, boolean.class, Object.class });

		// Check if we have a cached item matching the conditions
		//Cache.ValueWrapper cacheHit = findCachedItem(contexts.get(CacheableOperation.class));
		Cache.ValueWrapper cacheHit = (Cache.ValueWrapper) ReflectHelper.invokeMethod(this, CacheAspectSupport.class, "findCachedItem", 
				new Object[] { contexts.get(CustomCacheableOperation.class) }, 
				new Class[] { Collection.class });

		// Collect puts from any @Cacheable miss, if no cached item is found
		List<CachePutRequest> cachePutRequests = new LinkedList<CachePutRequest>();
		if (cacheHit == null) {
			collectPutRequests(contexts.get(CustomCacheableOperation.class), new Object(), cachePutRequests);
		}

		Cache.ValueWrapper result = null;

		// If there are no put requests, just use the cache hit
		if (cachePutRequests.isEmpty() && !hasCachePut(contexts)) {
			result = cacheHit;
		}

		// Invoke the method if don't have a cache hit
		if (result == null) {
			result = new SimpleValueWrapper(invokeOperation(invoker));
		}

		// Collect any explicit @CachePuts
		Object obj = result.get();
		collectPutRequests(contexts.get(CustomCachePutOperation.class), result.get(), cachePutRequests);
		// Process any collected put requests, either from @CachePut or a @Cacheable miss
		for (CachePutRequest cachePutRequest : cachePutRequests) {
			cachePutRequest.apply(obj);
		}

		// Process any late evictions
		//processCacheEvicts(contexts.get(CacheEvictOperation.class), false, result.get());
		ReflectHelper.invokeMethod(this, CacheAspectSupport.class, "processCacheEvicts", 
				new Object[] { contexts.get(CacheEvictOperation.class), false, obj }, 
				new Class[] { Collection.class, boolean.class, Object.class });
		return obj;
	}
	
	private void collectPutRequests(Collection<CacheOperationContext> contexts, Object result, Collection<CachePutRequest> putRequests) {
		for (CacheOperationContext context : contexts) {
			Boolean isConditionPassing = (Boolean)ReflectHelper.invokeMethod(this, CacheAspectSupport.class, "isConditionPassing", 
					new Object[] { context, result }, 
					new Class[] { CacheOperationContext.class, Object.class });
			if (isConditionPassing.booleanValue()) {
				//Object key = generateKey(context, result);
				Object key = ReflectHelper.invokeMethod(this, CacheAspectSupport.class, "generateKey", 
						new Object[] { context, result }, 
						new Class[] { CacheOperationContext.class, Object.class });
				putRequests.add(new CachePutRequest(context, key));
			}
		}
	}
	
	private boolean hasCachePut(CacheOperationContexts contexts) {
		// Evaluate the conditions *without* the result object because we don't have it yet.
		Collection<CacheOperationContext> cachePutContexts = contexts.get(CustomCachePutOperation.class);
		Collection<CacheOperationContext> excluded = new ArrayList<CacheOperationContext>();
		for (CacheOperationContext context : cachePutContexts) {
			ICacheOperationContext ctx = (ICacheOperationContext)context;
			try {
				if (!ctx.isConditionPassing(new Object())) {
	                excluded.add(context);
				}
			}
			catch (EvaluationException e) {
				// Ignoring failure due to missing result, consider the cache put has
				// to proceed
			}
		}
		// check if  all puts have been excluded by condition
		return cachePutContexts.size() != excluded.size();


	}
	
	private Object getKey(Object key) {
		return key instanceof CustomSimpleKey ? key.toString() : key;
	}

	private Class<?> getTargetClass(Object target) {
		Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
		if (targetClass == null && target != null) {
			targetClass = target.getClass();
		}
		return targetClass;
	}

	protected class CacheOperationContexts {

		private final MultiValueMap<Class<? extends CacheOperation>, CacheOperationContext> contexts = new LinkedMultiValueMap<Class<? extends CacheOperation>, CacheOperationContext>();

		public CacheOperationContexts(Collection<? extends CacheOperation> operations, Method method, Object[] args, Object target, Class<?> targetClass) {

			for (CacheOperation operation : operations) {
				this.contexts.add(operation.getClass(), getOperationContext(operation, method, args, target, targetClass));
			}
		}

		public Collection<CacheOperationContext> get(Class<? extends CacheOperation> operationClass) {
			Collection<CacheOperationContext> result = this.contexts.get(operationClass);
			return (result != null ? result : Collections.<CacheOperationContext> emptyList());
		}
	}

	protected class CachePutRequest {

		private final CacheOperationContext context;

		private final Object key;

		public CachePutRequest(CacheOperationContext context, Object key) {
			this.context = context;
			this.key = key;
		}

		public void apply(Object result) {
			ICacheOperationContext ctx = (ICacheOperationContext) this.context;
			if (ctx.canPutToCache(result)) {
				for (Cache cache : ctx.getCaches()) {
					doPut(ctx.getOperation(), cache, this.key, result);
				}
			}
		}
	}

	protected CacheOperationContext getOperationContext(CacheOperation operation, Method method, Object[] args, Object target, Class<?> targetClass) {
		CacheOperationMetadata metadata = getCacheOperationMetadata(operation, method, targetClass);
		return new ICacheOperationContext(metadata, args, target);
	}

	protected class ICacheOperationContext extends CacheOperationContext {
		
		public ICacheOperationContext(CacheOperationMetadata metadata, Object[] args, Object target) {
			super(metadata, args, target);
		}

		@Override
		protected Collection<? extends Cache> getCaches() {
			return super.getCaches();
		}

		@Override
		protected boolean canPutToCache(Object value) {
			return super.canPutToCache(value);
		}
		
		@Override
		protected boolean isConditionPassing(Object result) {
			return super.isConditionPassing(result);
		}
	}
}
