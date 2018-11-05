package org.walkframework.redis.lock;

/**
 * 获取锁异常类
 * 
 * @author shf675
 * 
 */
public class AcquireLockException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AcquireLockException(Throwable cause) {
		super("acquire lock error.", cause);
	}

}
