package org.walkframework.base.mvc.service.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.walkframework.base.mvc.entity.TdMNotifyCfg;
import org.walkframework.base.mvc.entity.TfMNotify;
import org.walkframework.base.system.task.notify.NotifyData;
import org.walkframework.batis.bean.BatchEachHandler;
import org.walkframework.shiro.authc.principal.BasePrincipal;

/**
 * 通知服务
 * 
 * @author shf675
 *
 */
@Service("notifyService")
public class NotifyService extends AbstractBaseService implements INotifyService {

	/**
	 * 批量插入通知队列
	 * 
	 * @param notifyList
	 */
	@SuppressWarnings("serial")
	@Override
	public void insertNotifyList(final String serviceId, List<NotifyData> notifyList) {
		if (!CollectionUtils.isEmpty(notifyList)) {
			TdMNotifyCfg cfg = dao().selectOne(new TdMNotifyCfg(){{
				setServiceId(serviceId).asCondition();
			}});
			
			List<TfMNotify> list = new ArrayList<TfMNotify>();
			for (NotifyData notifyData : notifyList) {
				TfMNotify tfMNotify = new TfMNotify();
				
				//通知信息快速拷贝
				BeanUtils.copyProperties(notifyData, tfMNotify);
				
				//配置信息快速拷贝
				BeanUtils.copyProperties(cfg, tfMNotify);

				//设置业务平台编码
				tfMNotify.setServiceId(serviceId);
				
				list.add(tfMNotify);
			}
			
			final String batchId = getSequence("SEQ_BATCH_ID");
			dao().insertBatch(list, new BatchEachHandler<TfMNotify>() {
				@Override
				public void onEach(TfMNotify notify) {
					notify.setNotifyId(getSequence("SEQ_NOTIFY_ID"));
					notify.setBatchId(batchId);
					notify.setNotifyState("0");
					notify.setCreateTime(common.getCurrentTime());
					Object obj = SecurityUtils.getSubject().getPrincipal();
					if (obj instanceof BasePrincipal) {
						notify.setCreateStaffId(((BasePrincipal) obj).getUserId());
					}
				}
			});
		}
	}

	/**
	 * 获取序列
	 * @param sequence
	 * @return String
	 */
	protected String getSequence(String sequenceName) {
		String seq = dao().getDialect().getSequence(dao(), sequenceName);
		return common.decodeTimestamp("yyyyMMddHHmmss", common.getCurrentTime()).concat(common.lpad(seq, 6, "0"));
	}
}
