package org.walkframework.base.system.rule;

import java.util.Map;


/**
 * 规则验服务
 * 
 * @author shf675
 *
 */
public interface RuleValidService {
	
	/**
	 * 校验所有规则
	 * 
	 * 所有规则校验通过返回null
	 * 不通过返回错误消息集合
	 * 
	 * @param sources
	 * @return
	 */
	Map<Class<? extends RuleValidator>, String> validRules(Object sources);
	
}
