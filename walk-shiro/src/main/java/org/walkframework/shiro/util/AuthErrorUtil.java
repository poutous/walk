package org.walkframework.shiro.util;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.util.StringUtils;
import org.walkframework.shiro.exception.IdentifyingCodeErrorException;

/**
 * @author shf675
 *
 */
public class AuthErrorUtil {
	
	public static String getErrorInfo(AuthenticationException ae) {
		String error = "";
		if (ae instanceof UnknownAccountException) {
			error = "用户名/密码错误";
		} else if (ae instanceof IncorrectCredentialsException) {
			error = "用户名/密码错误";
		} else if (ae instanceof IdentifyingCodeErrorException) {
			error = "验证码错误";
		} else if (ae != null) {
			error = StringUtils.hasText(ae.getMessage()) ? ae.getMessage() : "未知错误";
		}
		return error;
	}
}
