package org.walkframework.data.exception;

public class ConditionEmptyException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConditionEmptyException() {
		super("Condition is empty, please call the entity object.asCondition ().setXX.");
	}

}
