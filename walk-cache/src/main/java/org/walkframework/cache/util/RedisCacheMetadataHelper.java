package org.walkframework.cache.util;

import org.springframework.data.redis.cache.RedisCache;

/**
 * 
 * 
 * @author shf675
 *
 */
public class RedisCacheMetadataHelper {
	
	private final RedisCache redisCache;
	
	private final Object cacheMetadata;
	
	public RedisCacheMetadataHelper(RedisCache redisCache){
		this.redisCache = redisCache;
		this.cacheMetadata = ReflectHelper.getFieldValue(this.redisCache, "cacheMetadata");
		
	}
	
	/**
	 * @return true if the {@link RedisCache} uses a prefix for key ranges.
	 */
	public boolean usesKeyPrefix() {
		return (Boolean)invokeMethod("usesKeyPrefix");
	}

	/**
	 * Get the binary representation of the key prefix.
	 *
	 * @return never {@literal null}.
	 */
	public byte[] getKeyPrefix() {
		return (byte[])invokeMethod("getKeyPrefix");
	}

	/**
	 * Get the binary representation of the key identifying the data structure used to maintain known keys.
	 *
	 * @return never {@literal null}.
	 */
	public byte[] getSetOfKnownKeysKey() {
		return (byte[])invokeMethod("getSetOfKnownKeysKey");
	}

	/**
	 * Get the binary representation of the key identifying the data structure used to lock the cache.
	 *
	 * @return never {@literal null}.
	 */
	public byte[] getCacheLockKey() {
		return (byte[])invokeMethod("getCacheLockKey");
	}

	/**
	 * Get the name of the cache.
	 *
	 * @return
	 */
	public String getCacheName() {
		return (String)invokeMethod("getCacheName");
	}

	/**
	 * Get the default expiration time in seconds.
	 *
	 * @return
	 */
	public long getDefaultExpiration() {
		return (Long)invokeMethod("getDefaultExpiration");
	}
	
	public Object getCacheMetadata() {
		return cacheMetadata;
	}
	
	private Object invokeMethod(String methodName){
		return ReflectHelper.invokeMethod(this.cacheMetadata, this.cacheMetadata.getClass(), methodName, null, null);
	}
}
