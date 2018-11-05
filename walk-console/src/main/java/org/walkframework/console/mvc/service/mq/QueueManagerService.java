package org.walkframework.console.mvc.service.mq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.stereotype.Service;
import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.cache.util.CollectionHelper;
import org.walkframework.console.mvc.service.base.BaseConsoleService;
import org.walkframework.console.tools.utils.HexSerializableUtil;
import org.walkframework.data.bean.PageData;
import org.walkframework.data.bean.Pagination;
import org.walkframework.data.util.DataMap;
import org.walkframework.data.util.IData;
import org.walkframework.data.util.InParam;
import org.walkframework.mq.queue.IQueue;
import org.walkframework.mq.queue.IQueueManager;
import org.walkframework.mq.queue.redis.RedisQueue;
import org.walkframework.mq.queue.redis.ValueWrapper;

import com.alibaba.fastjson.JSONObject;

/**
 * 队列管理
 * 
 * @author shf675
 * 
 */
@Service("queueManagerService")
public class QueueManagerService extends BaseConsoleService {
	
	private final static String QUEUE_MAP_NAME = "_QUEUE_MAP_NAME";

	private IQueueManager queueManager;
	
	public IQueueManager getQueueManager() {
		if(this.queueManager == null){
			this.queueManager = SpringContextHolder.getBean("queueManager", IQueueManager.class);
		}
		return queueManager;
	}

	/**
	 * 队列列表查询
	 * 
	 * @param inParam
	 * @param pagination
	 * @return
	 */
	@SuppressWarnings( { "unchecked", "serial" })
	public PageData<IData<String, Object>> queryQueueList(InParam<String, Object> inParam, Pagination pagination) {
		Collection<String> queueNames = getQueueManager().getQueueNames();
		final String queueName = inParam.getString("queueName", "");
		
		// 查找cacheName
		queueNames = CollectionUtils.select(queueNames, new Predicate() {
			@Override
			public boolean evaluate(Object name) {
				if (name != null && name.toString().matches(getPattenStr(queueName))) {
					return true;
				}
				return false;
			}
		});
		// 根据分页参数进行分割
		List<String> sublist = CollectionHelper.subCollection(queueNames, pagination.getStart(), pagination.getSize());
		List caches = new ArrayList<IData<String, Object>>();
		for (final String name : sublist) {
			caches.add(new DataMap<String, Object>() {
				{
					put("queueName", name);
					put("queueSize", getQueueManager().getQueue(name).size());
				}
			});
		}

		// 返回分页对象
		PageData<IData<String, Object>> pageData = new PageData<IData<String, Object>>();
		pageData.setPageSize(pagination.getSize());
		pageData.setTotal(queueNames.size());
		pageData.setRows(caches);
		return pageData;
	}
	
	/**
	 * 队列清空
	 * 
	 * @param inParam
	 * @return
	 */
	public void clearQueue(InParam<String, Object> inParam) {
		String[] queueNames = inParam.getString("queueNames", "").split(",");
		if (queueNames.length == 0) {
			common.error("未选择任何记录！");
		}
		// 循环清空
		for (String queueName : queueNames) {
			getQueueManager().getQueue(queueName).clear();
		}
	}
	
	/**
	 * 队列元素列表查询
	 * 
	 * @param inParam
	 * @param pagination
	 * @return
	 */
	@SuppressWarnings( { "unchecked", "serial" })
	public PageData<IData<String, Object>> queryQueueElementList(InParam<String, Object> inParam, Pagination pagination) {
		// 每次查询前先清空
		getQueueMap().clear();

		// 分页对象
		PageData<IData<String, Object>> pageData = new PageData<IData<String, Object>>();

		// 设置分页数量
		pageData.setPageSize(pagination.getSize());

		final String queueName = inParam.getString("queueName", "");
		List<IData<String, Object>> elements = new ArrayList<IData<String, Object>>();
		IQueue queue = getQueueManager().getQueue(queueName);
		Iterator<Object> iter = queue.iterator(pagination.getStart(), pagination.getSize());
		if (iter != null) {
			int i = 0;
			while (iter.hasNext()) {
				Object queueContent = iter instanceof RedisQueue.QueueIterator? ((RedisQueue.QueueIterator)iter).nextValueWrapper():iter.next();
				IData<String, Object> data = new DataMap<String, Object>();
				data.put("queueIndex", getQueueIndex(i, queueContent));
				data.put("queueContent", queueContent instanceof ValueWrapper ? ((ValueWrapper)queueContent).get() : queueContent);
				elements.add(data);
				i++;
			}
			// 设置总数
			pageData.setTotal(queue.size());
		}
		
		// 设置结果集
		pageData.setRows(elements);
		return pageData;
	}
	
	/**
	 * 新元素入队
	 * 
	 * @param inParam
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void offerQueueElement(InParam<String, Object> inParam) {
		Object eleValue = inParam.getString("eleValue", "").trim();
		if("".equals(eleValue)){
			common.error("错误：元素内容不能为空！");
			
		}
		String eleMode = inParam.getString("eleMode");
		if("hex".equals(eleMode)){
			try {
				eleValue = HexSerializableUtil.decodeHex(eleValue.toString());
			} catch (Exception e) {
				common.error("元素内容不合法！<br>请调用HexSerializableUtil.encodeHex(object)生成hex值后再进行操作！", e);
			}
		}
		
		String queueName = inParam.getString("queueName");
		IQueue queue = getQueueManager().getQueue(queueName);
		queue.offer(eleValue);
	}

	/**
	 * 批量删除队列元素
	 * 
	 * @param inParam
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void removeQueueElement(InParam<String, Object> inParam) {
		String queueName = inParam.getString("queueName");
		String[] queueIndexs = inParam.getString("queueIndexs", "").split(",");
		if (queueIndexs.length == 0) {
			common.error("未选择任何记录！");
		}

		// 循环移除
		IQueue queue = getQueueManager().getQueue(queueName);
		for (String queueIndex : queueIndexs) {
			queue.remove(getQueueMap().get(queueIndex));
		}
	}
	
	/**
	 * 查看队列元素值
	 * 
	 * @param inParam
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object viewQueueElementValue(InParam<String, Object> inParam) {
		String showMode = inParam.getString("showMode", "text");
		String queueIndex = inParam.getString("queueIndex");
		Object value = getQueueMap().get(queueIndex);
		value = value instanceof ValueWrapper ? ((ValueWrapper)value).get() : value;
		String retValue = "";
		if ("text".equals(showMode)) {
			retValue = value == null ? "" : value.toString();
		} else if ("json".equals(showMode)) {
			retValue = toJSONString(value);
		} else if ("hex".equals(showMode)) {
			retValue = HexSerializableUtil.encodeHex(value);
		}
		JSONObject json = new JSONObject();
		json.put("index", queueIndex);
		json.put("type", value == null ? "null" : value.getClass().getName());
		json.put("value", retValue);
		json.put("allowSave", true);
		return json;
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
	 * 获取队列元素索引
	 * 
	 * @param index
	 * @param cacheKey
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected String getQueueIndex(int index, Object queueContent) {
		Map<String, Object> map = getQueueMap();
		if (queueContent != null) {
			String queueIndex = index + "";
			map.put(queueIndex, queueContent);
			getSubject().getSession().setAttribute(QUEUE_MAP_NAME, map);
			return queueIndex;
		}
		return null;
	}

	/**
	 * 获取队列元素缓存map
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, Object> getQueueMap() {
		Map<String, Object> map = (Map<String, Object>) getSubject().getSession().getAttribute(QUEUE_MAP_NAME);
		if (map == null) {
			map = new HashMap<String, Object>();
			getSubject().getSession().setAttribute(QUEUE_MAP_NAME, map);
		}
		return map;
	}
}
