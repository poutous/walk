package org.walkframework.shiro.authc.principal;

import java.io.Serializable;

import org.walkframework.shiro.authc.token.BaseToken;
import org.walkframework.shiro.authc.token.CasToken;

/**
 * 所有用户身份信息继承此基类
 * 
 * @author shf675
 */
public class BasePrincipal implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private BaseToken token;
	
	private String userId;
	
	private String userName;
	
	private boolean fromCas;
	
	public BasePrincipal(BaseToken token){
		this.token = token;
		setFromCas(token instanceof CasToken);
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public BaseToken getToken() {
		return token;
	}
	
	public boolean isFromCas() {
		return fromCas;
	}

	public void setFromCas(boolean fromCas) {
		this.fromCas = fromCas;
	}
}
