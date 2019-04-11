package org.walkframework.activiti.mvc.service.process;
import java.util.List;

import javax.annotation.Resource;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.walkframework.activiti.mvc.entity.ActUdWorkorder;
import org.walkframework.activiti.system.process.WriteBackEntity;
import org.walkframework.base.mvc.dao.BaseSqlSessionDao;
import org.walkframework.base.mvc.service.base.BaseService;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 回写业务表服务
 * 
 * @author shf675
 * 
 */
@Service("actWriteBackService")
public class ActWriteBackService extends BaseService {
	
	@Resource(name = "sqlSessionDao")
	private BaseSqlSessionDao dao;
	
	@Resource(name = "actProcessConfigService")
	ActProcessConfigService actProcessConfigService;
	
	@Autowired
	TaskService taskService;
	
	/**
	 * 回写业务表
	 * 
	 * @return
	 */
	public void writeBackBusinessTable(final WriteBackEntity writeBackEntity) {
		//1、获取业务表信息
		JSONObject customJson = actProcessConfigService.getCustomJson(writeBackEntity.getProcInstId());
		writeBackEntity.setBusinessTable(customJson.getString("businessTable"));
		writeBackEntity.setBusinessIdPrimaryKey(customJson.getString("businessIdPrimaryKey"));
		
		//2、回写业务表
		int row = dao.update("ActProcessSQL.writeBackBusinessTable", writeBackEntity);
		if(row > 0 && log.isInfoEnabled()){
			log.info("回写业务表成功！ " + JSON.toJSONString(writeBackEntity));
		}
		
		//3、回写工作流工单表
		final String[] taskCandidates = getTaskCandidates(writeBackEntity.getProcInstId());
		ActUdWorkorder actUdWorkorder = new ActUdWorkorder();
		actUdWorkorder.setOrderId(writeBackEntity.getBusinessId()).asCondition();
		actUdWorkorder.setProcInstId(writeBackEntity.getProcInstId());
		actUdWorkorder.setProcDefKey(writeBackEntity.getProcDefKey());
		actUdWorkorder.setProcState(writeBackEntity.getProcState());
		actUdWorkorder.setProcTaskDefKey(writeBackEntity.getProcTaskDefKey());
		actUdWorkorder.setCurrCandidateUsers(taskCandidates[0]);
		actUdWorkorder.setCurrCandidateGroups(taskCandidates[1]);
		actUdWorkorder.setUpdateTime(common.getCurrentTime());
		actUdWorkorder.setUpdateStaffId(writeBackEntity.getOperator());
		row = dao.update(actUdWorkorder);
		if(row > 0 && log.isInfoEnabled()){
			log.info("回写工作流工单表成功！ " + JSON.toJSONString(actUdWorkorder));
		}
	}
	
	/**
	 * 更新工作流工单表
	 * 
	 * @param actUdWorkorder
	 */
	public void updateActUdWorkorder(ActUdWorkorder actUdWorkorder) {
		dao.update(actUdWorkorder);
	}
	
	/**
	 * 获取当前任务候选人列表、候选组列表
	 * 
	 * @param taskId
	 * @return
	 */
	private String[] getTaskCandidates(String procInstId) {
		Task currTask = taskService.createTaskQuery().processInstanceId(procInstId).singleResult();
		List<IdentityLink> identityLinkList = taskService.getIdentityLinksForTask(currTask.getId());
		StringBuilder candidateUsers = new StringBuilder();
		StringBuilder candidateGroups = new StringBuilder();
		if(CollectionUtils.isNotEmpty(identityLinkList)){
			for (IdentityLink identityLink : identityLinkList) {
				if(StringUtils.isNotEmpty(identityLink.getUserId()) && !"null".equals(identityLink.getUserId())){
					candidateUsers.append(identityLink.getUserId()).append(",");
				}
				if(StringUtils.isNotEmpty(identityLink.getGroupId()) && !"null".equals(identityLink.getGroupId())){
					candidateGroups.append(identityLink.getGroupId()).append(",");
				}
			}
			if(candidateUsers.length() > 0){
				candidateUsers.deleteCharAt(candidateUsers.length() - 1);
			}
			if(candidateGroups.length() > 0){
				candidateGroups.deleteCharAt(candidateGroups.length() - 1);
			}
		}
		return new String[]{candidateUsers.length() > 0 ? candidateUsers.toString() : "" , candidateGroups.length() > 0 ? candidateGroups.toString() : ""};
	}
}
