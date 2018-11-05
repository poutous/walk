package org.walkframework.shiro.web.filter.authc;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.shiro.authc.principal.BasePrincipal;
import org.walkframework.shiro.authc.token.BaseToken;
import org.walkframework.shiro.authc.token.CasToken;
import org.walkframework.shiro.realm.BaseCasRealm;

/**
 * 登出过滤器
 * 
 * @author shf675
 * 
 */
public class BaseLogoutFilter extends LogoutFilter {
	private static Logger log = LoggerFactory.getLogger(BaseLogoutFilter.class);
	/**
	 * CAS方式登录的退出地址做特殊处理
	 * 
	 * @param request
	 * @param response
	 * @param subject
	 * @return
	 */
	@Override
	protected String getRedirectUrl(ServletRequest request, ServletResponse response, Subject subject) {
		String logoutUrl = super.getRedirectUrl(request, response, subject);
		return getLogoutUrlIfCas(logoutUrl, subject);
	}
	
	/**
	 * 如果是CAS 获取登出url 
	 * 
	 * @param subject
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	protected String getLogoutUrlIfCas(String logoutUrl, Subject subject){
		// CAS方式登录的退出地址做特殊处理
		BasePrincipal principal = (BasePrincipal) subject.getPrincipal();
		if(principal == null){
			return logoutUrl;
		}
		BaseToken token = principal.getToken();
		if (token instanceof CasToken) {
			CasToken casToken = (CasToken) token;
			BaseCasRealm casRealm = getCasRealm();
			if (casRealm != null) {
				String casServerPath = casRealm.getCasServerPath(casToken);
				String casClientService = casRealm.getCasClientService(casToken);
				String casSpecialParameterName = casRealm.getCasSpecialParameterName();
				String casSpecialParameterValue = casToken.getSpecialParameter();
				
				StringBuilder casLogoutUrl = new StringBuilder();
				String[] paths = casServerPath.split("\\?");
				casLogoutUrl.append(paths[0].substring(0, paths[0].lastIndexOf("/")) + "/logout?" + paths[1]);
				
				StringBuilder fullCasClientService = new StringBuilder();
				fullCasClientService.append(casClientService);
				
				if(StringUtils.hasText(casSpecialParameterValue)){
					if (casClientService.indexOf("?") > -1) {
						fullCasClientService.append("&");
					} else {
						fullCasClientService.append("?");
					}
					fullCasClientService.append(casSpecialParameterName).append("=").append(casSpecialParameterValue);
				}
				
				try {
					casLogoutUrl.append(URLEncoder.encode(fullCasClientService.toString(), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					log.error(e.getMessage(), e);
				}
				return casLogoutUrl.toString();
			}
		}
		return logoutUrl;
	}

	/**
	 * 获取CAS REALM
	 * 
	 * @return
	 */
	public BaseCasRealm getCasRealm() {
		SecurityManager securityManager = SecurityUtils.getSecurityManager();
		if (securityManager instanceof RealmSecurityManager) {
			Collection<Realm> realms = ((RealmSecurityManager) securityManager).getRealms();
			if (realms != null) {
				for (Realm realm : realms) {
					if (realm instanceof BaseCasRealm) {
						return (BaseCasRealm) realm;
					}
				}
			}
		}
		return null;
	}
}
