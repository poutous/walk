package org.walkframework.base.tools.utils;

import org.walkframework.base.mvc.service.common.IAppKeyService;
import org.walkframework.base.tools.spring.SpringContextHolder;

/**
 * appkey工具类
 * 
 * @author shf675
 *
 */
public abstract class AppKeyUtil {
	
	/**
	 * 获取appkey
	 *  
	 * @param appId
	 * @return
	 */
	public static String getAppKey(String appId, String encode){
		IAppKeyService appKeyService = SpringContextHolder.getBean(IAppKeyService.class);
		return appKeyService.getAppKey(appId, encode);
	}
}
