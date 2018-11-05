package org.walkframework.shiro.web.session.mgt;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.session.ExpiredSessionException;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.cache.util.ReflectHelper;
import org.walkframework.shiro.cache.ShiroCache;
import org.walkframework.shiro.session.BaseSessionDAO;

/**
 * 会话管理器
 * 
 * @author shf675
 * 
 */
public class ShiroWebSessionManager extends DefaultWebSessionManager {

	private static final Logger log = LoggerFactory.getLogger(ShiroWebSessionManager.class);
	
	/**
	 * 是否校验未登录状态下产生的会话
	 */
	private boolean validateNotLogin = true;

	/**
	 * 将sessionManager设置给sessionDAO
	 * 
	 * @return
	 */
	@Override
	public void setSessionDAO(SessionDAO sessionDAO) {
		super.setSessionDAO(sessionDAO);
		if (sessionDAO != null && sessionDAO instanceof BaseSessionDAO) {
			((BaseSessionDAO) sessionDAO).setSessionManager(this);
		}
	}

	/**
	 * 重写父类会话校验方法，避免直接调用getActiveSessions造成的性能问题
	 * 
	 * @return
	 */
	@Override
	public void validateSessions() {
		Cache<Serializable, Session> cache = getActiveSessionsCacheLazy();
		if (cache != null && cache instanceof ShiroCache) {
			if (log.isInfoEnabled()) {
				log.info("Validating all active sessions...");
			}

			int invalidCount = 0;
			Iterator<Object> keysIterator = ((ShiroCache<Serializable, Session>) cache).getNativeCache().keys();
			if (keysIterator != null) {
				while (keysIterator.hasNext()) {
					Serializable sessionId = (Serializable) keysIterator.next();
					Session s = cache.get(sessionId);
					if (s != null) {
						try {
							validate(s, null);

							//未登录状态下产生的会话视为无效会话，需要清理。
							if(isValidateNotLogin()){
								validateNotLogin(s, cache);
							}
						} catch (InvalidSessionException e) {
							if (log.isDebugEnabled()) {
								boolean expired = (e instanceof ExpiredSessionException);
								String msg = "Invalidated session with id [" + s.getId() + "]" + (expired ? " (expired)" : " (stopped)");
								log.debug(msg);
							}
							invalidCount++;
						} catch (Throwable e) {
							log.error("validate session error with id [" + s.getId() + "]", e);
						}
					}
				}
			}

			if (log.isInfoEnabled()) {
				String msg = "Finished session validation.";
				if (invalidCount > 0) {
					msg += "  [" + invalidCount + "] sessions were stopped.";
				} else {
					msg += "  No sessions were stopped.";
				}
				log.info(msg);
			}
		} else {
			super.validateSessions();
		}
	}

	/**
	 * 未登录状态下产生的会话视为无效会话，需要清理。
	 * 
	 * @param session
	 */
	protected void validateNotLogin(Session session, Cache<Serializable, Session> cache) {
		Object principals = session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
		if (principals == null || "".equals(principals.toString())) {
			session.stop();
			cache.remove(session.getId());
			throw new InvalidSessionException("Session with id [" + session.getId() + "] is generated in no login status, has been explicitly stopped.");
		}
	}

	/**
	 * 获取会话所在的缓存对象
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Cache<Serializable, Session> getActiveSessionsCacheLazy() {
		SessionDAO sessionDAO = super.getSessionDAO();
		if (sessionDAO != null && sessionDAO instanceof CachingSessionDAO) {
			return (Cache<Serializable, Session>) ReflectHelper.invokeMethod(super.getSessionDAO(), CachingSessionDAO.class, "getActiveSessionsCacheLazy", null, null);
		}
		return null;
	}
	
	public boolean isValidateNotLogin() {
		return validateNotLogin;
	}

	public void setValidateNotLogin(boolean validateNotLogin) {
		this.validateNotLogin = validateNotLogin;
	}
}
