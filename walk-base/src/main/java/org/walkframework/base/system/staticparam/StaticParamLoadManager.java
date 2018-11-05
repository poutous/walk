package org.walkframework.base.system.staticparam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.util.StringUtils;
import org.walkframework.base.mvc.service.common.AbstractBaseService;
import org.walkframework.cache.ICache;
import org.walkframework.cache.ICacheManager;
import org.walkframework.data.bean.PageData;
import org.walkframework.data.bean.Pagination;
import org.walkframework.data.util.DataMap;
import org.walkframework.data.util.IData;

/**
 * 静态参数缓存加载器
 * 
 * @author shf675
 * 
 */
public class StaticParamLoadManager extends AbstractBaseService implements BeanNameAware {

	private final static Logger log = LoggerFactory.getLogger(StaticParamLoadManager.class);

	/**
	 * 默认分页尺寸
	 */
	private static final int DEFAULT_PAGE_SIZE = 1000;

	/**
	 * 分页数量。分批加载，防止一次加载过多数据造成内存溢出
	 */
	private int pageSize = DEFAULT_PAGE_SIZE;

	/**
	 * 缓存管理器
	 */
	private ICacheManager cacheManager;
	
	/**
	 * 静态参数配置文件路径
	 */
	private String configLocation;
	
	/**
	 * 当前beanId
	 */
	private String beanId;
	

	@Override
	public void setBeanName(String name) {
		this.beanId = name;
	}
	
	public String getBeanId() {
		return beanId;
	}

	/**
	 * 根据文件加载
	 */
	public void loadAllByFile() {
		log.info("******{} Start load...", configLocation);
		long beginTime = System.currentTimeMillis();
		loadAll(StaticParamUtil.loadStaticParamConfigList(getConfigLocation()));
		log.info("******{} End load... cost time:{}s", configLocation, ((double) (System.currentTimeMillis() - beginTime) / (double) 1000));
	}
	
	/**
	 * 加载全部
	 */
	public void loadAll(List<StaticParam> allStaticParams) {
		if (allStaticParams != null) {
			ICache staticParamCache = getCacheManager().getICache(StaticParamConstants.STATIC_PARAM_CACHE_NAME);
			for (StaticParam staticParam : allStaticParams) {
				//将此映射置入缓存
				staticParam.setManagerId(this.beanId);
				staticParamCache.put(staticParam.getKey(), staticParam);
				
				if(staticParam.isLoad()){
					// 1、初始化静态参数表TD_S_STATIC
					if (StaticParamConstants.TD_S_STATIC.equals(staticParam.getKey())) {
						loadStaticParams(staticParam);
					}
					// 2、初始化普通静态参数表
					else {
						loadTableParams(staticParam);
					}
					log.info("{} Loaded.", staticParam.getKey());
				}
			}
		}
	}

	/**
	 * 加载静态参数表TD_S_STATIC
	 * 
	 */
	public void loadStaticParams(StaticParam staticParam) {
		if(!staticParam.isLoad()){
			return;
		}
		ICache cache = StaticParamUtil.getCache(getCacheManager(), staticParam.getKey());
		IData<String, List<IData<String, String>>> typeIdList;
		IData<String, IData<String, IData<String, String>>> typeIdMap;

		// 分批加载，防止内存溢出
		int pageSize = getPageSize();
		int i = 0;
		for (;;) {
			Pagination pagination = new Pagination();
			pagination.setNeedCount(false);
			pagination.setRange(i * pageSize, pageSize);
			PageData<IData<String, String>> pageData = dao().selectList("CommonSQL.selectAllStaticList", null, pagination);
			List<IData<String, String>> subList = pageData.getRows();
			if (subList.size() > 0) {
				try {
					typeIdList = new DataMap<String, List<IData<String, String>>>();
					typeIdMap = new DataMap<String, IData<String, IData<String, String>>>();
					for (IData<String, String> data : subList) {
						String typeId = data.getString("TYPE_ID");

						// 按typeId分List结构
						List<IData<String, String>> dataIdList = typeIdList.get(typeId);
						if (dataIdList == null) {
							dataIdList = new ArrayList<IData<String, String>>();
							typeIdList.put(typeId, dataIdList);
						}
						dataIdList.add(data);

						// 全map结构
						IData<String, IData<String, String>> dataIdMap = typeIdMap.get(typeId);
						if (dataIdMap == null) {
							dataIdMap = new DataMap<String, IData<String, String>>();
							typeIdMap.put(typeId, dataIdMap);
						}
						dataIdMap.put(data.getString("DATA_ID"), data);
					}

					// 按typeId分List结构置入缓存
					for (Map.Entry<String, List<IData<String, String>>> entry : typeIdList.entrySet()) {
						cache.put(StaticParamUtil.getListCacheKey(entry.getKey()), entry.getValue());
					}

					// 全map结构置入缓存
					for (Map.Entry<String, IData<String, IData<String, String>>> entry : typeIdMap.entrySet()) {
						String typeId = StaticParamUtil.getMapCacheKey(entry.getKey());
						IData<String, IData<String, String>> dataIdMap = cache.getValue(typeId);
						if (dataIdMap == null) {
							cache.put(typeId, entry.getValue());
						} else {
							dataIdMap.putAll(entry.getValue());
							cache.put(typeId, dataIdMap);
						}
					}

				} catch (Exception e) {
					throw new RuntimeException("error!", e);
				}

				if (subList.size() < pageSize) {
					break;
				}
			} else {
				break;
			}
			i++;
		}
	}

	/**
	 * 加载指定普通静态参数表
	 * 
	 * @param staticParam
	 */
	public void loadTableParams(StaticParam staticParam) {
		if(!staticParam.isLoad()){
			return;
		}
		ICache cache = StaticParamUtil.getCache(getCacheManager(), staticParam.getKey());

		// 分批加载，防止内存溢出
		int pageSize = getPageSize();
		int i = 0;
		for (;;) {
			Pagination pagination = new Pagination();
			pagination.setNeedCount(false);
			pagination.setRange(i * pageSize, pageSize);
			String sqlId = StringUtils.isEmpty(staticParam.getSqlId()) ? "CommonSQL.selectTableList" : staticParam.getSqlId();
			PageData<IData<String, String>> pageData = dao().selectList(sqlId, staticParam, pagination);
			List<IData<String, String>> subList = pageData.getRows();
			if (subList.size() > 0) {
				try {
					for (IData<String, String> data : subList) {
						cache.put(StaticParamUtil.getMapCacheKey(data.getString(staticParam.getPrimaryKey())), data);
					}
					cache.put(StaticParamUtil.getListCacheKey(), subList);
				} catch (Exception e) {
					throw new RuntimeException("error!", e);
				}

				if (subList.size() < pageSize) {
					break;
				}
			} else {
				break;
			}
			i++;
		}
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public ICacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(ICacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	
	public String getConfigLocation() {
		return configLocation;
	}
	
	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}
}
