package org.walkframework.base.mvc.service.common;

import org.springframework.util.StringUtils;
import org.walkframework.base.mvc.entity.TdMAppCfg;
import org.walkframework.data.entity.Conditions;

/**
 * appkey 服务
 * 
 * @author shf675
 * 
 */
public class AppKeyService extends AbstractBaseService implements IAppKeyService {

	//默认缓存60秒
	private int DEFAULT_CACHE_SECONDS = 60;
	
	private int cacheSeconds = DEFAULT_CACHE_SECONDS;

	/**
	 * 获取appkey
	 * 缓存1分钟
	 * 
	 * @return
	 */
	@SuppressWarnings("serial")
	public String getAppKey(final String appId, String encode) {
		if("true".equals(encode)){
			TdMAppCfg cfg = dao().selectOne(new Conditions(TdMAppCfg.class){{
				andEqual(TdMAppCfg.APP_ID, appId);
				andLessEqual(TdMAppCfg.START_DATE, common.getCurrentTime());
				andGreaterEqual(TdMAppCfg.END_DATE, common.getCurrentTime());
			}}, getCacheSeconds());
			if (cfg == null || StringUtils.isEmpty(cfg.getAppKey())) {
				String errMsg = "appId[" + appId + "] is not configured or ineffective or expired!";
				common.error(errMsg);
			}
			return cfg.getAppKey();
		}
		return null;
	}
	
	public int getCacheSeconds() {
		return cacheSeconds;
	}

	public void setCacheSeconds(int cacheSeconds) {
		this.cacheSeconds = cacheSeconds;
	}
}
