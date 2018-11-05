package org.walkframework.shiro.web.servlet;

import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.beans.factory.InitializingBean;

/**
 * cookie扩展
 * 
 * 会话集群(多工程会话共享)
 * 
 * @author shf675
 *
 */
public class SharedSessionCookie extends SimpleCookie implements InitializingBean {

	private boolean sharedSession;

	private String sharedSessionCookiePath = "/";

	@Override
	public void afterPropertiesSet() throws Exception {
		//如果设置了共享会话，设置共享会话cookiepath
		if (isSharedSession()) {
			setPath(getSharedSessionCookiePath());
		}
	}

	public String getSharedSessionCookiePath() {
		return sharedSessionCookiePath;
	}

	public void setSharedSessionCookiePath(String sharedSessionCookiePath) {
		this.sharedSessionCookiePath = sharedSessionCookiePath;
	}

	public boolean isSharedSession() {
		return sharedSession;
	}

	public void setSharedSession(boolean sharedSession) {
		this.sharedSession = sharedSession;
	}

}
