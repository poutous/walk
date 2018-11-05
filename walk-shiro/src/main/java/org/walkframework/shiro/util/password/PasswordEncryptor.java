package org.walkframework.shiro.util.password;

import java.io.Serializable;

/**
 * 密码生成器
 * 
 * @author shf675
 *
 */
public interface PasswordEncryptor extends Serializable{

    /**
     * 密码加密
     * 
     * @param password
     * @return
     */
    String encrypt(String password);
    
    /**
     * 密码加密，带盐值
     * 
     * @param password
     * @param salt
     * @return
     */
    String encrypt(String password, String salt);
}
