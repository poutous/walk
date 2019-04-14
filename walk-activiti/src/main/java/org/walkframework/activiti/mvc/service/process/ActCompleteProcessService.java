package org.walkframework.activiti.mvc.service.process;
import java.util.Map;

import javax.annotation.Resource;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.walkframework.activiti.mvc.entity.ActUdWorkorder;
import org.walkframework.activiti.system.constant.ProcessConstants;
import org.walkframework.activiti.system.process.CompleteEntity;
import org.walkframework.activiti.system.process.ProcessLog;
import org.walkframework.activiti.system.process.WriteBackEntity;
import org.walkframework.base.mvc.dao.BaseSqlSessionDao;
import org.walkframework.base.mvc.service.base.BaseService;

import com.google.common.collect.Maps;

/**
 * 提交流程服务
 * 
 * @author shf675
 * 
 */
@Service("actCompleteProcessService")
public class ActCompleteProcessService extends BaseService {
	
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
	 * 提交流程
	 * 
	 * @param completeEntity
	 * @return
	 */
	@SuppressWarnings({ "serial"})
	public void doComplete(final CompleteEntity completeEntity) {
		//1、基础参数校验
		preCheck(completeEntity);
		
		//2、获取当前任务
		final Task currTask = taskService.createTaskQuery().processInstanceId(completeEntity.getProcInstId()).singleResult();
		if(currTask == null){
			throw new ActivitiException("流程实例[" + completeEntity.getProcInstId() + "]不存在或流程已经结束！");
		}
		//获取当前任务业务表信息
		Map<String, Object> currTaskVars = taskService.getVariables(currTask.getId());
		final String businessId = currTaskVars.get(ProcessConstants.PROCESS_BUSINESSID) + "";
		
		//3、当前处理人签收任务，表示本任务节点是当前人处理的
		taskService.claim(currTask.getId(), completeEntity.getCompleteUserId());

		//4、完成当前节点，进入下一节点
		Map<String, Object> variables = getVariables(completeEntity);
		taskService.complete(currTask.getId(), variables);
		
		//5、获取下一节点任务并进行相关处理
		final Task nextTask = taskService.createTaskQuery().processInstanceId(completeEntity.getProcInstId()).singleResult();
		if (nextTask != null) {
			//设置下一节点任务处理人或处理组
			//候选人
			String[] toUsers = completeEntity.getToUsers();
			if(toUsers != null && toUsers.length > 0){
				for (String userId : toUsers) {
					taskService.addCandidateUser(nextTask.getId(), userId);
				}
			}
			//候选组
			String[] toGroups = completeEntity.getToGroups();
			if(toGroups != null && toGroups.length > 0){
				for (String groupId : toGroups) {
					taskService.addCandidateGroup(nextTask.getId(), groupId);
				}
			}
			
			//回写业务表
			actWriteBackService.writeBackBusinessTable(new WriteBackEntity(){{
				setBusinessId(businessId);
				setProcDefKey(nextTask.getProcessDefinitionId().split(":")[0]);
				setProcInstId(completeEntity.getProcInstId());
				setProcState(actProcessConfigService.getCurrTaskNodeConfig(completeEntity.getProcInstId()).getNodeStateValue());
				setProcTaskDefKey(nextTask.getTaskDefinitionKey());
				setOperator(completeEntity.getCompleteUserId());
			}});
			
			//更新上一任务流程日志
			actProcessLogService.doUpdateProcessLog(new ProcessLog(){{
				setProcInstId(completeEntity.getProcInstId());
				setDealStaffId(completeEntity.getCompleteUserId());
				setDealInfo(StringUtils.isNotEmpty(completeEntity.getCompleteRemark()) ? completeEntity.getCompleteRemark() : currTask.getName());
			}});
			
			//插入下一任务流程日志
			actProcessLogService.doInsertProcessLog(new ProcessLog(){{
				setOrderId(businessId);
				setProcInstId(completeEntity.getProcInstId());
				setRemark(StringUtils.isNotEmpty(completeEntity.getCompleteRemark()) ? completeEntity.getCompleteRemark() : "进入" + nextTask.getName() + "任务节点");
			}});
		} 
		//下一节点任务为空表示流程已经结束了
		else {
			//回写业务表
			actWriteBackService.writeBackBusinessTable(new WriteBackEntity(){{
				setBusinessId(businessId);
				setProcDefKey(currTask.getProcessDefinitionId().split(":")[0]);
				setProcInstId(completeEntity.getProcInstId());
				setProcState(ProcessConstants.END_NODE_STATE);
				setProcTaskDefKey(ProcessConstants.END_NODE_KEY);
				setOperator(completeEntity.getCompleteUserId());
			}});
			
			//更新流程日志
			actProcessLogService.doUpdateProcessLog(new ProcessLog(){{
				setProcInstId(completeEntity.getProcInstId());
				setDealStaffId(completeEntity.getCompleteUserId());
				setDealInfo(StringUtils.isNotEmpty(completeEntity.getCompleteRemark()) ? completeEntity.getCompleteRemark() : "流程结束");
			}});
		}
	}
	
	/**
	 * 提交流程基础参数校验
	 * 
	 * @param startEntity
	 */
	private void preCheck(CompleteEntity completeEntity){
		if(StringUtils.isEmpty(completeEntity.getProcInstId())) {
			throw new ActivitiException("参数：procInstId不能为空！");
		}
		if(StringUtils.isEmpty(completeEntity.getCompleteUserId())) {
			throw new ActivitiException("参数：userId不能为空！");
		}
	}
	
	/**
	 * 获取流程变量
	 * 
	 * @param completeEntity
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, Object> getVariables(CompleteEntity completeEntity){
		Map<String, Object> variables = Maps.newHashMap();
		
		//网关参数设置
		Map gatewayVariables = new BeanMap(completeEntity.getGateway());
		variables.putAll(gatewayVariables);
		
		//设置工单发起人
		ActUdWorkorder orderInfo = actWorkOrderService.queryWorkOrderByProcInstId(completeEntity.getProcInstId());
		if(orderInfo == null){
			throw new ActivitiException("不存在的流程实例：" + completeEntity.getProcInstId());
		}
		variables.put(ProcessConstants.PROCESS_ASSIGN_SUBMITOR, orderInfo.getSubmitor());
		return variables;
	}
}
