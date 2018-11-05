package org.walkframework.cache.annotation;

/**
 * @author shf675
 * 
 */
public interface CacheAnnotationConstant {

	/**
	 * 未设置缓存时间的默认值
	 * 相当于不设置缓存时间，使用缓存管理器默认的过期时间
	 */
	String noSetCacheSecondsDefaultValue = "-675";

	/**
	 * 缓存注解未设置value时固定前缀
	 */
	String SPRING_CACHE_ANNOTATION_DEFAULT_NAME_PREFIX = "spring_cache_annotation_default_name_";

	/**
	 * 缓存注解设置value时固定前缀
	 */
	String SPRING_CACHE_ANNOTATION_SET_NAME_PREFIX = "spring_cache_annotation_set_name_";
}
