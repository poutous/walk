package org.walkframework.data.exception;

public class EmptyEntityException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EmptyEntityException() {
		super("Entity object is an empty object, please set the property value");
	}

}
