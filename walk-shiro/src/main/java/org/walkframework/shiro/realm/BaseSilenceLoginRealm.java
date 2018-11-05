package org.walkframework.shiro.realm;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.walkframework.shiro.authc.token.BaseToken;
import org.walkframework.shiro.authc.token.SilenceLoginToken;
import org.walkframework.shiro.util.ISignChecker;


/**
 * 基于静默登录的realm
 * 
 * @author shf675
 *
 */
public abstract class BaseSilenceLoginRealm extends BaseUserRealm {

	private ISignChecker signChecker;
	
	/**
	 * 获取用户认证信息
	 * 
	 * @param token 
	 * @return
	 * @throws AuthenticationException
	 */
	@Override
	protected AuthenticationInfo doGetUserAuthenticationInfo(BaseToken token) {
		try {
			if (token instanceof SilenceLoginToken) {
				SilenceLoginToken silenceToken = (SilenceLoginToken) token;
				String appId = silenceToken.getAppId();
				String timestamp = silenceToken.getTimestamp();
				String sign = silenceToken.getSign();
				//委托给签名校验器校验
				signChecker.check(appId, timestamp, sign);
			}
			return doGetSilenceUserAuthenticationInfo((BaseToken) token);
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
	protected abstract AuthenticationInfo doGetSilenceUserAuthenticationInfo(BaseToken token);

	public ISignChecker getSignChecker() {
		return signChecker;
	}

	public void setSignChecker(ISignChecker signChecker) {
		this.signChecker = signChecker;
	}
}
