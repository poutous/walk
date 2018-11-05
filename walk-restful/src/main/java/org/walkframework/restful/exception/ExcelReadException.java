package org.walkframework.restful.exception;

/**
 * @author wangxin
 * 
 */
public class ExcelReadException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private static String msgPrefix = "文档读取异常 : ";

	public ExcelReadException() {
		super();

	}

	public ExcelReadException(String message, Throwable cause) {
		super(msgPrefix + message, cause);
	}

	public ExcelReadException(String format, Object... args) {
		super(msgPrefix + String.format(format, args));
	}

	public ExcelReadException(String message) {
		super(msgPrefix + message);

	}

	public ExcelReadException(Throwable cause) {
		super(cause);

	}

}
