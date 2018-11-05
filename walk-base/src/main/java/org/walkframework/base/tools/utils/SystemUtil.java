package org.walkframework.base.tools.utils;

import org.walkframework.base.tools.spring.SpringPropertyHolder;

public abstract class SystemUtil {

	/**
	 * 取当前页面
	 * 
	 * @param context
	 * @param uri
	 * @return
	 */
	public static String getPage(String context, String uri) {
		uri = uri == null ? "": uri;
		String prefix = context + SpringPropertyHolder.getContextProperty("viewPrefix");
		String suffix = SpringPropertyHolder.getContextProperty("viewSuffix");
		String page1 = uri.startsWith(prefix) ? uri.substring(prefix.length(), uri.length()) : uri;
		String page = page1.endsWith(suffix) ? page1.substring(0, page1.length() - suffix.length()) : page1;
		return page;
	}
}
