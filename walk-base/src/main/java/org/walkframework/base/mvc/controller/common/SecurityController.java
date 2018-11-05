package org.walkframework.base.mvc.controller.common;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.walkframework.base.mvc.controller.base.BaseController;
import org.walkframework.base.system.common.Message;



/**
 * 安全
 *
 */
@RestController
public class SecurityController extends BaseController{
	
	/**
	 * 跳转到无权限界面
	 * 
	 * @return
	 */
	@RequestMapping("/unauthorized")
	public Object unauthorized(HttpServletRequest request) {
		String unauthorizedMessage = "您无权限访问该页面！";
		//ajax请求错误处理 或者 jquery.form提交错误处理
		if (common.isAjaxRequest(request) || "true".equals(request.getParameter(Message.JQUERY_FORM_AJAX_REQUEST))) {
			return message.error("您无权限访问该页面！");
		}
		
		ModelAndView mv = new ModelAndView("common/error/unauthorized");
		mv.addObject("unauthorizedMessage", unauthorizedMessage);
		return mv;
	}
	
}
