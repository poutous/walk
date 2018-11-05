package org.walkframework.base.system.rule;

/**
 * 规则验证器
 * 
 * @author shf675
 *
 */
public interface RuleValidator {
	
	/**
	 * 校验
	 * @param source
	 * @return
	 */
	boolean valid(Object source);
	
	/**
	 * 获取错误消息
	 * 
	 * @return
	 */
	String getErrorMessage();
}
