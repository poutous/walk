package org.walkframework.base.tools.utils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.base.mvc.entity.TdSParam;
import org.walkframework.base.mvc.service.common.CommonService;
import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.factory.SingletonFactory;
import org.walkframework.base.system.staticparam.StaticParamConstants;
import org.walkframework.base.system.staticparam.StaticParamUtil;
import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.data.entity.EntityHelper;
import org.walkframework.data.util.IData;

/**
 * 参数翻译静态类
 * 
 */
public abstract class ParamTranslateUtil {

	protected static Logger log = LoggerFactory.getLogger(ParamTranslateUtil.class);
	protected static Common common = SingletonFactory.getInstance(Common.class);

	/**
	 * 去静态参数表翻译名称
	 * 
	 * @param typeId
	 * @param value
	 * @return
	 */
	public static String convertStatic(String typeId, String value) {
		if(StringUtil.isEmpty(value)){
			return null;
		}
		// 尝试从缓存取
		IData<String, Object> cacheData = StaticParamUtil.getCache(StaticParamConstants.TD_S_STATIC).getValue(StaticParamUtil.getMapCacheKey(typeId));
		if (cacheData != null && !cacheData.isEmpty()) {
			return MapSelector.getValue(cacheData, value + StaticParamConstants.POINT + "DATA_NAME");
		}

		String convertName = null;
		try {
			convertName = SpringContextHolder.getBean(CommonService.class).convertCode2Name(typeId, value);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return convertName == null ? "" : convertName;
	}

	/**
	 * get translate value
	 * 
	 * @param value
	 * @param datasrc
	 * @return String
	 */
	public static String getTranslateValue(String value, String datasrc) {
		String staticValue = null;
		try {
			String srcValue = value;
			if(StringUtil.isEmpty(srcValue)){
				return null;
			}
			CommonService commonService = SpringContextHolder.getBean(CommonService.class);
			if (datasrc.indexOf(".") == -1) {
				// 尝试从缓存取
				IData<String, Object> cacheData = StaticParamUtil.getCache(StaticParamConstants.TD_S_STATIC).getValue(StaticParamUtil.getMapCacheKey(datasrc));
				if (cacheData != null && !cacheData.isEmpty()) {
					return MapSelector.getValue(cacheData, value + StaticParamConstants.POINT + "DATA_NAME");
				}
				staticValue = commonService.convertCode2Name(datasrc, srcValue);
			} else {
				// 尝试从缓存取
				try {
					boolean flag = true;
					String[] srcs = datasrc.split(";");
					for (int i = 0; i < srcs.length; i++) {
						String[] array = srcs[i].split("\\.");
						String tableName = array[0];
						String colName = array[2];
						IData<String, Object> cacheData = StaticParamUtil.getCache(tableName).getValue(StaticParamUtil.getMapCacheKey(srcValue));
						if (cacheData == null || cacheData.isEmpty()) {
							flag = false;
							break;
						}
						staticValue = cacheData.getString(colName);
						if (staticValue == null) {
							flag = false;
							break;
						}
						srcValue = staticValue;
					}
					if (flag) {
						return srcValue;
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}

				// 从数据库查询
				String[] srcs = datasrc.split(";");
				for (int i = 0; i < srcs.length; i++) {
					String[] array = srcs[i].split("\\.");
					String tableName = array[0];
					String colCode = array[1];
					String colName = array[2];
					staticValue = commonService.convertCode2Name(tableName, colCode, colName, srcValue);
					if (staticValue == null) {
						return staticValue;
					}
					srcValue = staticValue;
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return staticValue == null ? "" : staticValue;
	}

	/**
	 * 根据id去指定表翻译名称
	 * 
	 * @param tableName
	 * @param colCode
	 * @param colName
	 * @param value
	 * @return
	 */
	public static String convertTable(String tableName, String colCode, String colName, String value) {
		// 尝试从缓存取
		IData<String, Object> cacheData = StaticParamUtil.getCache(tableName).getValue(StaticParamUtil.getMapCacheKey(value));
		if (cacheData != null && !cacheData.isEmpty()) {
			return cacheData.getString(colName);
		}

		String convertName = null;
		try {
			convertName = SpringContextHolder.getBean(CommonService.class).convertCode2Name(tableName, colCode, colName, value);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return convertName == null ? "" : convertName;
	}

	/**
	 * 获取列表
	 * 
	 * @param key1
	 * @return
	 */
	public static List<TdSParam> staticlist(String typeId) {
		// 尝试从缓存取
		List<IData<String, Object>> cacheData = StaticParamUtil.getCache(StaticParamConstants.TD_S_STATIC).getValue(StaticParamUtil.getListCacheKey(typeId));
		if (cacheData != null && !cacheData.isEmpty()) {
			List<TdSParam> list = new ArrayList<TdSParam>();
			for (IData<String, Object> data : cacheData) {
				TdSParam tdSParam = EntityHelper.map2entity(data, TdSParam.class);
				list.add(tdSParam);
			}
			return list;
		}

		List<TdSParam> list = null;
		try {
			list = SpringContextHolder.getBean(CommonService.class).queryStaticList(typeId, null);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return list;
	}
}
