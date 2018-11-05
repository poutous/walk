package org.walkframework.shiro.web.filter.authz;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.RolesAuthorizationFilter;

/**
 * 拓展org.apache.shiro.web.filter.authz.RolesAuthorizationFilter
 * shiro默认只支持且，不支持或，现拓展以支持or
 *
 * @author shf675
 */
public class WithORRolesAuthorizationFilter extends RolesAuthorizationFilter {
	
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws IOException {
    	Subject subject = getSubject(request, response);
    	
    	//查找存在或的配置，如有权限直接放行
    	boolean isAccessAllowed = OrAuthorizationHelper.orAccessAllowed(subject, (String[]) mappedValue);
        return isAccessAllowed ? isAccessAllowed : super.isAccessAllowed(request, response, mappedValue);
    }

}
