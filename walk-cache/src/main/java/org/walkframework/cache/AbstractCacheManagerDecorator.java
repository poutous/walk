package org.walkframework.cache;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.AbstractCacheManager;

/**
 * @author shf675
 *
 */
public abstract class AbstractCacheManagerDecorator implements ICacheManager {
	
	private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>(16);

	private CacheManager cacheManager;

	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	/**
	 * 根据缓存名获取一个缓存
	 * 
	 * @param cacheName
	 * @return
	 */
	@Override
	public ICache getICache(String cacheName) {
		return (ICache)getCache(cacheName);
	}

	@Override
	public Collection<String> getCacheNames() {
		return this.cacheManager.getCacheNames();
	}
	
	@Override
	public Collection<String> getCacheNames(boolean reload) {
		CacheManager cacheManager = getCacheManager();
		if(cacheManager instanceof AbstractCacheManager){
			//重新加载
			if(reload){
				((AbstractCacheManager)cacheManager).initializeCaches();
			}
			return getCacheNames();
		}
		return getCacheNames();
	}
	
	protected void setConcurrentCache(String cacheName, Cache cache){
		cacheMap.put(cacheName, cache);
	}
	
	protected Cache getConcurrentCache(String cacheName){
		return cacheMap.get(cacheName);
	}
	
}
