package org.walkframework.console.mvc.service.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.ehcache.Ehcache;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Service;
import org.walkframework.base.system.staticparam.StaticParam;
import org.walkframework.base.system.staticparam.StaticParamConstants;
import org.walkframework.base.system.staticparam.StaticParamDataLoader;
import org.walkframework.base.system.staticparam.StaticParamLoadManager;
import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.cache.ICache;
import org.walkframework.cache.ICacheManager;
import org.walkframework.cache.ehcache.EhCacheDecorator;
import org.walkframework.cache.redis.RedisCacheDecorator;
import org.walkframework.cache.util.CollectionHelper;
import org.walkframework.console.mvc.service.base.BaseConsoleService;
import org.walkframework.console.tools.utils.HexSerializableUtil;
import org.walkframework.data.bean.PageData;
import org.walkframework.data.bean.Pagination;
import org.walkframework.data.util.DataMap;
import org.walkframework.data.util.IData;
import org.walkframework.data.util.InParam;

import com.alibaba.fastjson.JSONObject;

/**
 * 缓存管理
 * 
 * @author shf675
 * 
 */
@Service("cacheManagerService")
public class CacheManagerService extends BaseConsoleService {
	
	private final static String CACHE_KEY_MAP_NAME = "_CACHE_KEY_MAP_NAME";

	@Resource(name = "${cacheManagerName}")
	private ICacheManager cacheManager;

	/**
	 * 缓存列表查询
	 * 
	 * @param inParam
	 * @param pagination
	 * @return
	 */
	@SuppressWarnings( { "unchecked", "serial" })
	public PageData<IData<String, Object>> queryCacheList(InParam<String, Object> inParam, Pagination pagination) {
		Collection<String> cacheNames = cacheManager.getCacheNames();
		final String cacheName = inParam.getString("cacheName", "");
		
		// 查找cacheName
		cacheNames = CollectionUtils.select(cacheNames, new Predicate() {
			@Override
			public boolean evaluate(Object name) {
				if (name != null && name.toString().matches(getPattenStr(cacheName))) {
					return true;
				}
				return false;
			}
		});
		// 根据分页参数进行分割
		List<String> sublist = CollectionHelper.subCollection(cacheNames, pagination.getStart(), pagination.getSize());
		List caches = new ArrayList<IData<String, Object>>();
		for (final String name : sublist) {
			caches.add(new DataMap<String, Object>() {
				{
					put("cacheName", name);
					put("cacheSize", cacheManager.getICache(name).size());
				}
			});
		}

		// 返回分页对象
		PageData<IData<String, Object>> pageData = new PageData<IData<String, Object>>();
		pageData.setPageSize(pagination.getSize());
		pageData.setTotal(cacheNames.size());
		pageData.setRows(caches);
		return pageData;
	}

	/**
	 * 缓存批量清空
	 * 
	 * @param inParam
	 * @return
	 */
	public void clearCache(InParam<String, Object> inParam) {
		String[] cacheNames = inParam.getString("cacheNames", "").split(",");
		if (cacheNames.length == 0) {
			common.error("未选择任何记录！");
		}
		// 循环清空
		for (String cacheName : cacheNames) {
			cacheManager.getICache(cacheName).clear();
		}
	}

	/**
	 * 缓存元素列表查询
	 * 
	 * @param inParam
	 * @param pagination
	 * @return
	 */
	@SuppressWarnings( { "unchecked", "serial" })
	public PageData<IData<String, Object>> queryCacheElementList(InParam<String, Object> inParam, Pagination pagination) {
		// 每次查询前先清空
		getCacheKeyMap().clear();

		// 分页对象
		PageData<IData<String, Object>> pageData = new PageData<IData<String, Object>>();

		// 设置分页数量
		pageData.setPageSize(pagination.getSize());

		final String cacheKey = inParam.getString("cacheKey", "");
		List<IData<String, Object>> elements = new ArrayList<IData<String, Object>>();
		final ICache cache = cacheManager.getICache(inParam.getString("cacheName"));
		if (cache instanceof RedisCacheDecorator) {
			String keyPattern = "".equals(cacheKey) ? "*" : "*" + cacheKey + "*";
			RedisCacheDecorator redisCache = (RedisCacheDecorator) cache;
			Iterator<Object> iter = redisCache.keys(pagination.getStart(), pagination.getSize(), keyPattern);
			if (iter != null) {
				int i = 0;
				while (iter.hasNext()) {
					addData(elements, cache, iter.next(), i);
					i++;
				}
				// 设置总数
				pageData.setTotal(redisCache.size(keyPattern));
			}

		} else if (cache instanceof EhCacheDecorator) {
			Collection keys = ((Ehcache) cache.getNativeCache()).getKeys();
			// 查找key
			if (!"".equals(cacheKey)) {
				keys = CollectionUtils.select(keys, new Predicate() {
					@Override
					public boolean evaluate(Object key) {
						if (key instanceof String && key.toString().matches(getPattenStr(cacheKey))) {
							return true;
						}
						return false;
					}
				});
			}
			// 设置总数
			pageData.setTotal(keys.size());

			// 根据分页参数进行分割
			List<Object> sublist = CollectionHelper.subCollection(keys, pagination.getStart(), pagination.getSize());
			int i = 0;
			for (final Object key : sublist) {
				addData(elements, cache, key, i);
				i++;
			}
		}
		// 设置结果集
		pageData.setRows(elements);
		return pageData;
	}
	
	/**
	 * 新增元素
	 * 
	 * @param inParam
	 * @return
	 */
	public void addElement(InParam<String, Object> inParam) {
		Object eleKey = inParam.getString("eleKey", "").trim();
		Object eleValue = inParam.getString("eleValue", "").trim();
		if("".equals(eleKey)){
			common.error("错误：元素key不能为空！");
			
		}
		if("".equals(eleValue)){
			common.error("错误：元素value不能为空！");
			
		}
		
		if("hex".equals(inParam.getString("eleKeyMode"))){
			try {
				eleKey = HexSerializableUtil.decodeHex(eleKey.toString());
			} catch (Exception e) {
				common.error("元素key不合法！<br>请调用HexSerializableUtil.encodeHex(object)生成hex值后再进行操作！", e);
			}
		}
		
		if("hex".equals(inParam.getString("eleValueMode"))){
			try {
				eleValue = HexSerializableUtil.decodeHex(eleValue.toString());
			} catch (Exception e) {
				common.error("元素value不合法！<br>请调用HexSerializableUtil.encodeHex(object)生成hex值后再进行操作！", e);
			}
		}
		
		String cacheName = inParam.getString("cacheName");
		ICache cache = cacheManager.getICache(cacheName);
		cache.put(eleKey, eleValue);
	}
	
	/**
	 * 批量删除缓存元素
	 * 
	 * @param inParam
	 * @return
	 */
	public void removeCacheElement(InParam<String, Object> inParam) {
		String cacheName = inParam.getString("cacheName");
		String[] cacheKeyIndexs = inParam.getString("cacheKeyIndexs", "").split(",");
		if (cacheKeyIndexs.length == 0) {
			common.error("未选择任何记录！");
		}
		// 循环移除
		ICache cache = cacheManager.getICache(cacheName);
		for (String cacheKeyIndex : cacheKeyIndexs) {
			cache.evict(getCacheKeyMap().get(cacheKeyIndex));
		}
	}

	/**
	 * 批量设置元素过期时间
	 * 
	 * @param inParam
	 * @return
	 */
	public void setExpireCacheElement(InParam<String, Object> inParam) {
		Boolean all = inParam.getBoolean("all");
		String cacheName = inParam.getString("cacheName");
		Long expireTime = inParam.getLong("expireTime");
		String[] cacheKeyIndexs = inParam.getString("cacheKeyIndexs", "").split(",");
		if (cacheKeyIndexs.length == 0) {
			common.error("未选择任何记录！");
		}
		
		ICache cache = cacheManager.getICache(cacheName);
		
		//如果是设置所有元素的过期时间
		if(all){
			Iterator<Object> iter = cache.keys();
			if (iter != null) {
				while (iter.hasNext()) {
					cache.expire(iter.next(), expireTime);
				}
			}
		} else {
			//指定的元素设置过期时间
			for (String cacheKeyIndex : cacheKeyIndexs) {
				cache.expire(getCacheKeyMap().get(cacheKeyIndex), expireTime);
			}
		}
	}
	
	/**
	 * 查看元素值
	 * 
	 * @param inParam
	 * @return
	 */
	public Object viewCacheElementValue(InParam<String, Object> inParam) {
		String cacheName = inParam.getString("cacheName");
		String showMode = inParam.getString("showMode", "text");
		String cacheKeyIndex = inParam.getString("cacheKeyIndex");
		ICache cache = cacheManager.getICache(cacheName);
		Object value = cache.getValue(getCacheKeyMap().get(cacheKeyIndex));
		String retValue = "";
		if ("text".equals(showMode)) {
			retValue = value == null ? "" : value.toString();
		} else if ("json".equals(showMode)) {
			retValue = toJSONString(value);
		} else if ("hex".equals(showMode)) {
			retValue = HexSerializableUtil.encodeHex(value);
		}
		JSONObject json = new JSONObject();
		json.put("index", cacheKeyIndex);
		json.put("type", value == null ? "null" : value.getClass().getName());
		json.put("value", retValue);
		json.put("allowSave", true);
		return json;
	}
	
	/**
	 * 保存新值
	 * 
	 * @param inParam
	 * @return
	 */
	public void saveNewVale(InParam<String, Object> inParam) {
		ICache cache = cacheManager.getICache(inParam.getString("cacheName"));
		Object key = getCacheKeyMap().get(inParam.getString("cacheKeyIndex"));
		try {
			//解码、反序列化
			Object newValue = HexSerializableUtil.decodeHex(inParam.getString("newValue", ""));
			cache.put(key, newValue);
		} catch (DecoderException e) {
			common.error("新值不合法！<br>请调用HexSerializableUtil.encodeHex(object)生成hex值后再保存！", e);
		}
	}
	
	/**
	 * 添加数据
	 * 
	 * @param elements
	 * @param cache
	 * @param key
	 * @param index
	 */
	private void addData(List<IData<String, Object>> elements, ICache cache, Object key, int index){
		IData<String, Object> data = new DataMap<String, Object>();
		data.put("cacheKeyIndex", getCacheKeyIndex(index, key));
		data.put("cacheKey", key == null ? null : key.toString());
		data.put("cacheTTL", cache.ttl(key));
		elements.add(data);
	}
	
	/**
	 * 静态参数表缓存列表查询
	 * 
	 * @param inParam
	 * @param pagination
	 * @return
	 */
	@SuppressWarnings( { "unchecked", "serial" })
	public PageData<IData<String, Object>> queryStaticParamCacheList(InParam<String, Object> inParam, Pagination pagination) {
		// 分页对象
		PageData<IData<String, Object>> pageData = new PageData<IData<String, Object>>();

		// 设置分页数量
		pageData.setPageSize(pagination.getSize());

		final ICache staticParamCache = cacheManager.getICache(StaticParamConstants.STATIC_PARAM_CACHE_NAME);
		final String cacheName = inParam.getString("cacheName", "");
		List<IData<String, Object>> elements = new ArrayList<IData<String, Object>>();
		if (staticParamCache instanceof RedisCacheDecorator) {
			String keyPattern = "".equals(cacheName) ? "*" : "*" + cacheName + "*";
			RedisCacheDecorator redisCache = (RedisCacheDecorator) staticParamCache;
			Iterator<Object> iter = redisCache.keys(pagination.getStart(), pagination.getSize(), keyPattern);
			if (iter != null) {
				int i = 0;
				while (iter.hasNext()) {
					addStaticParamData(elements, iter.next());
					i++;
				}
				// 设置总数
				pageData.setTotal(redisCache.size(keyPattern));
			}

		} else if (staticParamCache instanceof EhCacheDecorator) {
			Collection keys = ((Ehcache) staticParamCache.getNativeCache()).getKeys();
			// 查找key
			if (!"".equals(cacheName)) {
				keys = CollectionUtils.select(keys, new Predicate() {
					@Override
					public boolean evaluate(Object key) {
						if (key instanceof String && key.toString().matches(getPattenStr(cacheName))) {
							return true;
						}
						return false;
					}
				});
			}
			// 设置总数
			pageData.setTotal(keys.size());

			// 根据分页参数进行分割
			List<Object> sublist = CollectionHelper.subCollection(keys, pagination.getStart(), pagination.getSize());
			int i = 0;
			for (final Object key : sublist) {
				addStaticParamData(elements, key);
				i++;
			}
		}
		// 设置结果集
		pageData.setRows(elements);
		return pageData;
	}
	
	/**
	 * 重新加载静态参数表
	 * 
	 * @param inParam
	 * @return
	 */
	@SuppressWarnings("serial")
	public void reloadStaticParam(InParam<String, Object> inParam) {
		String[] cacheNames = inParam.getString("cacheNames", "").split(",");
		if (cacheNames.length == 0) {
			common.error("未选择任何记录！");
		}
		
		final ICache staticParamCache = cacheManager.getICache(StaticParamConstants.STATIC_PARAM_CACHE_NAME);
		// 循环加载
		for (String cacheName : cacheNames) {
			final StaticParam staticParam = staticParamCache.getValue(cacheName);
			String beanId = staticParam.getManagerId();
			try {
				StaticParamDataLoader staticParamDataLoader = SpringContextHolder.getBean(StaticParamDataLoader.class);
				StaticParamLoadManager staticParamLoadManager = staticParamDataLoader.getStaticParamsCaches().get(beanId);
				if(staticParamLoadManager == null){
					common.error(String.format("bean id[%s]不存在，可能是未配置或在其他服务上。", beanId));
				}
				staticParamLoadManager.loadAll(new ArrayList<StaticParam>(){{
					add(staticParam);
				}});
			} catch (NoSuchBeanDefinitionException e) {
				common.error(String.format("静态参数加载器未配置。"), e);
			}
		}
	}
	
	/**
	 * 添加数据
	 * 
	 * @param elements
	 * @param cache
	 * @param key
	 * @param index
	 */
	private void addStaticParamData(List<IData<String, Object>> elements, Object name){
		if(name != null){
			IData<String, Object> data = new DataMap<String, Object>();
			data.put("showCacheName", name);
			data.put("cacheName", StaticParamConstants.STATIC_PARAM_PREFIX.concat(".").concat(name.toString()));
			data.put("cacheSize", cacheManager.getICache(data.getString("cacheName")).size());
			elements.add(data);
		}
	}
	
	/**
	 * 获取匹配串
	 * 
	 * @param originalValue
	 * @return
	 */
	protected String getPattenStr(String originalValue) {
		return "(.*)".concat(originalValue.replaceAll("\\*", "(.*)").concat("(.*)"));
	}

	/**
	 * 获取缓存元素键值索引
	 * 
	 * @param index
	 * @param cacheKey
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected String getCacheKeyIndex(int index, Object cacheKey) {
		Map<String, Object> cacheKeyMap = getCacheKeyMap();
		if (cacheKey != null) {
			String keyIndex = index + "";
			cacheKeyMap.put(keyIndex, cacheKey);
			getSubject().getSession().setAttribute(CACHE_KEY_MAP_NAME, cacheKeyMap);
			return keyIndex;
		}
		return null;
	}

	/**
	 * 获取缓存元素键值缓存map
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, Object> getCacheKeyMap() {
		Map<String, Object> cacheKeyMap = (Map<String, Object>) getSubject().getSession().getAttribute(CACHE_KEY_MAP_NAME);
		if (cacheKeyMap == null) {
			cacheKeyMap = new HashMap<String, Object>();
			getSubject().getSession().setAttribute(CACHE_KEY_MAP_NAME, cacheKeyMap);
		}
		return cacheKeyMap;
	}
}
