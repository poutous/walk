package org.walkframework.base.tools.utils;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public abstract class MapSelector {
	
	private static final String DEFAULT_SEPARATOR = "\\.";
	
	/**
	 * 指定分隔符取Map里层数据
	 * 分隔符为.
	 * 
	 * @param 
	 * @param selector
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getValue(Map src, String selector) {
		return getValue(src, selector, DEFAULT_SEPARATOR);
	}
	
	/**
	 * 指定分隔符取Map里层数据。例如：如果分隔符为>，取值方法：CustMess>Custid
	 * 
	 * @param src
	 * @param selector
	 * @param separator
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getValue(Map src, String selector, String separator) {
		Object result = null;
		if (StringUtils.isEmpty(selector)) {
			result = src;
		} else {
			// 处理表达式
			String[] level = selector.split(separator);
			
			int lastIdx = level.length - 1;
			String key = null;
			Object tmpResult = src;
			boolean lastIsList = false;
			for (int i = 0; i < level.length; i++) {
				key = StringUtils.trim(level[i]);
				if (StringUtils.isEmpty(key)) {
					lastIsList = false;
					continue;
				}
				if (null == tmpResult) {
					break;
				}
				if (lastIsList) {
					tmpResult = ((List<Map<String, Object>> )tmpResult).get(NumberUtils.toInt(key, 0));
				} else {
					if ("*".equals(key)) {
						tmpResult = ((Map<String, Object>)tmpResult).entrySet().iterator().next().getValue();
					} else {
						tmpResult = ((Map<String, Object>)tmpResult).get(key);
					}
				}
				if (tmpResult instanceof List) {
					lastIsList = true;
				} else {
					lastIsList = false;
				}
				if (i == lastIdx) {
					result = (T)tmpResult;
				}
			}
		}
		
		return (T) result;
	}
}
