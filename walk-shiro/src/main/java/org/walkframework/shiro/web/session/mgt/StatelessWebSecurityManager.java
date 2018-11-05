package org.walkframework.shiro.web.session.mgt;

import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

/**
 * @author shf675
 *
 */
public class StatelessWebSecurityManager extends DefaultWebSecurityManager {

	@Override
	public void setSessionManager(SessionManager sessionManager) {
		super.setSessionManager(sessionManager);
		((DefaultSessionStorageEvaluator) ((DefaultSubjectDAO) getSubjectDAO()).getSessionStorageEvaluator()).setSessionStorageEnabled(false);
	}
}
