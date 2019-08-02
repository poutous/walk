package org.walkframework.shiro.web.servlet;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.servlet.ShiroHttpServletResponse;

/**
 * 扩展ShiroHttpServletResponse 解决重定向协议问题
 * 
 * @author shf675
 * 
 */
public class ShiroExtHttpServletResponse extends ShiroHttpServletResponse {

	public ShiroExtHttpServletResponse(HttpServletResponse wrapped, ServletContext context, ShiroHttpServletRequest request) {
		super(wrapped, context, request);
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		location = resolveHttpsRedirect(location);
		
		//weblogic容器特殊处理，解决Response already committed报错
		if(isWeblogic()){
			getResponse().reset();
		}
		
		super.sendRedirect(location);
	}

	/**
	 * 当容器是http，代理或负载是https时，解决重定向协议问题。
	 * 
	 * underscores_in_headers on;
	 * proxy_set_header https_ignore_servers xxx.xxx.xxx.xx1|xxx.xxx.xxx.xx2;
	 * 
	 * proxy_set_header X-Forwarded-Proto https;
	 * 
	 * @param location
	 * @return
	 */
	protected String resolveHttpsRedirect(String location) {
		HttpServletRequest request = getRequest();
		//忽略的服务处理
		String httpsIgnoreServers = request.getHeader("https_ignore_servers") + "";
		if(request.getServerName().matches(httpsIgnoreServers)) {
			return location;
		}
				
		String scheme = request.getScheme();
		String protocolHeaderValue = request.getHeader("X-Forwarded-Proto");
		if ("http".equalsIgnoreCase(scheme) && "https".equalsIgnoreCase(protocolHeaderValue) 
				&& location != null && !location.toLowerCase().startsWith("http://") && !location.toLowerCase().startsWith("https://")) {
			
			StringBuilder httpsLocation = new StringBuilder();
			httpsLocation.append("https://").append(request.getServerName());
			if (request.getServerPort() != 80 && request.getServerPort() != 443) {
				httpsLocation.append(":").append(request.getServerPort());
			}
			//httpsLocation.append(request.getContextPath());

			if (!location.startsWith("/")) {
				httpsLocation.append("/");
			}
			httpsLocation.append(location);

			location = httpsLocation.toString();
		}
		return location;
	}
	
	/**
	 * 判断是否是weblogic容器
	 * 
	 * @return
	 */
	private boolean isWeblogic(){
		return this.getRequest().getSession().getServletContext().getServerInfo().toLowerCase().indexOf("weblogic") > -1;
	}
}
