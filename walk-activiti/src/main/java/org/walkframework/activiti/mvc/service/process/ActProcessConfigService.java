package org.walkframework.activiti.mvc.service.process;

import java.util.List;

import javax.annotation.Resource;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.form.DefaultTaskFormHandler;
import org.activiti.engine.impl.form.FormPropertyHandler;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.walkframework.activiti.system.process.NodeConfigEntity;
import org.walkframework.base.mvc.dao.BaseSqlSessionDao;
import org.walkframework.base.mvc.service.base.BaseService;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 流程配置服务
 * 
 * @author shf675
 * 
 */
@Service("actProcessConfigService")
public class ActProcessConfigService extends BaseService {

	@Resource(name = "sqlSessionDao")
	BaseSqlSessionDao dao;

	@Autowired
	RuntimeService runtimeService;

	@Autowired
	TaskService taskService;

	@Autowired
	IdentityService identityService;

	@Autowired
	RepositoryService repositoryService;

	@Autowired
	ProcessEngine processEngine;

	@Autowired
	HistoryService historyService;
	
	/**
	 * 根据流程实例获取模型自定义JSON参数
	 * 
	 * @param procInstId
	 * @return
	 */
	public JSONObject getCustomJson(String deploymentId) {
		Model modelData = repositoryService.createModelQuery().deploymentId(deploymentId).singleResult();
		if(StringUtils.isNotEmpty(modelData.getMetaInfo())){
			JSONObject metaInfo = JSON.parseObject(modelData.getMetaInfo());
			return metaInfo.getJSONObject("customJson");
		}
		return null;
	}
	
	/**
	 * 获取当前任务节点定义信息
	 * 
	 * @param taskId
	 * @return
	 */
	public NodeConfigEntity getCurrTaskNodeConfig(String procInstId) {
		Task task = taskService.createTaskQuery().processInstanceId(procInstId).singleResult();
		final ProcessDefinition processDefinition = repositoryService.getProcessDefinition(task.getProcessDefinitionId());
		String processDefinitionId = task.getProcessDefinitionId(); // 获取流程定义id

		ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) processEngine.getRepositoryService().getProcessDefinition(processDefinitionId);
		ActivityImpl activityImpl = processDefinitionEntity.findActivity(task.getTaskDefinitionKey()); // 根据活动id获取活动实例
		TaskDefinition taskDef = (TaskDefinition) activityImpl.getProperties().get("taskDefinition");
		NodeConfigEntity config = new NodeConfigEntity();
		config.setProcDefId(processDefinitionId);
		config.setProcDefKey(processDefinition.getKey());
		config.setProcDevVer(processDefinition.getVersion() + "");
		config.setNodeKey(task.getTaskDefinitionKey());
		config.setNodeName(task.getName());
		config.setNodeStateValue(getFormPropertyValue(taskDef, "nodeStateValue"));
		config.setPageUrl(taskDef.getFormKeyExpression() == null ? null : taskDef.getFormKeyExpression().getExpressionText());
		return config;
	}

	/**
	 * 获取表单属性值
	 * 
	 * @param taskDef
	 * @param propertyName
	 * @return
	 */
	private String getFormPropertyValue(TaskDefinition taskDef, String propertyName) {
		DefaultTaskFormHandler fh = (DefaultTaskFormHandler) taskDef.getTaskFormHandler();
		List<FormPropertyHandler> formPropertyHandlers = fh == null ? null : (List<FormPropertyHandler>) fh.getFormPropertyHandlers();
		if (CollectionUtils.isNotEmpty(formPropertyHandlers)) {
			for (FormPropertyHandler formPropertyHandler : formPropertyHandlers) {
				if (propertyName.equals(formPropertyHandler.getId())) {
					return formPropertyHandler.getVariableExpression() == null ? null : formPropertyHandler.getVariableExpression().getExpressionText();
				}
			}
		}
		return null;
	}
}
