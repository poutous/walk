package org.walkframework.data.exception;

public class ConditionValueIsNullException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConditionValueIsNullException(String condition) {
		super("Condition[" + condition + "] corresponding value is null, please set the value or use.AndIsNull (column) conditions.addCondition () or andIsNotNull () to represent.");
	}

}
