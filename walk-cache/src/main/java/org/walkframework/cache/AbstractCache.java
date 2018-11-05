package org.walkframework.cache;

import java.util.concurrent.Callable;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * 
 * @author liuqf5
 */
public abstract class AbstractCache implements ICache {
	
	private CacheManager cacheManager;
	
	private Cache cache;

	public AbstractCache(CacheManager cacheManager, Cache cache) {
		this.cacheManager = cacheManager;
		this.cache = cache;
	}
	
	@Override
	public CacheManager getCacheManager() {
		return this.cacheManager;
	}

	@Override
	public String getName() {
		return this.cache.getName();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(Object key) {
		ValueWrapper vw = get(key);
		if(vw != null){
			return (T) vw.get();
		}
		return null;
	}

	@Override
	public ValueWrapper get(Object key) {
		return this.cache.get(key);
	}
	
	@Override
	public <T> T get(Object key, Class<T> type) {
		return this.cache.get(key, type);
	}
	
	@Override
	public <T> T get(Object key, Callable<T> valueLoader) {
		return this.cache.get(key, valueLoader);
	}

	@Override
	public void put(Object key, Object value) {
		this.cache.put(key, value);
	}

	@Override
	public void evict(Object key) {
		this.cache.evict(key);
	}

	@Override
	public void clear() {
		this.cache.clear();
	}
	
	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		return this.cache.putIfAbsent(key, value);
	}
	
	@Override
	public Object getNativeCache() {
		return this.cache.getNativeCache();
	}

}
