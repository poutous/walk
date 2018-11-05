package org.walkframework.shiro.session;

import java.io.Serializable;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.walkframework.shiro.cache.ShiroCache;
import org.walkframework.shiro.web.session.mgt.ShiroWebSessionManager;

/**
 * 自定义sessionDao
 * 
 * @author shf675
 * 
 */
public class BaseSessionDAO extends EnterpriseCacheSessionDAO {
	
	private ShiroWebSessionManager sessionManager;

	/**
	 * 缓存session 并设置过期时间
	 * 
	 * @param session
	 * @param sessionId
	 * @param cache
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void cache(Session session, Serializable sessionId, Cache<Serializable, Session> cache) {
		super.cache(session, sessionId, cache);
		if(SecurityUtils.getSubject().isAuthenticated() && cache instanceof ShiroCache){
			long globalSessionTimeout = getSessionManager().getGlobalSessionTimeout();
			((ShiroCache) cache).expire(sessionId, globalSessionTimeout / 1000);
		}
	}

	public ShiroWebSessionManager getSessionManager() {
		return sessionManager;
	}

	public void setSessionManager(ShiroWebSessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
}
