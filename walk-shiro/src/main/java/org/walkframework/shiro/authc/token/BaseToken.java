package org.walkframework.shiro.authc.token;

import org.apache.shiro.authc.HostAuthenticationToken;
import org.apache.shiro.authc.RememberMeAuthenticationToken;
import org.walkframework.shiro.authc.principal.BasePrincipal;

/**
 * token 基类
 * 
 * 自实现token需继承本类
 * 
 * @author shf675
 *
 */
public abstract class BaseToken implements HostAuthenticationToken, RememberMeAuthenticationToken {
	private static final long serialVersionUID = 1L;

	private BasePrincipal principal;

	private String host;
	
	private boolean isRememberMe;

	@Override
	public BasePrincipal getPrincipal() {
		return principal;
	}

	@Override
	public boolean isRememberMe() {
		return isRememberMe;
	}
	
	@Override
	public String getHost() {
		return this.host;
	}
	
	public void setPrincipal(BasePrincipal principal) {
		this.principal = principal;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	public void setRememberMe(boolean isRememberMe) {
		this.isRememberMe = isRememberMe;
	}

	public void clear() {
        this.principal = null;
        this.host = null;
        this.isRememberMe = false;
    }
	
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append(" - ");
        sb.append(principal == null ? "null":principal.getUserId());
        sb.append(", rememberMe=").append(this.isRememberMe);
        if (host != null) {
            sb.append(" (").append(host).append(")");
        }
        return sb.toString();
    }
}
