package org.walkframework.cache.ehcache;

import net.sf.ehcache.Ehcache;

import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.support.AbstractCacheManager;
import org.walkframework.cache.AbstractCacheManagerDecorator;
import org.walkframework.cache.util.ReflectHelper;

/**
 * 基于ehcache实现的缓存管理器
 * 
 * @author shf675
 * 
 */
public class EhCacheCacheManagerDecorator extends AbstractCacheManagerDecorator {
	
	/**
	 * 是否动态创建缓存
	 */
	private boolean dynamic = true;

	public boolean isDynamic() {
		return dynamic;
	}

	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

	@Override
	public Cache getCache(String cacheName) {
		Cache concurrentCache = getConcurrentCache(cacheName);
		if(concurrentCache != null){
			return concurrentCache;
		}
		Cache cache = getCacheManager().getCache(cacheName);
		if (cache == null) {
			net.sf.ehcache.CacheManager nativeCacheManager = ((EhCacheCacheManager) getCacheManager()).getCacheManager();
			Ehcache ehcache = nativeCacheManager.getEhcache(cacheName);
			if (ehcache == null && isDynamic()) {
				// 如果不存在，则动态创建
				ehcache = nativeCacheManager.addCacheIfAbsent(cacheName);
			}

			if (ehcache != null) {
				ReflectHelper.invokeMethod(getCacheManager(), AbstractCacheManager.class, "addCache", new Object[] { new EhCacheCache(ehcache) }, new Class[] { Cache.class });
				cache = getCacheManager().getCache(cacheName);
			}
		}	
		cache = new EhCacheDecorator(getCacheManager(), cache);
		setConcurrentCache(cacheName, cache);
		return cache;
	}
}
