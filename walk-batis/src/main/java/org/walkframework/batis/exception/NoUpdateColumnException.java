package org.walkframework.batis.exception;

/**
 * 未设置更新列异常
 * 
 * @author shf675
 *
 */
public class NoUpdateColumnException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NoUpdateColumnException() {
		super("No column to be updated.");
	}

}
