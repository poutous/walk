package org.walkframework.redis.lock;

import org.springframework.data.redis.connection.RedisConnection;


/**
 * 锁回调
 * 
 * @author shf675
 *
 * @param <T>
 */
public interface LockCallback<T> {
	T doInLock(RedisConnection connection);
}