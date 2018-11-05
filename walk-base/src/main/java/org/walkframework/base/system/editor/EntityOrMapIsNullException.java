package org.walkframework.base.system.editor;

/**
 * 
 * @author shf675
 *
 */
public class EntityOrMapIsNullException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EntityOrMapIsNullException() {
		super("object is null.");
	}
}
