/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.walkframework.shiro.realm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cas.CasAuthenticationException;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.subject.WebSubject;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.constant.CasConstants;
import org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.Saml11TicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.shiro.authc.token.BaseToken;
import org.walkframework.shiro.authc.token.CasToken;
import org.walkframework.shiro.session.BaseSessionIdGenerator;
import org.walkframework.shiro.web.filter.authc.CasAuthFilter;

/**
 * This realm implementation acts as a CAS client to a CAS server for
 * authentication and basic authorization. <p/> This realm functions by
 * inspecting a submitted {@link org.apache.shiro.cas.CasToken CasToken} (which
 * essentially wraps a CAS service ticket) and validates it against the CAS
 * server using a configured CAS
 * {@link org.jasig.cas.client.validation.TicketValidator TicketValidator}.
 * <p/> The {@link #getValidationProtocol() validationProtocol} is {@code CAS}
 * by default, which indicates that a a
 * {@link org.jasig.cas.client.validation.Cas20ServiceTicketValidator Cas20ServiceTicketValidator}
 * will be used for ticket validation. You can alternatively set or
 * {@link org.jasig.cas.client.validation.Saml11TicketValidator Saml11TicketValidator}
 * of CAS client. It is based on {@link AuthorizingRealm AuthorizingRealm} for
 * both authentication and authorization. User id and attributes are retrieved
 * from the CAS service ticket validation response during authentication phase.
 * Roles and permissions are computed during authorization phase (according to
 * the attributes previously retrieved).
 * 
 * 重写shiro自带CasRealm
 * 
 * @author modify by shf675
 * 
 * @since 1.2
 */
public abstract class BaseCasRealm extends AbstractRealm {

	private static Logger log = LoggerFactory.getLogger(BaseCasRealm.class);

	public static final String DEFAULT_REMEMBER_ME_ATTRIBUTE_NAME = "longTermAuthenticationRequestTokenUsed";

	public static final String DEFAULT_CAS_SERVER_ROLES_ATTRIBUTE_NAME = "roles";

	public static final String DEFAULT_CAS_SERVER_PERMISSIONS_ATTRIBUTE_NAME = "permissions";

	public static final String DEFAULT_VALIDATION_PROTOCOL = "CAS";

	/*
	 * CAS protocol to use for ticket validation : CAS (default) or SAML : - CAS
	 * protocol can be used with CAS server version < 3.1 : in this case, no
	 * user attributes can be retrieved from the CAS ticket validation response
	 * (except if there are some customizations on CAS server side) - SAML
	 * protocol can be used with CAS server version >= 3.1 : in this case, user
	 * attributes can be extracted from the CAS ticket validation response
	 */
	private String validationProtocol = DEFAULT_VALIDATION_PROTOCOL;

	// default name of the CAS attribute for remember me authentication
	// (CAS3.4.10+)
	private String rememberMeAttributeName = DEFAULT_REMEMBER_ME_ATTRIBUTE_NAME;

	// this class from the CAS client is used to validate a service ticket on
	// CAS server
	private TicketValidator ticketValidator;

	/** Start：自定义的一些参数**************************************************************************** */
	private CasAuthFilter casAuthFilter;

	// CAS服务端返回的principal中角色属性名
	private String casServerRolesAttributeName = DEFAULT_CAS_SERVER_ROLES_ATTRIBUTE_NAME;

	// CAS服务端返回的principal中权限属性名
	private String casServerPermissionsAttributeName = DEFAULT_CAS_SERVER_PERMISSIONS_ATTRIBUTE_NAME;

	/** End：自定义的一些参数**************************************************************************** */

	// 编码
	private String encoding;

	// 是否加密标识
	private boolean encode;

	// 接入程序名称
	private String appId;

	// 加密密钥
	private String appKey;

	/**
	 * Authenticates a user and retrieves its information.
	 * 
	 * @param token
	 *            the authentication token
	 * @throws AuthenticationException
	 *             if there is an error during authentication.
	 */
	@Override
	@SuppressWarnings( { "unchecked", "serial", "deprecation" })
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

		CasToken casToken = (CasToken) token;
		if (token == null) {
			return null;
		}

		String ticket = (String) casToken.getCredentials();
		if (!StringUtils.hasText(ticket)) {
			return null;
		}
		String casServer = getCasServer(casToken);
		TicketValidator ticketValidator = ensureTicketValidator(casServer);

		// 校验传输加密
		if (isEncode() && ticketValidator instanceof AbstractUrlBasedTicketValidator) {
			AbstractUrlBasedTicketValidator validator = ((AbstractUrlBasedTicketValidator) ticketValidator);
			if (StringUtils.hasText(encoding)) {
				validator.setEncoding(encoding);
			}
			validator.setCustomParameters(new HashMap<String, String>() {
				{
					put(CasConstants.ENCODE_NAME, String.valueOf(isEncode()));
					put(CasConstants.APP_ID_NAME, getAppId());
					put(CasConstants.APP_KEY_NAME, getAppKey());
				}
			});
		}

		try {
			// contact CAS server to validate service ticket
			String fullCasClientService = new StringBuilder().append(getCasClientService(casToken)).append(getCasClientLoginPath()).toString();
			Assertion casAssertion = ticketValidator.validate(ticket, fullCasClientService);
			// get principal, user id and attributes
			AttributePrincipal casPrincipal = casAssertion.getPrincipal();
			String userId = casPrincipal.getName();
			if (log.isDebugEnabled()) {
				log.debug("Validate ticket : {} in CAS server : {} to retrieve user : {}", ticket, casServer, userId);
			}

			Map<String, Object> attributes = casPrincipal.getAttributes();
			// refresh authentication token (user id + remember me)
			String rememberMeAttributeName = getRememberMeAttributeName();
			String rememberMeStringValue = (String) attributes.get(rememberMeAttributeName);
			boolean isRemembered = rememberMeStringValue != null && Boolean.parseBoolean(rememberMeStringValue);
			if (isRemembered) {
				casToken.setRememberMe(true);
			}
			
			//设置当前用户名
			setUserName(casPrincipal);
			return doGetUserAuthenticationInfo(casToken, casPrincipal);
		} catch (Throwable e) {
			if (e instanceof AuthenticationException) {
				throw (AuthenticationException) e;
			}
			String msg = "Unable to validate ticket [" + ticket + "]. message:" + e.getMessage();
			log.error(msg, e);
			throw new CasAuthenticationException(msg, e);
		}
	}
	
	/**
	 * 设置当前用户名，默认设置主账号，子类可按需要重写
	 * 
	 * @param casPrincipal
	 */
	protected void setUserName(AttributePrincipal casPrincipal){
		ServletRequest request = ((WebSubject)SecurityUtils.getSubject()).getServletRequest();
		request.setAttribute(BaseSessionIdGenerator.USER_NAME_KEY, casPrincipal.getName());
	}

	/**
	 * 设置自用户认证信息 需子类实现
	 * 
	 * @param token
	 * @return
	 * @throws AuthenticationException
	 */
	protected abstract AuthenticationInfo doGetUserAuthenticationInfo(BaseToken token, AttributePrincipal casPrincipal);

	/**
	 * Retrieves the AuthorizationInfo for the given principals (the CAS
	 * previously authenticated user : id + attributes).
	 * 
	 * @param principals
	 *            the primary identifying principals of the AuthorizationInfo
	 *            that should be retrieved.
	 * @return the AuthorizationInfo associated with this principals.
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		SimplePrincipalCollection principalCollection = (SimplePrincipalCollection) principals;
		SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
		try {
			List<Object> listPrincipals = principalCollection.asList();
			if (listPrincipals.size() > 1) {
				Map<String, String> attributes = (Map<String, String>) listPrincipals.get(1);

				// add roles
				if (StringUtils.hasText(casServerRolesAttributeName)) {
					String roles = attributes.get(casServerRolesAttributeName);
					if (StringUtils.hasText(roles)) {
						addRoles(simpleAuthorizationInfo, split(roles));
					}
				}

				// add permissions
				if (StringUtils.hasText(casServerPermissionsAttributeName)) {
					String permissions = attributes.get(casServerPermissionsAttributeName);
					if (StringUtils.hasText(permissions)) {
						addPermissions(simpleAuthorizationInfo, split(permissions));
					}
				}
			}
			return doGetUserAuthorizationInfo(principals, simpleAuthorizationInfo);
		} catch (Throwable e) {
			if (e instanceof AuthorizationException) {
				throw (AuthenticationException) e;
			}
			String msg = "Get AuthorizationInfo error. message:" + e.getMessage();
			log.error(msg, e);
			throw new AuthorizationException(msg, e);
		}
	}

	/**
	 * 获取用户授权信息 需子类实现
	 * 
	 * @param principals
	 * @param authorizationInfo
	 * @return
	 */
	protected abstract AuthorizationInfo doGetUserAuthorizationInfo(PrincipalCollection principals, SimpleAuthorizationInfo authorizationInfo);

	/**
	 * Split a string into a list of not empty and trimmed strings, delimiter is
	 * a comma.
	 * 
	 * @param s
	 *            the input string
	 * @return the list of not empty and trimmed strings
	 */
	private List<String> split(String s) {
		List<String> list = new ArrayList<String>();
		String[] elements = StringUtils.split(s, ',');
		if (elements != null && elements.length > 0) {
			for (String element : elements) {
				if (StringUtils.hasText(element)) {
					list.add(element.trim());
				}
			}
		}
		return list;
	}

	/**
	 * Add roles to the simple authorization info.
	 * 
	 * @param simpleAuthorizationInfo
	 * @param roles
	 *            the list of roles to add
	 */
	private void addRoles(SimpleAuthorizationInfo simpleAuthorizationInfo, List<String> roles) {
		for (String role : roles) {
			simpleAuthorizationInfo.addRole(role);
		}
	}

	/**
	 * Add permissions to the simple authorization info.
	 * 
	 * @param simpleAuthorizationInfo
	 * @param permissions
	 *            the list of permissions to add
	 */
	private void addPermissions(SimpleAuthorizationInfo simpleAuthorizationInfo, List<String> permissions) {
		for (String permission : permissions) {
			simpleAuthorizationInfo.addStringPermission(permission);
		}
	}

	protected TicketValidator ensureTicketValidator(String casServer) {
		if (this.ticketValidator == null) {
			this.ticketValidator = createTicketValidator(casServer);
		}
		return this.ticketValidator;
	}

	protected TicketValidator createTicketValidator(String casServer) {
		if ("saml".equalsIgnoreCase(getValidationProtocol())) {
			return new Saml11TicketValidator(casServer);
		}
		return new Cas20ServiceTicketValidator(casServer);
	}

	public String getValidationProtocol() {
		return validationProtocol;
	}

	public void setValidationProtocol(String validationProtocol) {
		this.validationProtocol = validationProtocol;
	}

	public String getRememberMeAttributeName() {
		return rememberMeAttributeName;
	}

	public void setRememberMeAttributeName(String rememberMeAttributeName) {
		this.rememberMeAttributeName = rememberMeAttributeName;
	}

	public void setCasServerRolesAttributeName(String casServerRolesAttributeName) {
		this.casServerRolesAttributeName = casServerRolesAttributeName;
	}

	public void setCasServerPermissionsAttributeName(String casServerPermissionsAttributeName) {
		this.casServerPermissionsAttributeName = casServerPermissionsAttributeName;
	}

	public String getCasServerRolesAttributeName() {
		return casServerRolesAttributeName;
	}

	public String getCasServerPermissionsAttributeName() {
		return casServerPermissionsAttributeName;
	}

	public CasAuthFilter getCasAuthFilter() {
		return casAuthFilter;
	}

	public void setCasAuthFilter(CasAuthFilter casAuthFilter) {
		this.casAuthFilter = casAuthFilter;
	}

	/**
	 * 获取CAS服务端地址
	 * 
	 * @param casToken
	 * @return
	 */
	public String getCasServer(CasToken casToken) {
		return getCasAuthFilter().getRealCasServer(casToken.getSpecialParameter());
	}

	/**
	 * 获取CAS服务端登录地址
	 * 
	 * @param casToken
	 * @return
	 */
	public String getCasServerPath(CasToken casToken) {
		return getCasAuthFilter().getRealCasServerPath(casToken.getSpecialParameter());
	}

	/**
	 * 获取CAS服务端地址
	 * 
	 * @param casToken
	 * @return
	 */
	public String getCasClientService(CasToken casToken) {
		return getCasAuthFilter().getRealCasClientService(casToken.getSpecialParameter());
	}

	/**
	 * 获取CAS客户端登录路径
	 * 
	 * @return
	 */
	public String getCasClientLoginPath() {
		return getCasAuthFilter().getCasClientLoginPath();
	}

	/**
	 * 获取url中指定的casServer参数名
	 * 
	 * @return
	 */
	public String getCasSpecialParameterName() {
		return getCasAuthFilter().getCasSpecialParameterName();
	}

	public boolean isEncode() {
		return encode;
	}

	public void setEncode(boolean encode) {
		this.encode = encode;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
}
