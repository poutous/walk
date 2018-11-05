package org.walkframework.shiro.realm;

import java.util.List;

import javax.servlet.ServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.web.subject.WebSubject;
import org.walkframework.shiro.authc.principal.BasePrincipal;
import org.walkframework.shiro.authc.token.BaseToken;
import org.walkframework.shiro.authc.token.FormToken;
import org.walkframework.shiro.authc.token.SilenceLoginToken;
import org.walkframework.shiro.session.BaseSessionIdGenerator;


/**
 * 基于正常登录的realm
 * 
 * @author shf675
 * 
 */
public abstract class BaseUserRealm extends AbstractRealm {

	/**
	 * 获取认证信息
	 * 
	 * @param token
	 * @return
	 * @throws AuthenticationException
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		try {
			// 设置用户名
			ServletRequest request = ((WebSubject) SecurityUtils.getSubject()).getServletRequest();
			if (token instanceof FormToken) {
				request.setAttribute(BaseSessionIdGenerator.USER_NAME_KEY, ((FormToken) token).getUsername());
			} else if (token instanceof SilenceLoginToken) {
				request.setAttribute(BaseSessionIdGenerator.USER_NAME_KEY, ((SilenceLoginToken) token).getUsername());
			}

			return doGetUserAuthenticationInfo((BaseToken) token);
		} catch (Throwable e) {
			if (e instanceof AuthenticationException) {
				throw (AuthenticationException) e;
			}
			String msg = "Get authenticationInfo error! message:" + e.getMessage();
			log.error(msg, e);
			throw new AuthenticationException(msg, e);
		}
	}

	/**
	 * 获取用户认证信息
	 * 
	 * @param token
	 * @return
	 * @throws AuthenticationException
	 */
	protected abstract AuthenticationInfo doGetUserAuthenticationInfo(BaseToken token);

	/**
	 * 获取授权信息
	 * 
	 * @param principals
	 * @return
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		try {
			String userId = ((BasePrincipal) principals.getPrimaryPrincipal()).getUserId();
			SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();

			// 查询用户拥有角色列表
			try {
				List<String> roles = getUserService().findRoles(userId);
				authorizationInfo.addRoles(roles);
			} catch (Exception e) {
				log.warn("Not loaded to user role list information. The possible reason is that the findRoles statement is not implemented in UserSQL.xml");
			}

			// 查询用户拥有权限列表
			try {
				List<String> permissions = getUserService().findPermissions(userId);
				authorizationInfo.addStringPermissions(permissions);
			} catch (Exception e) {
				log.warn("Not loaded to user permission list information. The possible reason is that the findPermissions statement is not implemented in UserSQL.xml");
			}

			return doGetUserAuthorizationInfo(principals, authorizationInfo);
		} catch (Throwable e) {
			if (e instanceof AuthorizationException) {
				throw (AuthenticationException) e;
			}
			String msg = "Get AuthorizationInfo error. message:" + e.getMessage();
			log.error(msg, e);
			throw new AuthorizationException(msg, e);
		}
	}

	/**
	 * 获取用户授权信息
	 * 
	 * @param principals
	 * @param authorizationInfo
	 * @return
	 */
	protected abstract AuthorizationInfo doGetUserAuthorizationInfo(PrincipalCollection principals, SimpleAuthorizationInfo authorizationInfo);

}
