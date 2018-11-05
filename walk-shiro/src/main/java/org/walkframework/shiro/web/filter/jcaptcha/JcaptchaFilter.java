package org.walkframework.shiro.web.filter.jcaptcha;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.walkframework.shiro.exception.IdentifyingCodeErrorException;
import org.walkframework.shiro.service.JcaptchaImageCaptchaService;

/**
 * 验证码过滤器
 *
 */
public class JcaptchaFilter extends AccessControlFilter {

	private JcaptchaImageCaptchaService jcaptchaImageCaptchaService;

	private boolean jcaptchaEnabled = true;
	
	private boolean onDeniedRedirectToLogin;

	private String failureKeyAttribute = "shiroLoginFailure";

	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
		//1.设置验证码是否开启属性
		request.setAttribute("jcaptchaEnabled", isJcaptchaEnabled());
		HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
		//2.判断验证码是否禁用, 或不是表单提交
		if (!isJcaptchaEnabled() || !"post".equalsIgnoreCase(httpServletRequest.getMethod())) {
			return true;
		}
		//3.表单提交, 验证验证码是否正确
		return getJcaptchaImageCaptchaService().validateJcaptcha(httpServletRequest);
	}

	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		//验证码失败, 存储失败key
		request.setAttribute(getFailureKeyAttribute(), new IdentifyingCodeErrorException());
		if(onDeniedRedirectToLogin){
			SecurityUtils.getSubject().getSession().setAttribute(getFailureKeyAttribute(), request.getAttribute(getFailureKeyAttribute()));
			//重定向到登录界面
			redirectToLogin(request, response);
		}
		return false;
	}

	public boolean isJcaptchaEnabled() {
		return jcaptchaEnabled;
	}

	public void setJcaptchaEnabled(boolean jcaptchaEnabled) {
		this.jcaptchaEnabled = jcaptchaEnabled;
	}

	public String getFailureKeyAttribute() {
		return failureKeyAttribute;
	}

	public void setFailureKeyAttribute(String failureKeyAttribute) {
		this.failureKeyAttribute = failureKeyAttribute;
	}

	public JcaptchaImageCaptchaService getJcaptchaImageCaptchaService() {
		return jcaptchaImageCaptchaService;
	}

	public void setJcaptchaImageCaptchaService(JcaptchaImageCaptchaService jcaptchaImageCaptchaService) {
		this.jcaptchaImageCaptchaService = jcaptchaImageCaptchaService;
	}

	public boolean isOnDeniedRedirectToLogin() {
		return onDeniedRedirectToLogin;
	}

	public void setOnDeniedRedirectToLogin(boolean onDeniedRedirectToLogin) {
		this.onDeniedRedirectToLogin = onDeniedRedirectToLogin;
	}
}