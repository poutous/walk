package org.walkframework.restful.exception;

/**
 * @author wangxin
 * 
 */
public class ExcelDataException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private static String msgPrefix = "文档有误 : ";

	public ExcelDataException() {
		super();
	}

	public ExcelDataException(String message, Throwable cause) {
		super(msgPrefix + message, cause);
	}

	public ExcelDataException(String message) {
		super(msgPrefix + message);

	}

	public ExcelDataException(String format, Object... args) {
		super(msgPrefix + String.format(format, args));
	}

	public ExcelDataException(Throwable cause) {
		super(cause);
	}

}
