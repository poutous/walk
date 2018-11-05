package org.walkframework.batis.exception;

/**
 * 检查分页数量是否超出最大限制结果集设置异常
 * 
 * @author shf675
 *
 */
public class ExceedsMaxLimitResultsetException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ExceedsMaxLimitResultsetException(int maxLimitResultset, int currPageSize) {
		super("Expected Maximum limit result set. The maximum limit result set is " + maxLimitResultset + ", but the target return result set is " + currPageSize + ".");
	}

}
