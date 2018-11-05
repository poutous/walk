package org.walkframework.restful.exception;

import org.apache.shiro.authc.AccountException;
import org.walkframework.restful.constant.RspConstants;

/**
 * 签名校验异常类
 * 
 * @author shf675
 *
 */
public class RspException extends AccountException {
	private static final long serialVersionUID = 1L;
	
	private Integer errorCode;
	private String errorMsg;

	public RspException() {
	}

	public RspException(String errorMsg) {
		this(RspConstants.OTHER_ERROR, errorMsg);
	}
	
	public RspException(Integer errorCode, String errorMsg) {
		this(errorCode, errorMsg, null);
	}

	public RspException(Integer errorCode, String errorMsg, Throwable cause) {
		super(errorMsg, cause);
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}
	
	public Integer getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
}
