package org.walkframework.shiro.web.filter.authz;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.StringUtils;

/**
 * @author shf675
 *
 */
public abstract class OrAuthorizationHelper {
	public static final char OR_DELIMITER_CHAR = '|';

	/**
	 * 遍历查找存在或的配置，如有权限直接放行
	 * 
	 * @param subject
	 * @param mappedValues
	 * @return
	 */
	public static boolean orAccessAllowed(Subject subject, String[] mappedValues) {
		for (String mappedValue : mappedValues) {
			if (mappedValue.contains(String.valueOf(OR_DELIMITER_CHAR))) {
				String[] orMappedValues = StringUtils.split(mappedValue, OR_DELIMITER_CHAR);
				for (String orMappedValue : orMappedValues) {
					if (subject.isPermitted(orMappedValue)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
