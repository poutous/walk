package org.walkframework.shiro.web.filter.authc;

import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.cas.CasFilter;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.walkframework.shiro.authc.token.CasToken;
import org.walkframework.shiro.realm.BaseCasRealm;
import org.walkframework.shiro.util.UrlParser;

/**
 * 自定义cas认证过滤器
 * 
 * 支持设置ticket参数名字
 * 
 * @author shf675
 * 
 */
@SuppressWarnings("deprecation")
public class CasAuthFilter extends CasFilter implements InitializingBean {

	private static Logger log = LoggerFactory.getLogger(CasAuthFilter.class);

	private static final String DEFAULT_TICKET_PARAMETER = "ticket";

	private static final String DEFAULT_CAS_SPECIAL_PARAMETER_NAME = "_specCas";

	private static final String DEFAULT_CAS_SERVER_PATH = "/login?service=";

	private static final String DEFAULT_CAS_CLIENT_LOGIN_PATH = "/caslogin";

	// url中指定的casServer参数名
	private String casSpecialParameterName = DEFAULT_CAS_SPECIAL_PARAMETER_NAME;

	// 默认的CAS服务端地址
	private String defaultCasServer;

	// 默认的CAS服务端登录地址
	private String defaultCasServerPath = DEFAULT_CAS_SERVER_PATH;

	// 默认的CAS客户端地址
	private String defaultCasClientService;

	// CAS客户端登录路径
	private String casClientLoginPath = DEFAULT_CAS_CLIENT_LOGIN_PATH;

	// CAS服务端地址列表
	private Map<String, String> casServers;

	// CAS服务端登录地址列表
	private Map<String, String> casServerPaths;

	// CAS客户端地址列表
	private Map<String, String> casClientServices;

	// cas客户端票据参数名称
	private String ticketParameterName = DEFAULT_TICKET_PARAMETER;
	
	private BaseCasRealm realm;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if(realm != null){
			realm.setCasAuthFilter(this);
		}
	}
	
	@Override
	protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
		//查找特殊参数
		String specialParameter = null;
		SavedRequest savedRequest = WebUtils.getSavedRequest(request);
		if(savedRequest != null){
			String requestUrl = savedRequest.getRequestUrl();
			if (requestUrl != null) {
				specialParameter = UrlParser.getValueByParam(requestUrl, getCasSpecialParameterName());
			}
		}

		//ticket
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String ticket = httpRequest.getParameter(getTicketParameterName());
		return new CasToken(ticket, specialParameter);
	}
	
	@Override
	protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
		//登录前设置realm
		((RealmSecurityManager)SecurityUtils.getSecurityManager()).setRealm(getRealm());
				
		AuthenticationToken token = createToken(request, response);
        if (token == null) {
            String msg = "createToken method implementation returned null. A valid non-null AuthenticationToken " +
                    "must be created in order to execute a login attempt.";
            throw new IllegalStateException(msg);
        }
        try {
            Subject subject = getSubject(request, response);
            
            //解决会话固定攻击问题
			AuthFilterHelper.resolveSessionFixation(subject, token);
            
            return onLoginSuccess(token, subject, request, response);
        } catch (AuthenticationException e) {
            return onLoginFailure(token, e, request, response);
        }
	}

	/**
	 * 获取CAS服务端地址
	 * 
	 * @param casSpecialParameterValue
	 */
	public String getRealCasServer(String casSpecialParameterValue) {
		String casServer = null;
		if (!CollectionUtils.isEmpty(this.casServers)) {
			if (StringUtils.hasText(casSpecialParameterValue)) {
				casServer = this.casServers.get(casSpecialParameterValue);
				if (!StringUtils.hasText(casServer)) {
					log.warn("The parameter [{}] specifies that the cas server[{}] does not exist. Trying to use the default cas server[{}].", this.casSpecialParameterName, casSpecialParameterValue, this.defaultCasServer);
					if (!StringUtils.hasText(this.defaultCasServer)) {
						throw new AuthenticationException("Default cas server is not set.");
					}
					casServer = this.defaultCasServer;
				}
			} else {
				if (!StringUtils.hasText(this.defaultCasServer)) {
					throw new AuthenticationException("Default cas server must be set when the request parameter " + this.casSpecialParameterName + "is not specified.");
				}
				casServer = this.defaultCasServer;
			}

		} else {
			if (!StringUtils.hasText(this.defaultCasServer)) {
				throw new AuthenticationException("When the property casServers is not set, the property defaultCasServer must be set to value.");
			}
			casServer = this.defaultCasServer;
		}
		return casServer;
	}

	/**
	 * 获取CAS服务端登录地址
	 * 
	 * @param casSpecialParameterValue
	 */
	public String getRealCasServerPath(String casSpecialParameterValue) {
		String casServerPath = null;
		if (!CollectionUtils.isEmpty(this.casServerPaths)) {
			if (StringUtils.hasText(casSpecialParameterValue)) {
				casServerPath = this.casServerPaths.get(casSpecialParameterValue);
				if (!StringUtils.hasText(casServerPath)) {
					log.warn("The parameter [{}] specifies that the cas server path[{}] does not exist. Trying to use the default cas server path[{}].", this.casSpecialParameterName, casSpecialParameterValue, this.defaultCasServerPath);
					if (!StringUtils.hasText(this.defaultCasServerPath)) {
						throw new AuthenticationException("Default cas server path is not set.");
					}
					casServerPath = this.defaultCasServerPath;
				}
			} else {
				if (!StringUtils.hasText(this.defaultCasServerPath)) {
					throw new AuthenticationException("Default cas server path must be set when the request parameter " + this.casSpecialParameterName + "is not specified.");
				}
				casServerPath = this.defaultCasServerPath;
			}

		} else {
			if (!StringUtils.hasText(this.defaultCasServerPath)) {
				throw new AuthenticationException("When the property casServerPaths is not set, the property defaultCasServerPath must be set to value.");
			}
			casServerPath = this.defaultCasServerPath;
		}
		return casServerPath;
	}

	/**
	 * 获取CAS客户端地址
	 * 
	 * @param casSpecialParameterValue
	 */
	public String getRealCasClientService(String casSpecialParameterValue) {
		String casClientService = null;
		if (!CollectionUtils.isEmpty(this.casClientServices)) {
			if (StringUtils.hasText(casSpecialParameterValue)) {
				casClientService = this.casClientServices.get(casSpecialParameterValue);
				if (!StringUtils.hasText(casClientService)) {
					log.warn("The parameter [{}] specifies that the cas client service[{}] does not exist. Trying to use the default cas client service[{}].", this.casSpecialParameterName, casSpecialParameterValue, this.defaultCasClientService);
					if (!StringUtils.hasText(this.defaultCasClientService)) {
						throw new AuthenticationException("Default cas client service is not set.");
					}
					casClientService = this.defaultCasClientService;
				}
			} else {
				if (!StringUtils.hasText(this.defaultCasClientService)) {
					throw new AuthenticationException("Default cas client service must be set when the request parameter " + this.casSpecialParameterName + "is not specified.");
				}
				casClientService = this.defaultCasClientService;
			}

		} else {
			if (!StringUtils.hasText(this.defaultCasClientService)) {
				throw new AuthenticationException("When the property casClientServices is not set, the property defaultCasClientService must be set to value.");
			}
			casClientService = this.defaultCasClientService;
		}
		return casClientService;
	}

	/**
	 * 登录失败后执行动作
	 * 
	 * 重写父类该方法，打印错误日志
	 * 
	 * @param token
	 * @param ae
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException ae, ServletRequest request, ServletResponse response) {
		log.error("Authentication exception", ae);
		return super.onLoginFailure(token, ae, request, response);
	}
	
	public String getDefaultCasServer() {
		return defaultCasServer;
	}

	public void setDefaultCasServer(String defaultCasServer) {
		this.defaultCasServer = defaultCasServer;
	}

	public String getDefaultCasClientService() {
		return defaultCasClientService;
	}

	public void setDefaultCasClientService(String defaultCasClientService) {
		this.defaultCasClientService = defaultCasClientService;
	}

	public Map<String, String> getCasServers() {
		return casServers;
	}

	public void setCasServers(Map<String, String> casServers) {
		this.casServers = casServers;
	}

	public Map<String, String> getCasClientServices() {
		return casClientServices;
	}

	public void setCasClientServices(Map<String, String> casClientServices) {
		this.casClientServices = casClientServices;
	}

	public void setTicketParameterName(String ticketParameterName) {
		this.ticketParameterName = ticketParameterName;
	}

	public String getTicketParameterName() {
		return ticketParameterName;
	}

	public String getCasSpecialParameterName() {
		return casSpecialParameterName;
	}

	public void setCasSpecialParameterName(String casSpecialParameterName) {
		this.casSpecialParameterName = casSpecialParameterName;
	}

	public String getDefaultCasServerPath() {
		return defaultCasServerPath;
	}

	public void setDefaultCasServerPath(String defaultCasServerPath) {
		this.defaultCasServerPath = defaultCasServerPath;
	}

	public Map<String, String> getCasServerPaths() {
		return casServerPaths;
	}

	public void setCasServerPaths(Map<String, String> casServerPaths) {
		this.casServerPaths = casServerPaths;
	}

	public String getCasClientLoginPath() {
		return casClientLoginPath;
	}

	public void setCasClientLoginPath(String casClientLoginPath) {
		this.casClientLoginPath = casClientLoginPath;
	}
	
	public BaseCasRealm getRealm() {
		return realm;
	}

	public void setRealm(BaseCasRealm realm) {
		this.realm = realm;
	}
}
