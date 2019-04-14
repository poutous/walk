package org.walkframework.activiti.mvc.service.process;

import java.util.Arrays;

import javax.annotation.Resource;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.walkframework.activiti.mvc.entity.ActUdWorkorder;
import org.walkframework.activiti.system.process.TransferEntity;
import org.walkframework.base.mvc.service.base.BaseService;

/**
 * 转办流程服务
 * 
 * @author shf675
 * 
 */
@Service("actTransferProcessService")
public class ActTransferProcessService extends BaseService {

	@Resource(name = "actProcessLogService")
	ActProcessLogService actProcessLogService;
	
	@Resource(name = "actWriteBackService")
	ActWriteBackService actWriteBackService;

	@Autowired
	TaskService taskService;

	/**
	 * 转办任务
	 * 
	 * @param completeEntity
	 * @return
	 */
	@SuppressWarnings("serial")
	public void doTransfer(final TransferEntity transferEntity) {
		
		// 1、转办参数校验
		preCheck(transferEntity);

		// 2、获取当前任务
		final Task currTask = taskService.createTaskQuery().processInstanceId(transferEntity.getProcInstId()).singleResult();
		if (currTask == null) {
			throw new ActivitiException("流程实例[" + transferEntity.getProcInstId() + "]不存在或流程已经结束！");
		}

		// 3、从处理候选人当中删除自己
		taskService.deleteCandidateUser(currTask.getId(), transferEntity.getCurrUserId());

		// 4、目标转办人到候选处理人或组中
		String defaultRemark = transferEntity.getCurrUserId() + "将任务转办给";
		if (transferEntity.getTransferUserIds() != null && transferEntity.getTransferUserIds().length > 0) {
			for (String userId : transferEntity.getTransferUserIds()) {
				taskService.addCandidateUser(currTask.getId(), userId);
			}
			defaultRemark += " 用户列表：" + Arrays.toString(transferEntity.getTransferUserIds());
		}
		if (transferEntity.getTransferGroups() != null && transferEntity.getTransferGroups().length > 0) {
			for (String group : transferEntity.getTransferGroups()) {
				taskService.addCandidateGroup(currTask.getId(), group);
			}
			defaultRemark += " 组列表：" + Arrays.toString(transferEntity.getTransferGroups());
		}

		// 5、记录转办日志
		final String transferRemark = StringUtils.isNotEmpty(transferEntity.getTransferRemark()) ? transferEntity.getTransferRemark() : defaultRemark;
		actProcessLogService.doUpdateProcessLogRemark(transferEntity.getProcInstId(), transferRemark);
		
		
		//6、修改工作流工单表
		actWriteBackService.updateActUdWorkorder(new ActUdWorkorder(){{
			setProcInstId(transferEntity.getProcInstId()).asCondition();
			setUpdateStaffId(transferEntity.getCurrUserId());
			setUpdateTime(common.getCurrentTime());
			setRemark(transferRemark);
		}});
	}

	/**
	 * 转办参数校验
	 * 
	 * @param startEntity
	 */
	private void preCheck(TransferEntity transferEntity) {
		if (StringUtils.isEmpty(transferEntity.getProcInstId())) {
			throw new ActivitiException("参数：procInstId不能为空！");
		}
		if (StringUtils.isEmpty(transferEntity.getCurrUserId())) {
			throw new ActivitiException("参数：currUserId不能为空！");
		}
		boolean userIdsEmpty = transferEntity.getTransferUserIds() == null || transferEntity.getTransferUserIds().length == 0;
		boolean groupsEmpty = transferEntity.getTransferGroups() == null || transferEntity.getTransferGroups().length == 0;
		if (userIdsEmpty && groupsEmpty) {
			throw new ActivitiException("参数：transferUserIds和transferGroups不能都为空！");
		}
	}
}
