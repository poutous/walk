package org.walkframework.base.system.exception;

/**
 * 一个属性不允许有多个翻译器
 * 
 * @author shf675
 *
 */
public class MultipleTranslatorException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MultipleTranslatorException() {
		super("each attribute is not allowed to have more than one translator.");
	}
}
