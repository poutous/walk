package org.walkframework.base.system.exception;

import org.apache.shiro.authc.AccountException;

/**
 * 签名校验异常类
 * 
 * @author shf675
 *
 */
public class SignCheckException extends AccountException {
	private static final long serialVersionUID = 1L;

	public SignCheckException() {
	}

	public SignCheckException(String message) {
		super(message);
	}

	public SignCheckException(Throwable cause) {
		super(cause);
	}

	public SignCheckException(String message, Throwable cause) {
		super(message, cause);
	}
}
