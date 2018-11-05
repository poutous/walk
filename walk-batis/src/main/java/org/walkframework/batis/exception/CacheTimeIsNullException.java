package org.walkframework.batis.exception;

/**
 * 
 * @author shf675
 *
 */
public class CacheTimeIsNullException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CacheTimeIsNullException() {
		super("Cache time is not set to null.");
	}

}
