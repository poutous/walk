package org.walkframework.base.tools.utils;

import org.walkframework.base.tools.spring.SpringPropertyHolder;

/**
 * @author shf675
 *
 */
public class FunctionUtil {

	public static String getContextProperty(String name) {
		return SpringPropertyHolder.getContextProperty(name);
	}
}
