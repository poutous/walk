package org.walkframework.batis.cache;

import org.walkframework.cache.ICache;
import org.walkframework.cache.ICacheManager;


/**
 * 缓存管理器
 * 
 * @author shf675
 *
 */
public abstract class CacheManager {

	private static ICacheManager cacheManager;

	public static void setCacheManager(ICacheManager cacheManager) {
		CacheManager.cacheManager = cacheManager;
	}

	public static ICacheManager getCacheManager() {
		return cacheManager;
	}
	
	public static ICache getCache(String name) {
		return cacheManager.getICache(name);
	}
}
