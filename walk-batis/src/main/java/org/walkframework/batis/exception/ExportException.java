package org.walkframework.batis.exception;

/**
 * 导出异常
 * 
 * @author shf675
 *
 */
public class ExportException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	public ExportException(Throwable cause){
		super("Export error.", cause);
	}

}
