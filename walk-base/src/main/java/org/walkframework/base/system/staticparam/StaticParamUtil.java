package org.walkframework.base.system.staticparam;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.walkframework.base.system.config.XMLConfig;
import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.base.tools.spring.SpringPropertyHolder;
import org.walkframework.cache.ICache;
import org.walkframework.cache.ICacheManager;
import org.walkframework.data.util.IDataset;

import com.alibaba.fastjson.JSON;

/**
 * @author shf675
 * 
 */
public abstract class StaticParamUtil {

	private final static Logger log = LoggerFactory.getLogger(StaticParamUtil.class);

	/**
	 * 获取全部静态参数表配置信息
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<StaticParam> loadStaticParamConfigList(String configLocation) {
		if (StringUtils.isEmpty(configLocation)) {
			log.warn("No configuration file specified...");
			return null;
		}
		List<StaticParam> list = null;
		try {
			XMLConfig config = new XMLConfig(configLocation);
			String load = config.getProperty(StaticParamConstants.STATIC_PARAM_ROOT_NODE_NAME + "/load");
			if("false".equals(load)){
				return null;
			}
			IDataset dataset = config.getDataset(StaticParamConstants.STATIC_PARAM_ROOT_NODE_NAME, XMLConfig.XML_FORMAT_ATTRIBUTE);
			list = JSON.parseArray(JSON.toJSONString(dataset), StaticParam.class);
		} catch (Exception e) {
			log.error("Error loading static parameter configuration file[" + configLocation + "]", e);
		}
		return list;
	}
	
	/**
	 * 根据key获取静态参数表配置信息
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static StaticParam loadStaticParamConfig(String configLocation, String key) {
		if(StringUtils.isEmpty(key)){
			return null;
		}
		List<StaticParam> list = loadStaticParamConfigList(configLocation);
		if(list != null){
			for (StaticParam staticParam : list) {
				if(key.endsWith(staticParam.getKey())){
					return staticParam;
				}
			}
		}
		return null;
	}

	public static String getListCacheKey() {
		return StaticParamConstants.DATA_TYPE_LIST;
	}

	public static String getListCacheKey(String key) {
		return getCacheKey(StaticParamConstants.DATA_TYPE_LIST, key);
	}

	public static String getMapCacheKey(String key) {
		return getCacheKey(StaticParamConstants.DATA_TYPE_MAP, key);
	}
	
	private static String getCacheKey(String dataType, String key) {
		StringBuilder keyName = new StringBuilder();
		keyName.append(dataType);
		keyName.append(StaticParamConstants.POINT);
		keyName.append(key);
		return keyName.toString();
	}
	
	/**
	 * 获取缓存
	 * 
	 * @param cacheManagerName
	 * @param key
	 * @return
	 */
	public static ICache getCache(String key) {
		StringBuilder cacheName = new StringBuilder();
		cacheName.append(StaticParamConstants.STATIC_PARAM_PREFIX);
		cacheName.append(StaticParamConstants.POINT);
		cacheName.append(key);
		return getCacheManager().getICache(cacheName.toString());
	}
	
	/**
	 * 获取缓存
	 * 
	 * @param cacheManagerName
	 * @param key
	 * @return
	 */
	public static ICache getCache(ICacheManager cacheManager, String key) {
		StringBuilder cacheName = new StringBuilder();
		cacheName.append(StaticParamConstants.STATIC_PARAM_PREFIX);
		cacheName.append(StaticParamConstants.POINT);
		cacheName.append(key);
		return cacheManager.getICache(cacheName.toString());
	}
	
	public static ICacheManager getCacheManager(){
		try {
			StaticParamLoadManager staticParamLoadManager = SpringContextHolder.getBean(StaticParamLoadManager.class);
			return staticParamLoadManager.getCacheManager();
		} catch (Exception e) {
			String cacheManagerName = SpringPropertyHolder.getContextProperty("cacheManagerName", "springCacheManager");
			return SpringContextHolder.getBean(cacheManagerName, ICacheManager.class);
		}
	}
}
