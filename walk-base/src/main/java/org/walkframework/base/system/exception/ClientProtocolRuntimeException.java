package org.walkframework.base.system.exception;


/**
 * Signals an error in the HTTP protocol.
 *
 * 
 */
public class ClientProtocolRuntimeException extends RuntimeException {
	private static final long serialVersionUID = -3677401974197330140L;

	public ClientProtocolRuntimeException() {
        super();
    }

    public ClientProtocolRuntimeException(final String s) {
        super(s);
    }

    public ClientProtocolRuntimeException(final Throwable cause) {
        initCause(cause);
    }

    public ClientProtocolRuntimeException(final String message, final Throwable cause) {
        super(message);
        initCause(cause);
    }


}
