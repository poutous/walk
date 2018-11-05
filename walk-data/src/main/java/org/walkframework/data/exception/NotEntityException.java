package org.walkframework.data.exception;

public class NotEntityException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NotEntityException(Class<?> clazz) {
		super(clazz.getName() + "Non standard entity class, use the tool automatically generated");
	}

}
