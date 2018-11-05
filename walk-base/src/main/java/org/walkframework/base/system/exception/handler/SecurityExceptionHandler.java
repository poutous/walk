package org.walkframework.base.system.exception.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.walkframework.base.system.common.Message;
import org.walkframework.base.system.exception.LoginException;
import org.walkframework.base.tools.spring.SpringPropertyHolder;


/**
 * 异常处理类
 * 
 */
@ControllerAdvice
public class SecurityExceptionHandler extends BaseExceptionHandler{

	/**
	 * 无权限异常
	 * 
	 * @param request
	 * @param response
	 * @param e
	 * @return
	 */
	@ExceptionHandler( { UnauthorizedException.class })
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ResponseBody
	public Object unauthenticatedExceptionHandler(HttpServletRequest request, HttpServletResponse response, UnauthorizedException e) {
		setExceptionInfo(request, response, e);
		
		//ajax请求错误处理 或者 jquery.form提交错误处理
		if (common.isAjaxRequest(request) || "true".equals(request.getParameter(Message.JQUERY_FORM_AJAX_REQUEST))) {
			return message.error("无权限异常：" + e.getMessage());
		}
		return errorPage(e, getUnauthorizedPage());
	}

	/**
	 * Ticket方式登录异常处理
	 * 
	 * @param request
	 * @param e
	 * @return
	 */
	@ExceptionHandler( { LoginException.class })
	public ModelAndView ticketExceptionHandler(HttpServletRequest request, HttpServletResponse response, LoginException e) {
		setExceptionInfo(request, response, e);

		return errorPage(e, getUnauthorizedPage());
	}
	
	private String getUnauthorizedPage(){
		return SpringPropertyHolder.getContextProperty("page.unauthorized", "common/error/unauthorized");
	}
	
}
