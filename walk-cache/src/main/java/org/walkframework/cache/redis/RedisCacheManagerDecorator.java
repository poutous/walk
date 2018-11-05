package org.walkframework.cache.redis;

import java.util.Collection;

import org.springframework.cache.Cache;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.walkframework.cache.AbstractCacheManagerDecorator;

/**
 * 基于redis实现的缓存管理器
 * 
 * @author shf675
 * 
 */
public class RedisCacheManagerDecorator extends AbstractCacheManagerDecorator {

	private final RedisSerializer<String> stringSerializer = new StringRedisSerializer();
	
	@Override
	public Cache getCache(String cacheName) {
		Cache concurrentCache = getConcurrentCache(cacheName);
		if(concurrentCache != null){
			return concurrentCache;
		}
		Cache cache = new RedisCacheDecorator(getCacheManager(), getCacheManager().getCache(cacheName), stringSerializer);
		setConcurrentCache(cacheName, cache);
		return cache;
	}
	
	@Override
	public Collection<String> getCacheNames() {
		return getCacheManager().getCacheNames();
	}
}
