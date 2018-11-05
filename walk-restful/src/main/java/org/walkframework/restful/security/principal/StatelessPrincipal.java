package org.walkframework.restful.security.principal;

import org.walkframework.data.util.DataMap;
import org.walkframework.data.util.IData;
import org.walkframework.shiro.authc.principal.BasePrincipal;
import org.walkframework.shiro.authc.token.BaseToken;

/**
 * 程序身份信息
 */
public class StatelessPrincipal extends BasePrincipal {
	private static final long serialVersionUID = 1L;

	 /**	
	 *程序ID	
	 */	
	private String appId;

	 /**	
	 *程序名称	
	 */	
	private String appName;

	 /**	
	 *程序加密key	
	 */	
	private String appKey;

	 /**	
	 *程序状态。1：正常；0：无效；4：锁定	
	 */	
	private String appState;

	 /**	
	 *是否校验签名
	 */	
	private boolean signCheck;

	 /**	
	 * 是否校验url权限
	 */	
	private boolean urlCheck;

	/**
	 * 额外的属性信息
	 */
	private IData<String, Object> attributes = new DataMap<String, Object>();

	public IData<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(IData<String, Object> attributes) {
		this.attributes = attributes;
	}

	public StatelessPrincipal(BaseToken token) {
		super(token);
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getAppState() {
		return appState;
	}

	public void setAppState(String appState) {
		this.appState = appState;
	}

	public boolean isSignCheck() {
		return signCheck;
	}

	public void setSignCheck(boolean signCheck) {
		this.signCheck = signCheck;
	}

	public boolean isUrlCheck() {
		return urlCheck;
	}

	public void setUrlCheck(boolean urlCheck) {
		this.urlCheck = urlCheck;
	}

}
