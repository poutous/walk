package org.walkframework.shiro.util;

/**
 * 签名检查接口
 * 
 * @author shf675
 *
 */
public interface ISignChecker {
	
	public <T> T check(String appId, String timestamp, String sign);
}
