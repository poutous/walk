package org.walkframework.console.mvc.service.mq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
import org.walkframework.mq.pubsub.IPubSubManager;

/**
 * 发布订阅管理
 * 
 * @author shf675
 * 
 */
@Service("pubSubManagerService")
public class PubSubManagerService extends BaseConsoleService {
	
	private IPubSubManager pubSubManager;

	public IPubSubManager getPubSubManager() {
		if(this.pubSubManager == null){
			this.pubSubManager = SpringContextHolder.getBean("pubSubManager", IPubSubManager.class);
		}
		return pubSubManager;
	}

	/**
	 * 频道列表查询
	 * 
	 * @param inParam
	 * @param pagination
	 * @return
	 */
	@SuppressWarnings( { "unchecked", "serial" })
	public PageData<IData<String, Object>> queryChannelList(InParam<String, Object> inParam, Pagination pagination) {
		Collection<String> channels = getPubSubManager().pubsubChannels("*");
		final String channel = inParam.getString("channel", "");
		
		// 查找channel
		channels = CollectionUtils.select(channels, new Predicate() {
			@Override
			public boolean evaluate(Object name) {
				if (name != null && name.toString().matches(getPattenStr(channel))) {
					return true;
				}
				return false;
			}
		});
		
		final Map<String, String> channelNums = getPubSubManager().pubsubNumSub(channels.toArray(new String[channels.size()]));
		// 根据分页参数进行分割
		List<String> sublist = CollectionHelper.subCollection(channels, pagination.getStart(), pagination.getSize());
		List<IData<String, Object>> channelList = new ArrayList<IData<String, Object>>();
		for (final String name : sublist) {
			channelList.add(new DataMap<String, Object>() {
				{
					put("channel", name);
					put("subscriberNum", channelNums == null ? "0" : channelNums.get(name));
				}
			});
		}

		// 返回分页对象
		PageData<IData<String, Object>> pageData = new PageData<IData<String, Object>>();
		pageData.setPageSize(pagination.getSize());
		pageData.setTotal(channels.size());
		pageData.setRows(channelList);
		return pageData;
	}
	
	/**
	 * 发布消息
	 * 
	 * @param inParam
	 * @return
	 */
	public void publishMessage(InParam<String, Object> inParam) {
		Object message = inParam.getString("message", "").trim();
		if("".equals(message)){
			common.error("错误：消息内容不能为空！");
			
		}
		String[] channels = inParam.getString("channels", "").split(",");
		if (channels.length == 0) {
			common.error("未选择任何频道！");
		}
		
		String msgMode = inParam.getString("msgMode");
		if("hex".equals(msgMode)){
			try {
				message = HexSerializableUtil.decodeHex(message.toString());
			} catch (Exception e) {
				common.error("消息内容不合法！<br>请调用HexSerializableUtil.encodeHex(object)生成hex值后再发送消息！", e);
			}
		}
		// 发布消息
		getPubSubManager().publish(message, channels);
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
}
