package org.walkframework.restful.exception;

/**
 * @author wangxin
 * 
 */
public class ConfigException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private static String msgPrefix = "配置有误 : ";

	public ConfigException() {
		super();
	}

	public ConfigException(String message, Throwable cause) {
		super(msgPrefix + message, cause);
	}

	public ConfigException(String format, Object... args) {
		super(msgPrefix + String.format(format, args));
	}

	public ConfigException(String message) {
		super(msgPrefix + message);

	}

	public ConfigException(Throwable cause) {
		super(cause);
	}

}
