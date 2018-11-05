package org.walkframework.shiro.exception;

import org.apache.shiro.authc.AuthenticationException;

/**
 * 验证码错误
 * 
 * @author shf675
 *
 */
public class IdentifyingCodeErrorException extends AuthenticationException {
	private static final long serialVersionUID = 1L;

	public IdentifyingCodeErrorException() {
		super();
	}

	public IdentifyingCodeErrorException(String message) {
		super(message);
	}

	public IdentifyingCodeErrorException(Throwable cause) {
		super(cause);
	}

	public IdentifyingCodeErrorException(String message, Throwable cause) {
		super(message, cause);
	}
}
