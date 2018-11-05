package org.walkframework.cache;

import java.util.Iterator;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * 自扩展缓存
 * 
 */
public interface ICache extends Cache {
	
	/**
	 * get方法返回的是个ValueWrapper类型对象，该方法转换成值
	 * 
	 * @param key
	 * @return
	 */
	<T> T getValue(Object key);

	/**
	 * 设置过期时间。-1表示永不过期，0表示立即失效
	 * 
	 * @param key：键值
	 * @param expire:
	 *            存活时间。单位秒
	 */
	void expire(Object key, long expireSeconds);

	/**
	 * 返回key的剩余存活时间，单位毫秒
	 */
	Long ttl(Object key);

	/**
	 * 获取所有key
	 */
	Iterator<Object> keys();

	/**
	 * 获取大小
	 */
	Long size();

	/**
	 * 获取缓存管理器
	 * 
	 * @return
	 */
	CacheManager getCacheManager();

}
