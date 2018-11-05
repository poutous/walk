package org.walkframework.shiro.authc.token;

import org.walkframework.shiro.util.password.PasswordEncryptor;
import org.walkframework.shiro.util.password.DefaultPasswordEncryptor;

/**
 * 基于用户名密码的token
 * 
 * @author shf675
 * 
 */
public class FormToken extends BaseToken {
	
	private static final long serialVersionUID = -3119169294607218827L;

	private String username;

	private String password;

	// 盐值
	private String salt;
	
	//是否加盐值
	private boolean useSalt = true;
	
	//密码加密器，默认使用shiro自带的Sha256Hash方式加密
	private PasswordEncryptor passwordEncryptor = new DefaultPasswordEncryptor();
	
	public FormToken(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public String getCredentials() {
		if(useSalt){
			return getPasswordEncryptor().encrypt(password, salt == null ? username : salt);
		}
		return getPasswordEncryptor().encrypt(password);
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getSalt() {
		return salt;
	}

	public boolean isUseSalt() {
		return useSalt;
	}

	public void setUseSalt(boolean useSalt) {
		this.useSalt = useSalt;
	}

	public PasswordEncryptor getPasswordEncryptor() {
		return passwordEncryptor;
	}

	public void setPasswordEncryptor(PasswordEncryptor passwordEncryptor) {
		this.passwordEncryptor = passwordEncryptor;
	}

}
