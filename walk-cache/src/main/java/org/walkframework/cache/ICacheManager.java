package org.walkframework.cache;

import java.util.Collection;

import org.springframework.cache.CacheManager;

/**
 * 缓存管理器
 * 
 * @author shf675
 */
public interface ICacheManager extends CacheManager {
	
	/**
	 * 根据缓存名获取一个缓存
	 * 
	 * @param cacheName
	 * @return
	 */
	ICache getICache(String cacheName);
	
	/**
	 * 获取缓存名称集合
	 * 
	 * @param reload：每次调用本方法时是否都重新获取
	 * @return
	 */
	Collection<String> getCacheNames(boolean reload);
}
