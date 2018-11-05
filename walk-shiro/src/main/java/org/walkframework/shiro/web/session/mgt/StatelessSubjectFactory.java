package org.walkframework.shiro.web.session.mgt;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.mgt.DefaultWebSubjectFactory;

/**
 * @author shf675
 *
 */
public class StatelessSubjectFactory extends DefaultWebSubjectFactory {
	public Subject createSubject(SubjectContext context) {
		//不创建session  
		context.setSessionCreationEnabled(false);
		return super.createSubject(context);
	}
}