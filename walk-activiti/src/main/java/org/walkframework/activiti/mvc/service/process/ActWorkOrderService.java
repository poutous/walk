package org.walkframework.activiti.mvc.service.process;
import javax.annotation.Resource;

import org.activiti.engine.ActivitiException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.walkframework.activiti.mvc.entity.ActUdWorkorder;
import org.walkframework.activiti.system.constant.ProcessConstants;
import org.walkframework.base.mvc.dao.BaseSqlSessionDao;
import org.walkframework.base.mvc.service.base.BaseService;

/**
 * 工作流工单服务
 * 
 * @author shf675
 */
@Service("actWorkOrderService")
public class ActWorkOrderService extends BaseService {
	
	@Resource(name = "sqlSessionDao")
	private BaseSqlSessionDao dao;

	/**
	 * 根据工单ID获取工单信息
	 * 
	 * @return
	 */
	@SuppressWarnings("serial")
	public ActUdWorkorder queryWorkOrderById(final String orderId) {
		return dao.selectOne(new ActUdWorkorder(){{
			setOrderId(orderId).asCondition();
		}});
	}
	
	/**
	 * 根据流程实例ID获取工单信息
	 * 
	 * @return
	 */
	@SuppressWarnings("serial")
	public ActUdWorkorder queryWorkOrderByProcInstId(final String procInstId) {
		return dao.selectOne(new ActUdWorkorder(){{
			setProcInstId(procInstId).asCondition();
		}});
	}
	
	/**
	 * 插入工单表
	 * 
	 * @param orderInfo
	 * @return
	 */
	public String insertWorkOrder(ActUdWorkorder orderInfo) {
		if(StringUtils.isNotEmpty(orderInfo.getOrderId())){
			throw new ActivitiException("orderId不允许设置值，orderId将在调用此方法后返回！");
		}
		String orderId = dao.getSequenceL16(ProcessConstants.SEQ_ORDER_ID);
		orderInfo.setOrderId(orderId);
		orderInfo.setCreateTime(common.getCurrentTime());
		dao.insert(orderInfo);
		return orderId;
	}
}
