package org.walkframework.shiro.realm;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.AuthorizingRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.shiro.service.IUserService;

/**
 * realm继承此基类
 * 
 * 加入认证信息与授权信息设置缓存时间支持
 * 
 * 注意：
 * 1、子类原本需要实现doGetAuthenticationInfo方法，现修改为需实现doGetUserAuthenticationInfo方法
 * 2、子类原本需要实现doGetAuthorizationInfo方法，现修改为需实现doGetUserAuthorizationInfo方法
 * 3、子类必须实现support方法
 * 
 * @author shf675
 */
public abstract class AbstractRealm extends AuthorizingRealm {
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	private IUserService userService;
	
	public void setUserService(IUserService userService) {
		this.userService = userService;
	}
	
	public IUserService getUserService() {
		return userService;
	}
	
	/**
	 * 设置支持的token类型
	 * 
	 * @param token
	 * @return
	 */
	@Override
	public boolean supports(AuthenticationToken token) {
		return support(token);
	}
	
	/**
	 * 设置支持的token类型
	 * 子类必须实现
	 * @param token
	 * @return
	 */
	protected abstract boolean support(AuthenticationToken token);
}
