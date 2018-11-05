package org.walkframework.shiro.web.filter.authc;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

/**
 * @author shf675
 *
 */
public class AuthFilterHelper {
	/**
	 * 解决会话固定攻击(Session fixation attack)问题。同时在新生成的sessionId后拼接加密后的用户名
	 * 
	 * 
	 * @param subject
	 * @param token
	 * 
	 * @see https://issues.apache.org/jira/browse/SHIRO-170
	 */
	public static void resolveSessionFixation(Subject subject, AuthenticationToken token){
		Session session = subject.getSession();
		
		// 保存会话的属性
		LinkedHashMap<Object, Object> attributes = new LinkedHashMap<Object, Object>();
		Collection<Object> keys = session.getAttributeKeys();
		for (Object key : keys) {
			Object value = session.getAttribute(key);
			if (value != null) {
				attributes.put(key, value);
			}
		}
		
		//结束登录时产生的会话
		session.stop();
		
		//登录
		subject.login(token);
		
		//重新生产会话
		session = subject.getSession();
		
		// 将属性值设置到新的会话中
		for (Object key : attributes.keySet()) {
			session.setAttribute(key, attributes.get(key));
		}
	}
}
