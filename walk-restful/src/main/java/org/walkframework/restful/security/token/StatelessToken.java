package org.walkframework.restful.security.token;

import org.walkframework.shiro.authc.token.BaseToken;

/**
 * 无状态token
 * 
 * @author shf675
 * 
 */
public class StatelessToken extends BaseToken {

	private static final long serialVersionUID = 8587329689973009598L;

	private String appId;
	
	private String timestamp;
	
	private String sign;
	
	public StatelessToken(String appId, String timestamp, String sign) {
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
}
