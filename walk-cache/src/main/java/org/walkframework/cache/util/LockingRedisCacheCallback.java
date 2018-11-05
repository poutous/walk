package org.walkframework.cache.util;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;

/**
 * @author shf675
 *
 * @param <T>
 */
public abstract class LockingRedisCacheCallback<T> implements RedisCallback<T>{
	
	//默认超时时间
	private static final int DEFAULT_EXPIRY_TIME_SECONDS = Integer.getInteger("redis.lock.expiry.millis", 60) / 1000;
	
	private final int acquiryTimeoutInSeconds;
	
	private final RedisCacheMetadataHelper metadataHelper;

	public LockingRedisCacheCallback(RedisCacheMetadataHelper metadataHelper) {
		this(metadataHelper, DEFAULT_EXPIRY_TIME_SECONDS);
	}
	
	public LockingRedisCacheCallback(RedisCacheMetadataHelper metadataHelper, int acquiryTimeoutInSeconds) {
		this.metadataHelper = metadataHelper;
		this.acquiryTimeoutInSeconds = acquiryTimeoutInSeconds;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.core.RedisCallback#doInRedis(org.springframework.data.redis.connection.RedisConnection)
	 */
	@Override
	public T doInRedis(RedisConnection connection) throws DataAccessException {

		if (connection.exists(metadataHelper.getCacheLockKey())) {
			return null;
		}
		try {
			connection.set(metadataHelper.getCacheLockKey(), metadataHelper.getCacheLockKey());
			
			//设置默认超时时间，否则会造成死锁
			connection.expire(metadataHelper.getCacheLockKey(), acquiryTimeoutInSeconds);
			return doInLock(connection);
		} finally {
			connection.del(metadataHelper.getCacheLockKey());
		}
	}

	public abstract T doInLock(RedisConnection connection);
}
