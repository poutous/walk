package org.walkframework.restful.exception;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.walkframework.base.system.exception.SignCheckException;
import org.walkframework.restful.constant.RspConstants;
import org.walkframework.restful.model.rsp.RspInfo;

/**
 * 异常处理类
 * 
 * @author shf675
 */
@ControllerAdvice
public class RspExceptionHandler {

	protected Logger log = LoggerFactory.getLogger(this.getClass());

	@ExceptionHandler( { Throwable.class })
	@ResponseBody
	public Object exceptionHandler(HttpServletRequest request, HttpServletResponse response, Throwable e) {
		Object rspInfo = null;
		if (e instanceof HttpMessageNotReadableException) {
			rspInfo = getRspInfo(RspConstants.FORMAT_ERROR, getErrorMsg(RspConstants.FORMAT_ERROR, e), e);
		} else if (e instanceof HttpRequestMethodNotSupportedException) {
			rspInfo = getRspInfo(RspConstants.SUBMIT_METHOD_ERROR, getErrorMsg(RspConstants.SUBMIT_METHOD_ERROR, e), e);
		} else if (e instanceof SignCheckException) {
			rspInfo = getRspInfo(RspConstants.VALID_ERROR, getErrorMsg(RspConstants.VALID_ERROR, e), e);
		} else if (e instanceof RspException) {
			RspException ex = (RspException)e;
			rspInfo = getRspInfo(ex.getErrorCode(), ex.getErrorMsg(), e);
		} else {
			rspInfo = getRspInfo(RspConstants.OTHER_ERROR, getErrorMsg(RspConstants.OTHER_ERROR, e), e);
		}
		return rspInfo;
	}

	/**
	 * 获取错误信息
	 * 
	 * @param rspCode
	 * @param e
	 * @return
	 */
	private String getErrorMsg(Integer rspCode, Throwable e) {
		Object errorMessage = e.getMessage();
		//避免将sql抛到前台
		if (e instanceof BadSqlGrammarException) {
			errorMessage = e.getCause();
		}
		return RspConstants.RSP.get(rspCode) + "：" + errorMessage;
	}

	/**
	 * 返回信息
	 * 
	 * @param respDesc
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	private RspInfo getRspInfo(Integer rspCode, String rspDesc, Throwable e) {
		String errorMsg = RspConstants.RSP.get(rspCode);
		errorMsg = errorMsg == null ?  rspDesc: errorMsg + "：" + rspDesc;
		
		log.error(errorMsg, e);
		
		RspInfo rspInfo = new RspInfo();
		rspInfo.setRspCode(rspCode);
		rspInfo.setRspDesc(rspDesc);
		return rspInfo;
	}
}
