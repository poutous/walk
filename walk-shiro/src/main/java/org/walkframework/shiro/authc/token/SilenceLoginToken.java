package org.walkframework.shiro.authc.token;

import org.walkframework.shiro.authc.token.BaseToken;

/**
 * 静默登录token
 * 
 * @author shf675
 *
 */
public class SilenceLoginToken extends BaseToken {
	
	private static final long serialVersionUID = 8587329689973009598L;
	
	private String username;

	private String appId;

	private String timestamp;

	private String sign;

	public SilenceLoginToken(String username, String appId, String timestamp, String sign) {
		this.username = username;
		this.appId = appId;
		this.timestamp = timestamp;
		this.sign = sign;
	}

	@Override
	public String getCredentials() {
		return sign;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
