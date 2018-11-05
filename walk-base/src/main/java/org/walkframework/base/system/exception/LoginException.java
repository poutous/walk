package org.walkframework.base.system.exception;

import org.apache.shiro.authc.AccountException;

public class LoginException extends AccountException {
	private static final long serialVersionUID = 1L;

	public LoginException() {
	}

	public LoginException(String message) {
		super(message);
	}

	public LoginException(Throwable cause) {
		super(cause);
	}

	public LoginException(String message, Throwable cause) {
		super(message, cause);
	}
}
