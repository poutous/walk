package org.walkframework.activiti.mvc.service.process;
import java.util.Map;

import javax.annotation.Resource;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.walkframework.activiti.mvc.entity.ActUdWorkorder;
import org.walkframework.activiti.system.constant.ProcessConstants;
import org.walkframework.activiti.system.process.NodeConfigEntity;
import org.walkframework.activiti.system.process.ProcessLog;
import org.walkframework.activiti.system.process.StartEntity;
import org.walkframework.activiti.system.process.WriteBackEntity;
import org.walkframework.base.mvc.dao.BaseSqlSessionDao;
import org.walkframework.base.mvc.service.base.BaseService;

import com.google.common.collect.Maps;

/**
 * 启动流程服务
 * 
 * @author shf675
 * 
 */
@Service("actStartProcessService")
public class ActStartProcessService extends BaseService {
	
	@Resource(name = "sqlSessionDao")
	BaseSqlSessionDao dao;
	
	@Resource(name = "actProcessConfigService")
	ActProcessConfigService actProcessConfigService;
	
	@Resource(name = "actProcessLogService")
	ActProcessLogService actProcessLogService;
	
	@Resource(name = "actWriteBackService")
	ActWriteBackService actWriteBackService;
	
	@Resource(name = "actWorkOrderService")
	ActWorkOrderService actWorkOrderService;
	
	@Autowired
	RuntimeService runtimeService;
	 
	@Autowired
	TaskService taskService;
	 
	/**
	 * 启动流程
	 * 
	 * @param startEntity
	 * @return
	 */
	@SuppressWarnings("serial")
	public String doStart(final StartEntity startEntity) {
		//1、启动流程校验
		preCheck(startEntity);
		
		//2、启动流程
		String processDefinitionKey = startEntity.getProcDefKey();
		String businessKey = ProcessConstants.PROCESS_WORKORDERTABLE + ":" + startEntity.getBusinessId();
		Map<String, Object> variables = getVariables(startEntity);
		final ProcessInstance procIns = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
		
		//3、获取流程实例ID
		final String procInstId = procIns.getProcessInstanceId();
		final Task task = taskService.createTaskQuery().processInstanceId(procInstId).singleResult();
		final String msg = String.format("启动流程：流程定义[%s]，流程实例[%s]。进入%s任务节点。", startEntity.getProcDefKey(), procInstId, task.getName());
		if(log.isInfoEnabled()){
			log.info(msg);
		}
		
		//4、回写业务表
		final NodeConfigEntity config = actProcessConfigService.getCurrTaskNodeConfig(procInstId);
		actWriteBackService.writeBackBusinessTable(new WriteBackEntity(){{
			setBusinessId(startEntity.getBusinessId());
			setBusinessTable(config.getBusinessTable());
			setBusinessIdPrimaryKey(config.getBusinessIdPrimaryKey());
			setProcDefKey(startEntity.getProcDefKey());
			setProcInstId(procInstId);
			setProcState(config.getNodeStateValue());
			setProcTaskDefKey(task.getTaskDefinitionKey());
			setSubmitor(startEntity.getSubmitor());
			setOperator(ProcessConstants.SYSTEM_AUTO_STAFF_ID);
			setBusinessDesc(startEntity.getBusinessDesc());
		}});
		
		//5、插入流程日志
		actProcessLogService.doInsertProcessLog(new ProcessLog(){{
			setOrderId(startEntity.getBusinessId());
			setProcInstId(procInstId);
			setRemark(msg);
		}});
		return procInstId;
	}
	
	/**
	 * 启动流程校验
	 * 
	 * @param startEntity
	 */
	private void preCheck(final StartEntity startEntity){
		if(StringUtils.isEmpty(startEntity.getProcDefKey())) {
			throw new ActivitiException("参数：procDefKey不能为空！");
		}
		if(StringUtils.isEmpty(startEntity.getBusinessId())) {
			throw new ActivitiException("参数：businessId不能为空！");
		}
		if(StringUtils.isEmpty(startEntity.getSubmitor())) {
			throw new ActivitiException("参数：submitor不能为空！");
		}
		
		//校验是否已经启动流程了
		ActUdWorkorder orderInfo = actWorkOrderService.queryWorkOrderById(startEntity.getBusinessId());
		if(orderInfo != null && StringUtils.isNotEmpty(orderInfo.getProcInstId())){
			throw new ActivitiException("工单：" + startEntity.getBusinessId() + "已经启动流程了，不能重复启动！ ");
		}
	}
	
	/**
	 * 获取流程变量
	 * 
	 * @param completeEntity
	 */
	private Map<String, Object> getVariables(StartEntity startEntity){
		Map<String, Object> variables = Maps.newHashMap();
		//设置业务表业务ID
		variables.put(ProcessConstants.PROCESS_BUSINESSID, startEntity.getBusinessId());
		
		//设置工单发起人
		variables.put(ProcessConstants.PROCESS_ASSIGN_SUBMITOR, startEntity.getSubmitor());
		return variables;
	}
}
