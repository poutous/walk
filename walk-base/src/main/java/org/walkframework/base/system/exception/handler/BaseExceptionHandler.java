package org.walkframework.base.system.exception.handler;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.util.StreamUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.common.Message;
import org.walkframework.base.system.factory.SingletonFactory;
import org.walkframework.base.tools.spring.SpringPropertyHolder;

/**
 * 异常处理类
 * 
 * @author shf675
 */
@ControllerAdvice
public class BaseExceptionHandler {
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	protected final static Common common = SingletonFactory.getInstance(Common.class);

	protected final static Message message = SingletonFactory.getInstance(Message.class);

	/**
	 * 
	 * 通用错误消息提示
	 * 
	 * @param request
	 * @param response
	 * @param e
	 * @return
	 * @throws IOException
	 */
	@ExceptionHandler( { Exception.class })
	@ResponseBody
	public Object exceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException {
		setExceptionInfo(request, response, e);

		String errorMessage = e.getMessage();

		if (e instanceof BadSqlGrammarException) {// 避免将sql抛到前台
			errorMessage = e.getCause() == null ? "" : e.getCause().getMessage();
		} else if (e instanceof BindException) {
			errorMessage = "Binding error: the input value type is incorrect, please re-enter!";
		}

		// ajax请求错误处理
		if (common.isAjaxRequest(request)) {
			writeInternal(errorMessage, response);
		}

		// jquery.form提交错误处理
		else if ("true".equals(request.getParameter(Message.JQUERY_FORM_AJAX_REQUEST))) {
			writeInternal(message.error(errorMessage), response);
		}

		// 直接返回错误页面
		else {
			return errorPage(e);
		}
		return null;
	}
	
	/**
	 * 转向错误页面
	 * 
	 * @param e
	 * @return
	 */
	protected ModelAndView errorPage(Exception e) {
		String errorPage = SpringPropertyHolder.getContextProperty("page.error", "common/error/error");
		return errorPage(e, errorPage);
	}

	/**
	 * 转向错误页面
	 * 
	 * @param e
	 * @return
	 */
	protected ModelAndView errorPage(Exception e, String page) {
		ModelAndView mv = new ModelAndView();
		mv.addObject("exception", e);
		mv.setViewName(page);
		return mv;
	}

	/**
	 * 设置异常信息
	 * 
	 * @param request
	 * @param response
	 * @param e
	 */
	protected void setExceptionInfo(HttpServletRequest request, HttpServletResponse response, Exception e) {
		log.error(e.getMessage(), e);
		response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
		request.setAttribute("success", "false");
		request.setAttribute("exception", common.getErrorInfo(e.getMessage()));
		// 非生产模式程序报错页面显示错误轨迹(错误详情)
		if (!"true".equals(SpringPropertyHolder.getContextProperty("productMode"))) {
			request.setAttribute("exceptionTrace", common.getErrorInfo(common.getStackTrace(e)));
		}
	}

	/**
	 * 向浏览器里写入信息
	 * 
	 * @param text
	 * @param response
	 * @throws IOException
	 */
	protected void writeInternal(String text, HttpServletResponse response) throws IOException {
		StreamUtils.copy(text, Charset.forName(response.getCharacterEncoding()), response.getOutputStream());
	}
}
