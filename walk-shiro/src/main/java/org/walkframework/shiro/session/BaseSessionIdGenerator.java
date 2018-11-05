package org.walkframework.shiro.session;

import java.io.Serializable;

import javax.servlet.ServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.web.subject.WebSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.shiro.util.password.Encryptor;

/**
 * 扩展默认的sessionId生成器
 * 
 * @author shf675
 * 
 */
public class BaseSessionIdGenerator extends JavaUuidSessionIdGenerator {

	private static final Logger log = LoggerFactory.getLogger(BaseSessionIdGenerator.class);
	
	public static final String USER_NAME_KEY = "__USER__NAME__";

	public static final String USER_NAME_PREFIX = "!";
	
	/**
	 * sessionId 盐值
	 */
	private String sessionIdSalt;

	@Override
	public Serializable generateId(Session session) {
		Serializable sessionId = super.generateId(session);

		ServletRequest request = ((WebSubject)SecurityUtils.getSubject()).getServletRequest();
		if (request != null) {
			Object username = request.getAttribute(USER_NAME_KEY);
			if (username != null && !"".equals(username)) {
				// 末尾加入加密后的用户名
				sessionId = sessionId.toString().concat(USER_NAME_PREFIX).concat(Encryptor.encrypt(username.toString(), getSessionIdSalt()).toLowerCase());
			}
		}

		return sessionId;
	}
	
	public void setSessionIdSalt(String sessionIdSalt) {
		this.sessionIdSalt = sessionIdSalt;
	}
	
	public String getSessionIdSalt() {
		if(this.sessionIdSalt == null){
			this.sessionIdSalt = "";
			log.warn("[sessionIdSalt] not configured in the property file. The default value of salt is empty string.");
		}
		return sessionIdSalt;
	}
}
