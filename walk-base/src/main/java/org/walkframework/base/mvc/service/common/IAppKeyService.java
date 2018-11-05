package org.walkframework.base.mvc.service.common;

public interface IAppKeyService {

	/**
	 * 获取appkey
	 * 
	 * @return
	 */
	String getAppKey(final String appId, String encode);

}