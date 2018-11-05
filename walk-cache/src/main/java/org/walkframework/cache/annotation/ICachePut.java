package org.walkframework.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * 替代org.springframework.cache.annotation.CachePut，加入缓存时间cacheSeconds
 * 
 * @author shf675
 */
@Target( { ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ICachePut {

	/**
	 * Alias for {@link #cacheNames}.
	 */
	@AliasFor("cacheNames")
	String[] value() default {};

	/**
	 * Names of the caches to use for the cache put operation.
	 * <p>
	 * Names may be used to determine the target cache (or caches), matching the
	 * qualifier value or bean name of a specific bean definition.
	 * 
	 * @since 4.2
	 * @see #value
	 * @see CacheConfig#cacheNames
	 */
	@AliasFor("value")
	String[] cacheNames() default {};

	/**
	 * Spring Expression Language (SpEL) expression for computing the key
	 * dynamically.
	 * <p>
	 * Default is {@code ""}, meaning all method parameters are considered as a
	 * key, unless a custom {@link #keyGenerator} has been set.
	 */
	String key() default "";

	/**
	 * The bean name of the custom
	 * {@link org.springframework.cache.interceptor.KeyGenerator} to use.
	 * <p>
	 * Mutually exclusive with the {@link #key} attribute.
	 * 
	 * @see CacheConfig#keyGenerator
	 */
	String keyGenerator() default "";

	/**
	 * The bean name of the custom
	 * {@link org.springframework.cache.CacheManager} to use to create a default
	 * {@link org.springframework.cache.interceptor.CacheResolver} if none is
	 * set already.
	 * <p>
	 * Mutually exclusive with the {@link #cacheResolver} attribute.
	 * 
	 * @see org.springframework.cache.interceptor.SimpleCacheResolver
	 * @see CacheConfig#cacheManager
	 */
	String cacheManager() default "";

	/**
	 * The bean name of the custom
	 * {@link org.springframework.cache.interceptor.CacheResolver} to use.
	 * 
	 * @see CacheConfig#cacheResolver
	 */
	String cacheResolver() default "";

	/**
	 * Spring Expression Language (SpEL) expression used for making the cache
	 * put operation conditional.
	 * <p>
	 * Default is {@code ""}, meaning the method result is always cached.
	 */
	String condition() default "";

	/**
	 * Spring Expression Language (SpEL) expression used to veto the cache put
	 * operation.
	 * <p>
	 * Unlike {@link #condition}, this expression is evaluated after the method
	 * has been called and can therefore refer to the {@code result}.
	 * <p>
	 * Default is {@code ""}, meaning that caching is never vetoed.
	 * 
	 * @since 3.2
	 */
	String unless() default "";

	/**
	 * 缓存时间。单位：秒
	 * 
	 * 使用String类型，方便使用${}表达式获取变量值
	 */
	String cacheSeconds() default CacheAnnotationConstant.noSetCacheSecondsDefaultValue;

}