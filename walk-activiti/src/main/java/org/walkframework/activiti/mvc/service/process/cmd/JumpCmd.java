package org.walkframework.activiti.mvc.service.process.cmd;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;

public class JumpCmd implements Command<ExecutionEntity> {

	private String processInstanceId;
	private String activityId;
	public static final String REASION_DELETE = "deleted";

	public JumpCmd(String processInstanceId, String activityId) {
		this.processInstanceId = processInstanceId;
		this.activityId = activityId;
	}

	@Override
	public ExecutionEntity execute(CommandContext commandContext) {
		ExecutionEntity executionEntity = commandContext.getExecutionEntityManager().findExecutionById(processInstanceId);
		executionEntity.destroyScope(REASION_DELETE);
		ProcessDefinitionImpl processDefinition = executionEntity.getProcessDefinition();
		ActivityImpl activity = processDefinition.findActivity(activityId);
		try {
			executionEntity.executeActivity(activity);
		} catch (Exception e) {
			throw new ActivitiException("任务节点跳转失败，目标任务节点不存在：", e);
		}
		return executionEntity;
	}

}