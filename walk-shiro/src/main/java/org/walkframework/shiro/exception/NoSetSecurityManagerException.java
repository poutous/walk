package org.walkframework.shiro.exception;

import org.apache.shiro.ShiroException;

/**
 * @author shf675
 *
 */
public class NoSetSecurityManagerException extends ShiroException {
	private static final long serialVersionUID = 1L;

	public NoSetSecurityManagerException() {
		super();
	}

	public NoSetSecurityManagerException(String message) {
		super(message);
	}

	public NoSetSecurityManagerException(Throwable cause) {
		super(cause);
	}

	public NoSetSecurityManagerException(String message, Throwable cause) {
		super(message, cause);
	}
}
