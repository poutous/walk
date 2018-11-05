package org.walkframework.shiro.authc.token;


/**
 * 基于cas的token
 * 
 * @author shf675
 * 
 */
public class CasToken extends BaseToken {

	private static final long serialVersionUID = 8587329689973009598L;

	private String ticket;
	
	private String specialParameter;
	
	public CasToken(String ticket) {
		this.ticket = ticket;
	}
	
	public CasToken(String ticket, String specialParameter) {
		this.ticket = ticket;
		this.specialParameter = specialParameter;
	}
	
	public String getSpecialParameter() {
		return specialParameter;
	}

	@Override
	public String getCredentials() {
		return ticket;
	}
}
