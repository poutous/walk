package org.walkframework.shiro.web.filter.authc;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.walkframework.shiro.authc.token.SilenceLoginToken;
import org.walkframework.shiro.realm.BaseUserRealm;


/**
 * 
 * 静默登录过滤器
 * 
 * @author shf675
 *
 */
public class SilenceLoginAuthFilter extends AuthenticatingFilter {

	private static Logger logger = LoggerFactory.getLogger(SilenceLoginAuthFilter.class);

	public static final String DEFAULT_ERROR_KEY_ATTRIBUTE_NAME = "shiroLoginFailure";

	private static final String USERNAME_PARAMETER = "username";
	private static final String APPID_PARAMETER = "appId";
	private static final String TIMESTAMP_PARAMETER = "timestamp";
	private static final String SIGN_PARAMETER = "sign";

	private BaseUserRealm realm;

	private String failureKeyAttribute = DEFAULT_ERROR_KEY_ATTRIBUTE_NAME;

	@Override
	protected SilenceLoginToken createToken(ServletRequest request, ServletResponse response) {
		String username = request.getParameter(USERNAME_PARAMETER);
		String appId = request.getParameter(APPID_PARAMETER);
		String timestamp = request.getParameter(TIMESTAMP_PARAMETER);
		String sign = request.getParameter(SIGN_PARAMETER);

		Assert.hasText(username, USERNAME_PARAMETER + " can't be empty!");
		Assert.hasText(appId, APPID_PARAMETER + " can't be empty!");
		Assert.hasText(timestamp, TIMESTAMP_PARAMETER + " can't be empty!");
		Assert.hasText(sign, SIGN_PARAMETER + " can't be empty!");

		return new SilenceLoginToken(username, appId, timestamp, sign);
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		//登录前设置realm
		((RealmSecurityManager) SecurityUtils.getSecurityManager()).setRealm(getRealm());

		return executeLogin(request, response);
	}

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
		return false;
	}

	@Override
	protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
		issueSuccessRedirect(request, response);
		return false;
	}

	/**
	 * 登录失败后设置错误信息
	 * 
	 */
	@Override
	protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
		logger.error("Authentication exception", e);
		SecurityUtils.getSubject().getSession().setAttribute(getFailureKeyAttribute(), e);
		try {
			WebUtils.redirectToSavedRequest(request, response, getSuccessUrl());
		} catch (IOException e1) {
			logger.error(e1.getMessage(), e);
		}
		return false;
	}

	public BaseUserRealm getRealm() {
		return realm;
	}

	public void setRealm(BaseUserRealm realm) {
		this.realm = realm;
	}

	public String getFailureKeyAttribute() {
		return failureKeyAttribute;
	}

	public void setFailureKeyAttribute(String failureKeyAttribute) {
		this.failureKeyAttribute = failureKeyAttribute;
	}
}
