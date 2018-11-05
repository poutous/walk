package org.walkframework.batis.exception;

/**
 * 批量更新异常
 * 
 * @author shf675
 *
 */
public class EmptyBatchListException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EmptyBatchListException() {
		super("List can not be empty when performing batch operation.");
	}

}
