package org.walkframework.shiro.web.filter.authc;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.authc.AuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.walkframework.shiro.exception.NoSetSecurityManagerException;

/**
 * 路由认证过滤器
 * 
 * @author shf675
 * 
 */
public class RouteAuthFilter extends AuthenticationFilter implements ApplicationContextAware {

	private static Logger log = LoggerFactory.getLogger(RouteAuthFilter.class);
	
	// 声明一个静态变量保存
	private ApplicationContext applicationContext;

	// 默认的url登录路由参数名称
	private final static String DEFAULT_URL_ROUTE_PARAMETER_NAME = "_authenticator";

	// url中的登录路由参数名称。例如：http://127.0.0.1:8080/project/example?_authenticator=cas
	private String urlRouteParameterName = DEFAULT_URL_ROUTE_PARAMETER_NAME;

	// 默认的认证过滤器。设置此项后，如果url中未定义认证路由参数或未在定义的认证过滤器列表中找到目标过滤器则使用本过滤器
	private Filter defaultAuthFilter;
	
	// 默认的认证过滤器名称
	private String defaultAuthFilterName;

	// 认证过滤器列表
	private Map<String, Filter> filters;

	// 目标认证过滤器
	private Filter delegateAuthFilter;

	private RealmSecurityManager securityManager;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
		// 开始路由
		routing(request, response);

		// 动态切换realm
		dynamicSwitchRealm();

		return super.preHandle(request, response);
	}

	/**
	 * 当访问被拒绝时获取登录url做跳转
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		//判断是否为ajax请求，后续处理：前端ajaxError全局处理，提示重新登录。
		if(!isLoginRequest(request, response) && isAjaxRequest(request)){
			((HttpServletResponse)response).setStatus(4011);//http状态码401.1表示登录失败
			return false;
		}
		if (CasAuthFilter.class.isAssignableFrom(this.delegateAuthFilter.getClass())) {
			// 保存当前request并重定向到登录地址
			saveRequestAndRedirectToLogin(request, response);
		} else {
			redirectToLogin(request, response);
		}
		return false;
	}

	/**
	 * 开始路由
	 * 
	 * @return
	 */
	protected void routing(ServletRequest request, ServletResponse response) {
		Filter defaultAuthFilter = getDefaultAuthFilter();
		Map<String, Filter> filters = getFilters();
		if (!CollectionUtils.isEmpty(filters)) {
			String loginUrlRouteParameterValue = request.getParameter(getUrlRouteParameterName());
			if (StringUtils.hasText(loginUrlRouteParameterValue)) {
				this.delegateAuthFilter = filters.get(loginUrlRouteParameterValue);
				if (this.delegateAuthFilter == null) {
					log.warn("The parameter [{}] specifies that the authentication filter[{}] does not exist. Trying to use the default authentication filter.", getUrlRouteParameterName(), loginUrlRouteParameterValue);
					if (defaultAuthFilter == null) {
						throw new AuthenticationException("Default authentication filter is not set.");
					}
					this.delegateAuthFilter = defaultAuthFilter;
				}
			} else {
				if (defaultAuthFilter == null) {
					throw new AuthenticationException("When no authentication filter is specified in the URL parameter, the default authentication filter must be defined.");
				}
				this.delegateAuthFilter = defaultAuthFilter;
			}
		} else {
			if (defaultAuthFilter == null) {
				throw new AuthenticationException("When the authentication filter list is empty, the default authentication filter must be set.");
			}
			this.delegateAuthFilter = defaultAuthFilter;
		}
	}
	
	/**
	 * 重定向到登录url
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@Override
	protected void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
		String loginUrl = getLoginUrl(request, response);
		WebUtils.issueRedirect(request, response, loginUrl);
	}

	/**
	 * 获取登录url
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	protected String getLoginUrl(ServletRequest request, ServletResponse response) {
		String loginUrl = super.getLoginUrl();
		if (CasAuthFilter.class.isAssignableFrom(this.delegateAuthFilter.getClass())) {
			CasAuthFilter casAuthFilter = (CasAuthFilter) this.delegateAuthFilter;
			String casSpecialParameterValue = request.getParameter(casAuthFilter.getCasSpecialParameterName());
			String casServer = casAuthFilter.getRealCasServer(casSpecialParameterValue);
			String casServerPath = casAuthFilter.getRealCasServerPath(casSpecialParameterValue);
			String casClientService = casAuthFilter.getRealCasClientService(casSpecialParameterValue);
			String casClientLoginPath = casAuthFilter.getCasClientLoginPath();

			if (casServerPath.toLowerCase().startsWith("http://") || casServerPath.toLowerCase().startsWith("https://")) {
				loginUrl = new StringBuilder().append(casServerPath).append(casClientService).append(casClientLoginPath).toString();
			} else {
				loginUrl = new StringBuilder().append(casServer).append(casServerPath).append(casClientService).append(casClientLoginPath).toString();
			}
		}
		return loginUrl;
	}

	/**
	 * 动态切换realm
	 */
	protected void dynamicSwitchRealm() {
		if(securityManager == null){
			throw new NoSetSecurityManagerException("No set property securityManager...");
		}
		Realm realm = getCurrRealm();
		Collection<Realm> realms = securityManager.getRealms();
		if (realms == null) {
			if (realm != null) {
				securityManager.setRealm(realm);
			}
		} else {
			//检查当前realm是否和过滤器一致
			if (realm != null && !realms.contains(realm)) {
				securityManager.setRealm(realm);
			}
		}
	}
	
	/**
	 * 获取当前realm
	 */
	protected Realm getCurrRealm() {
		Realm realm = null;
		if (this.delegateAuthFilter instanceof FormAuthFilter) {
			realm = ((FormAuthFilter) this.delegateAuthFilter).getRealm();
		} else if (this.delegateAuthFilter instanceof CasAuthFilter) {
			realm = ((CasAuthFilter) this.delegateAuthFilter).getRealm();
		}
		return realm;
	}
	
	/**
	 * isAjaxRequest:判断请求是否为Ajax请求
	 * 
	 * @param request
	 * @return
	 */
	private boolean isAjaxRequest(ServletRequest request) {
		String header = ((HttpServletRequest)request).getHeader("X-Requested-With");
		boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(header) ? true : false;
		return isAjax;
	}

	public Filter getDefaultAuthFilter() {
		if(defaultAuthFilter == null){
			//如果未设置defaultAuthFilter，可根据defaultAuthFilterName取
			defaultAuthFilter = applicationContext.getBean(getDefaultAuthFilterName(), Filter.class);
		}
		return defaultAuthFilter;
	}

	public void setDefaultAuthFilter(Filter defaultAuthFilter) {
		this.defaultAuthFilter = defaultAuthFilter;
	}

	public Map<String, Filter> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, Filter> filters) {
		this.filters = filters;
	}

	public String getUrlRouteParameterName() {
		return urlRouteParameterName;
	}

	public void setUrlRouteParameterName(String urlRouteParameterName) {
		this.urlRouteParameterName = urlRouteParameterName;
	}

	public RealmSecurityManager getSecurityManager() {
		return securityManager;
	}

	public void setSecurityManager(RealmSecurityManager securityManager) {
		this.securityManager = securityManager;
	}

	public String getDefaultAuthFilterName() {
		return defaultAuthFilterName;
	}

	public void setDefaultAuthFilterName(String defaultAuthFilterName) {
		this.defaultAuthFilterName = defaultAuthFilterName;
	}

}
