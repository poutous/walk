package org.walkframework.data.exception;

public class NoColumnException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	public NoColumnException(String table, String column){
		super(table + "table no" + column + "column.");
	}

}
