package org.walkframework.data.exception;

public class EntityClassIsNullException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EntityClassIsNullException() {
		super("Entity null can not be class.");
	}

}
