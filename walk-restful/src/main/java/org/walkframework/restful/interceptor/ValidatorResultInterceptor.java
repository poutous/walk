package org.walkframework.restful.interceptor;

import java.io.IOException;

import org.apache.shiro.authz.UnauthorizedException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.walkframework.restful.constant.RspConstants;
import org.walkframework.restful.model.rsp.RspInfo;

/**
 * spring校验结果拦截器
 * 
 * @author shf675
 *
 */
public class ValidatorResultInterceptor {

	protected Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * 环绕方法
	 * 
	 * @param pjp
	 * @param bindingResult
	 * @return
	 * @throws Throwable
	 */
	public Object doAround(ProceedingJoinPoint pjp, BindingResult bindingResult) throws Throwable {
		Object retVal = null;
		if (bindingResult != null && bindingResult.hasErrors()) {
			StringBuilder allErrors = new StringBuilder();
			allErrors.append(RspConstants.RSP.get(RspConstants.VALID_ERROR)).append("：");
			for (ObjectError err : bindingResult.getAllErrors()) {
				DefaultMessageSourceResolvable dmsr = (DefaultMessageSourceResolvable) err.getArguments()[0];
				allErrors.append("[" + dmsr.getCode() + "]" + err.getDefaultMessage() + ";");
			}
			String stringErrorInfo = allErrors.toString();
			retVal = getRspInfo(RspConstants.VALID_ERROR, stringErrorInfo);
			log.error(stringErrorInfo);
		} else {
			try {
				retVal = pjp.proceed();
			} catch(UnauthorizedException e){
				String errorMsg = RspConstants.RSP.get(RspConstants.INTERNAL_ERROR) + "：" + e.getMessage();
				retVal = getRspInfo(RspConstants.UNAUTHORIZED_ERROR, errorMsg);
				log.error(errorMsg, e);
			} catch (Throwable e) {
				Object errorMessage = e.getMessage();
				//避免将sql抛到前台
				if (e instanceof BadSqlGrammarException) {
					errorMessage = e.getCause();
				}
				String errorMsg = RspConstants.RSP.get(RspConstants.INTERNAL_ERROR) + "：" + errorMessage;
				retVal = getRspInfo(RspConstants.INTERNAL_ERROR, errorMsg);
				log.error(errorMsg, e);
			}
		}
		return retVal;
	}

	/**
	 * 返回信息
	 * 
	 * @param respDesc
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private RspInfo<?> getRspInfo(Integer rspCode, String rspDesc) {
		RspInfo<?> rspInfo = new RspInfo();
		rspInfo.setRspCode(rspCode);
		rspInfo.setRspDesc(rspDesc);
		return rspInfo;
	}
}
