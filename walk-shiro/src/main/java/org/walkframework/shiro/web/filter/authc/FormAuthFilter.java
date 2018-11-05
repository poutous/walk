package org.walkframework.shiro.web.filter.authc;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.shiro.authc.token.FormToken;
import org.walkframework.shiro.realm.BaseUserRealm;
import org.walkframework.shiro.util.password.DefaultPasswordEncryptor;
import org.walkframework.shiro.util.password.PasswordEncryptor;

/**
 * 基于表单的身份验证过滤器
 * 
 * @author shf675
 */
public class FormAuthFilter extends FormAuthenticationFilter {

	private static final Logger log = LoggerFactory.getLogger(FormAuthFilter.class);

	//密码加密器，默认使用shiro自带的Sha256Hash方式加密
	private PasswordEncryptor passwordEncryptor = new DefaultPasswordEncryptor();

	private BaseUserRealm realm;
	
	/**
	 * 是否使用盐值
	 */
	private boolean useSalt = true;

	/**
	 * 创建token
	 * 
	 */
	@Override
	protected FormToken createToken(ServletRequest request, ServletResponse response) {
		String username = getUsername(request);
		String password = getPassword(request);
		boolean rememberMe = isRememberMe(request);
		String host = getHost(request);

		FormToken token = new FormToken(username, password);
		token.setRememberMe(rememberMe);
		token.setHost(host);
		token.setUseSalt(isUseSalt());
		
		//设置密码加密器
		token.setPasswordEncryptor(getPasswordEncryptor());
		return token;
	}

	@Override
	protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
		//登录前设置realm
		((RealmSecurityManager)SecurityUtils.getSecurityManager()).setRealm(getRealm());
		
		AuthenticationToken token = createToken(request, response);
		if (token == null) {
			String msg = "createToken method implementation returned null. A valid non-null AuthenticationToken " + "must be created in order to execute a login attempt.";
			throw new IllegalStateException(msg);
		}
		try {
			Subject subject = getSubject(request, response);
			
			//解决会话固定攻击问题
			AuthFilterHelper.resolveSessionFixation(subject, token);
			
			return onLoginSuccess(token, subject, request, response);
		} catch (AuthenticationException e) {
			return onLoginFailure(token, e, request, response);
		}
	}

	/**
	 * 登录失败后设置错误信息
	 * 
	 */
	@Override
	protected void setFailureAttribute(ServletRequest request, AuthenticationException ae) {
		log.error("Authentication exception", ae);
		SecurityUtils.getSubject().getSession().setAttribute(getFailureKeyAttribute(), ae);
	}

	@Override
	protected String getHost(ServletRequest req) {
		HttpServletRequest request = (HttpServletRequest) req;
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	public PasswordEncryptor getPasswordEncryptor() {
		return passwordEncryptor;
	}

	public void setPasswordEncryptor(PasswordEncryptor passwordEncryptor) {
		this.passwordEncryptor = passwordEncryptor;
	}

	public BaseUserRealm getRealm() {
		return realm;
	}

	public void setRealm(BaseUserRealm realm) {
		this.realm = realm;
	}
	
	public boolean isUseSalt() {
		return useSalt;
	}

	public void setUseSalt(boolean useSalt) {
		this.useSalt = useSalt;
	}
}