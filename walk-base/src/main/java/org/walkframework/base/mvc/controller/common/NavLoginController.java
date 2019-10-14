package org.walkframework.base.mvc.controller.common;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.walkframework.base.mvc.controller.base.BaseController;
import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.shiro.util.AuthErrorUtil;

/**
 * walk开发平台登出 
 *
 */
@RestController
@RequestMapping(value = "/login")
public class NavLoginController extends BaseController {
	
	/**
	 * 登录
	 * 
	 * @return
	 */
	@RequestMapping(value = "/navlogin")
	public ModelAndView login(Model model, HttpServletRequest request) {
		String failureKeyAttribute = SpringContextHolder.getBean(FormAuthenticationFilter.class).getFailureKeyAttribute();
        model.addAttribute("error", AuthErrorUtil.getErrorInfo((AuthenticationException)SecurityUtils.getSubject().getSession().getAttribute(failureKeyAttribute)));
        SecurityUtils.getSubject().getSession().removeAttribute(failureKeyAttribute);
		return new ModelAndView("common/login/Login");
	}

	/**
	 * 退出
	 * @param request
	 * @throws Exception
	 */
	@RequestMapping(value = "/navlogout")
	public String logout(HttpServletRequest request) throws Exception {
		//退出
		SecurityUtils.getSubject().logout();
		return message.success("退出成功！");
	}
}