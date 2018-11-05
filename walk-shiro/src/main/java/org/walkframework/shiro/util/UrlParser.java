package org.walkframework.shiro.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * url解析
 * 
 * @author shf675
 *
 */
public abstract class UrlParser {
	
	protected static Logger log = LoggerFactory.getLogger(UrlParser.class);
	
	/**
	 * 解析url获取参数值
	 * 
	 * @param url
	 * @param name
	 * @return
	 */
	public static String getValueByParam(String url, String name){
		return getValueByParam(url, name, "");
	}
	
	/**
	 * 解析url获取参数值
	 * 
	 * @param url
	 * @param name
	 * @param defaultValue
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getValueByParam(String url, String name, String defaultValue){
		String u = null;
		try {
			u = URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		}
		Pattern p = Pattern.compile(name + "=([^&]*)(&|$)");
		Matcher m = p.matcher(u);
		if (m.find()) {
			String value = m.group(1);
			if(value != null && !"".equals(value.trim())){
				return value;
			}
		}
		return defaultValue;
	}
}
