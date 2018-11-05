package org.walkframework.shiro.util.password;

import org.apache.shiro.crypto.hash.Sha256Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * shiro自带的Sha256Hash方式加密
 * 
 * @author shf675
 *
 */
public class DefaultPasswordEncryptor implements PasswordEncryptor {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultPasswordEncryptor.class);

	/**
     * 密码加密
     * 
     * @param password
     * @return
     */
	@Override
	public String encrypt(String password) {
		try {
			return new Sha256Hash(password).toString();
		} catch (Exception e) {
			logger.error("Error encode password, use plain text.", e);
		}
		return password;
	}

	/**
     * 密码加密，带盐值
     * 
     * @param password
     * @param salt
     * @return
     */
	@Override
	public String encrypt(String password, String salt) {
		try {
			return new Sha256Hash(password, salt).toString();
		} catch (Exception e) {
			logger.error("Error encode password, use plain text.", e);
		}
		return password;
	}
}
