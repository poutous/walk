package org.walkframework.base.system.rule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 密码规则验服务实现类
 * 
 * @author shf675
 * 
 */
public class DefaultRuleValidService implements RuleValidService {

	// 同时执行全部校验器
	private boolean together = true;

	private List<RuleValidator> ruleValidators;

	public List<RuleValidator> getRuleValidators() {
		return ruleValidators;
	}

	public void setRuleValidators(List<RuleValidator> ruleValidators) {
		this.ruleValidators = ruleValidators;
	}

	/**
	 * 校验所有规则
	 * 
	 * 所有规则校验通过返回null 不通过返回错误消息集合
	 * 
	 * @param sources
	 * @return
	 */
	@Override
	public Map<Class<? extends RuleValidator>, String> validRules(Object source) {
		Map<Class<? extends RuleValidator>, String> errorMessages = new HashMap<Class<? extends RuleValidator>, String>();
		if (ruleValidators != null && ruleValidators.size() > 0) {
			for (RuleValidator ruleValidator : ruleValidators) {
				if (ruleValidator != null && !ruleValidator.valid(source)) {
					errorMessages.put(ruleValidator.getClass(), ruleValidator.getErrorMessage());
					if (!together) {
						break;
					}
				}
			}
		}
		return errorMessages.isEmpty() ? null : errorMessages;
	}

	public boolean isTogether() {
		return together;
	}

	public void setTogether(boolean together) {
		this.together = together;
	}
}
