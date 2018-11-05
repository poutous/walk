package org.walkframework.redis.lock;

import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;

/**
 * 基于redis的全局锁简单实现
 * 
 * Redis distributed lock implementation (fork by Bruno Bossola <bbossola@gmail.com>)
 * 
 * @author Alois Belaska <alois.belaska@gmail.com>
 * 
 * @see https://github.com/abelaska/jedis-lock
 */
public class RedisLock {

	//锁名统一加后缀
	private static final String LOCK_SUFFIX = "~redis-lock";
	
	private static final Lock NO_LOCK = new Lock(new UUID(0l, 0l), 0l);

	private static final int ONE_SECOND = 1000;

	private static final int DEFAULT_EXPIRY_TIME_MILLIS = Integer.getInteger("redis.lock.expiry.millis", 60 * ONE_SECOND);
	private static final int DEFAULT_ACQUIRE_TIMEOUT_MILLIS = Integer.getInteger("redis.lock.acquiry.millis", 10 * ONE_SECOND);
	private static final int DEFAULT_ACQUIRY_RESOLUTION_MILLIS = Integer.getInteger("redis.lock.acquiry.resolution.millis", 100);

	private final RedisOperations redisOperations;
	
	private final byte[] lockKeyPath;

	private final int lockExpiryInMillis;
	private final int acquiryTimeoutInMillis;
	private final UUID lockUUID;

	private Lock lock = null;

	/**
	 * Detailed constructor with default acquire timeout 10000 msecs and lock
	 * expiration of 60000 msecs.
	 * 
	 * @param redisOperations
	 * @param lockKey
	 *            lock key (ex. account:1, ...)
	 */
	public RedisLock(RedisOperations redisOperations, String lockKey) {
		this(redisOperations, lockKey, DEFAULT_ACQUIRE_TIMEOUT_MILLIS, DEFAULT_EXPIRY_TIME_MILLIS);
	}

	/**
	 * Detailed constructor with default lock expiration of 60000 msecs.
	 * 
	 * @param redisOperations
	 * @param lockKey
	 *            lock key (ex. account:1, ...)
	 * @param acquireTimeoutMillis
	 *            acquire timeout in miliseconds (default: 10000 msecs)
	 */
	public RedisLock(RedisOperations redisOperations, String lockKey, int acquireTimeoutMillis) {
		this(redisOperations, lockKey, acquireTimeoutMillis, DEFAULT_EXPIRY_TIME_MILLIS);
	}

	/**
	 * Detailed constructor.
	 * 
	 * @param redisOperations
	 * @param lockKey
	 *            lock key (ex. account:1, ...)
	 * @param acquireTimeoutMillis
	 *            acquire timeout in miliseconds (default: 10000 msecs)
	 * @param expiryTimeMillis
	 *            lock expiration in miliseconds (default: 60000 msecs)
	 */
	public RedisLock(RedisOperations redisOperations, String lockKey, int acquireTimeoutMillis, int expiryTimeMillis) {
		this(redisOperations, lockKey, acquireTimeoutMillis, expiryTimeMillis, UUID.randomUUID());
	}

	/**
	 * Detailed constructor.
	 * 
	 * @param redisOperations
	 * @param lockKey
	 *            lock key (ex. account:1, ...)
	 * @param acquireTimeoutMillis
	 *            acquire timeout in miliseconds (default: 10000 msecs)
	 * @param expiryTimeMillis
	 *            lock expiration in miliseconds (default: 60000 msecs)
	 * @param uuid
	 *            unique identification of this lock
	 */
	public RedisLock(RedisOperations redisOperations, String lockKey, int acquireTimeoutMillis, int expiryTimeMillis, UUID uuid) {
		this.redisOperations = redisOperations;
		this.lockKeyPath = redisOperations.getKeySerializer().serialize(lockKey + LOCK_SUFFIX);
		this.acquiryTimeoutInMillis = acquireTimeoutMillis;
		this.lockExpiryInMillis = expiryTimeMillis + 1;
		this.lockUUID = uuid;
	}

	/**
	 * 执行具体要锁的业务逻辑
	 * 
	 * @param action
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	public <T> T execute(final LockCallback<T> action) {
		return (T)this.redisOperations.execute(new RedisCallback<T>() {
			public T doInRedis(RedisConnection connection) throws DataAccessException {
				try {
					//1、获得锁
					if(acquireLock(connection)){
						//2、执行具体的业务逻辑
						return action.doInLock(connection);
					}
					return null;
				} catch (InterruptedException e) {
					throw new AcquireLockException(e);
				} finally {
					//3、释放锁
					releaseLock(connection);
				}
			}
		});
	}

	/**
	 * Acquire lock.
	 * 
	 * @param connection
	 * @return true if lock is acquired, false acquire timeouted
	 * @throws InterruptedException
	 *             in case of thread interruption
	 */
	protected synchronized boolean acquireLock(RedisConnection connection) throws InterruptedException {
		int timeout = acquiryTimeoutInMillis;
		while (timeout >= 0) {
			final Lock newLock = asLock(System.currentTimeMillis() + lockExpiryInMillis);
			byte[] byteNewLock = redisOperations.getValueSerializer().serialize(newLock.toString());
			if (connection.setNX(lockKeyPath, byteNewLock)) {
				this.lock = newLock;
				return true;
			}

			final String currentValueStr = (String)redisOperations.getValueSerializer().deserialize(connection.get(lockKeyPath));
			final Lock currentLock = Lock.fromString(currentValueStr);
			if (currentLock.isExpiredOrMine(lockUUID)) {
				String oldValueStr = (String)redisOperations.getValueSerializer().deserialize(connection.getSet(lockKeyPath, byteNewLock));
				if (oldValueStr != null && oldValueStr.equals(currentValueStr)) {
					this.lock = newLock;
					return true;
				}
			}

			timeout -= DEFAULT_ACQUIRY_RESOLUTION_MILLIS;
			Thread.sleep(DEFAULT_ACQUIRY_RESOLUTION_MILLIS);
		}

		return false;
	}

	/**
	 * Renew lock.
	 * 
	 * @return true if lock is acquired, false otherwise
	 * @throws InterruptedException
	 *             in case of thread interruption
	 */
	protected boolean renewLock(RedisConnection connection) throws InterruptedException {
		final Lock lock = Lock.fromString((String)redisOperations.getValueSerializer().deserialize(connection.get(lockKeyPath)));
		if (!lock.isExpiredOrMine(lockUUID)) {
			return false;
		}

		return acquireLock(connection);
	}

	/**
	 * Acquired lock release.
	 * 
	 * @param connection
	 */
	protected synchronized void releaseLock(RedisConnection connection) {
		if (isLocked()) {
			connection.del(lockKeyPath);
			this.lock = null;
		}
	}

	/**
	 * Check if owns the lock
	 * 
	 * @return true if lock owned
	 */
	protected synchronized boolean isLocked() {
		return this.lock != null;
	}

	/**
	 * Returns the expiry time of this lock
	 * 
	 * @return the expiry time in millis (or null if not locked)
	 */
	public synchronized long getLockExpiryTimeInMillis() {
		return this.lock.getExpiryTime();
	}

	private Lock asLock(long expires) {
		return new Lock(lockUUID, expires);
	}
	
	protected static class Lock {
		private UUID uuid;
		private long expiryTime;

		protected Lock(UUID uuid, long expiryTimeInMillis) {
			this.uuid = uuid;
			this.expiryTime = expiryTimeInMillis;
		}

		protected static Lock fromString(String text) {
			try {
				String[] parts = text.split(":");
				UUID theUUID = UUID.fromString(parts[0]);
				long theTime = Long.parseLong(parts[1]);
				return new Lock(theUUID, theTime);
			} catch (Exception any) {
				return NO_LOCK;
			}
		}

		public UUID getUUID() {
			return uuid;
		}

		public long getExpiryTime() {
			return expiryTime;
		}

		@Override
		public String toString() {
			return uuid.toString() + ":" + expiryTime;
		}

		boolean isExpired() {
			return getExpiryTime() < System.currentTimeMillis();
		}

		boolean isExpiredOrMine(UUID otherUUID) {
			return this.isExpired() || this.getUUID().equals(otherUUID);
		}
	}

}