package org.walkframework.restful.security.realm;

import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.walkframework.base.mvc.entity.TdMAppCfg;
import org.walkframework.base.system.interceptor.SignChecker;
import org.walkframework.restful.security.principal.StatelessPrincipal;
import org.walkframework.restful.security.token.StatelessToken;
import org.walkframework.shiro.authc.token.BaseToken;
import org.walkframework.shiro.realm.BaseUserRealm;


/**
 * 无状态realm
 * 
 * @author shf675
 *
 */
public class StatelessRealm extends BaseUserRealm {
	
	private SignChecker signChecker;
	
	/**
	 * 仅支持StatelessToken类型的Token
	 * @param token
	 * @return
	 */
	@Override
	public boolean support(AuthenticationToken token) {
		return token instanceof StatelessToken;
	}
	
	/**
	 * 获取用户认证信息
	 * 
	 * @param token 
	 * @return
	 * @throws AuthenticationException
	 */
	@Override
	protected AuthenticationInfo doGetUserAuthenticationInfo(BaseToken baseToken){
		StatelessToken token = (StatelessToken)baseToken;
		String appId = token.getAppId();
		String timestamp = token.getTimestamp();
		String sign = token.getSign();
		
		//委托给签名校验器校验
		TdMAppCfg appCfg = signChecker.check(appId, timestamp, sign);
		
		//程序身份信息
		StatelessPrincipal principal = new StatelessPrincipal(token);
		principal.setAppId(appCfg.getAppId());
		principal.setAppKey(appCfg.getAppKey());
		principal.setAppName(appCfg.getAppName());
		principal.setAppState(appCfg.getAppState());
		principal.setSignCheck("1".equals(appCfg.getSignCheck()));
		principal.setUrlCheck("1".equals(appCfg.getUrlCheck()));
		
		//将身份信息设置到token中
		token.setPrincipal(principal);
		return new SimpleAuthenticationInfo(principal, sign, getName());
	}

	/**
	 * 获取授权信息
	 * 
	 * @param principals
	 * @return
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
		
		try {
			StatelessPrincipal principal = (StatelessPrincipal)principals.getPrimaryPrincipal();
			String userId = principal.getAppId();
			
			//查询用户拥有角色列表
			try {
				List<String> roles = getUserService().findRoles(userId);
				authorizationInfo.addRoles(roles);
			} catch (Exception e) {
				log.warn("Not loaded to user role list information. The possible reason is that the findRoles statement is not implemented in UserSQL.xml");
			}
			
			//查询用户拥有权限列表
			try {
				List<String> permissions = getUserService().findPermissions(userId);
				authorizationInfo.addStringPermissions(permissions);
			} catch (Exception e) {
				log.warn("Not loaded to user permission list information. The possible reason is that the findPermissions statement is not implemented in UserSQL.xml");
			}

			return doGetUserAuthorizationInfo(principals, authorizationInfo);
		} catch (Throwable e) {
			if(e instanceof AuthorizationException){
				throw (AuthenticationException)e;
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
	protected AuthorizationInfo doGetUserAuthorizationInfo(PrincipalCollection principals, SimpleAuthorizationInfo authorizationInfo){
		return authorizationInfo;
	}
	
	public SignChecker getSignChecker() {
		return signChecker;
	}

	public void setSignChecker(SignChecker signChecker) {
		this.signChecker = signChecker;
	}
}
