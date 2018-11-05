package org.walkframework.console.mvc.service.base;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.walkframework.base.mvc.service.base.BaseService;

import com.alibaba.fastjson.JSON;

/**
 * console service基类
 * 
 * @author shf675
 * 
 */
public class BaseConsoleService extends BaseService {

	/**
	 * 尝试获取json串
	 * 
	 * @param value
	 * @return
	 */
	protected String toJSONString(Object value) {
		try {
			return JSON.toJSONString(value);
		} catch (Exception e) {
			return value == null ? null : value.toString();
		}
	}
	
	protected Subject getSubject() {
		return SecurityUtils.getSubject();
	}
}
