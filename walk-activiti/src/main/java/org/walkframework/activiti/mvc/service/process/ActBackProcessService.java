package org.walkframework.activiti.mvc.service.process;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.walkframework.activiti.mvc.entity.ActUdProcessLog;
import org.walkframework.activiti.mvc.service.process.cmd.JumpCmd;
import org.walkframework.activiti.system.constant.ProcessConstants;
import org.walkframework.activiti.system.process.BackEntity;
import org.walkframework.activiti.system.process.ProcessLog;
import org.walkframework.activiti.system.process.WriteBackEntity;
import org.walkframework.base.mvc.dao.BaseSqlSessionDao;
import org.walkframework.base.mvc.service.base.BaseService;

/**
 * 流程回退服务
 * 
 * @author shf675
 * 
 */
@Service("actBackProcessService")
public class ActBackProcessService extends BaseService {

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

	@Autowired
	IdentityService identityService;

	@Autowired
	RepositoryService repositoryService;

	@Autowired
	ProcessEngine processEngine;

	@Autowired
	HistoryService historyService;
	
	/**
	 * 回退流程到上个节点
	 * 
	 * @param startEntity
	 * @return
	 */
	@SuppressWarnings("serial")
	public void doBack(final BackEntity backEntity) {
		
		//1、回退参数校验
		preCheck(backEntity);
		
		//2、获取当前任务
		final Task currTask = taskService.createTaskQuery().processInstanceId(backEntity.getProcInstId()).singleResult();
		if(currTask == null){
			throw new ActivitiException("获取当前任务失败，可能原因为流程已结束，流程实例：" + backEntity.getProcInstId());
		}
		
		//3、获取回退目标任务节点
		String backTaskDefKey = backEntity.getBackTaskDefKey();
		if(StringUtils.isEmpty(backTaskDefKey)){
			//如果未设置目标回退节点，则查找上一任务节点
			backTaskDefKey = findPrevTaskDefinitionKey(backEntity.getProcInstId());
		}
		if(StringUtils.isEmpty(backTaskDefKey)){
			throw new ActivitiException("未获取到回退目标任务节点，流程实例：" + backEntity.getProcInstId());
		}
		
		//4、流程回退到目标任务节点
		CommandExecutor commandExecutor = ((RuntimeServiceImpl) runtimeService).getCommandExecutor();
		commandExecutor.execute(new JumpCmd(backEntity.getProcInstId(), backTaskDefKey));
		
		//5、获取回退的任务
		final Task backTask = taskService.createTaskQuery().processInstanceId(backEntity.getProcInstId()).singleResult();
		
		//6、回写业务表
		Map<String, Object> currTaskVars = taskService.getVariables(backTask.getId());
		final String businessId = currTaskVars.get(ProcessConstants.PROCESS_BUSINESSID) + "";
		actWriteBackService.writeBackBusinessTable(new WriteBackEntity(){{
			setBusinessId(businessId);
			setProcDefKey(backTask.getProcessDefinitionId().split(":")[0]);
			setProcInstId(backEntity.getProcInstId());
			setProcState(actProcessConfigService.getCurrTaskNodeConfig(backEntity.getProcInstId()).getNodeStateValue());
			setProcTaskDefKey(backTask.getTaskDefinitionKey());
			setOperator(backEntity.getBackUserId());
		}});
		
		//7、更新上一任务流程日志
		actProcessLogService.doUpdateProcessLog(new ProcessLog(){{
			setProcInstId(backEntity.getProcInstId());
			setDealStaffId(backEntity.getBackUserId());
			setDealInfo(StringUtils.isNotEmpty(backEntity.getBackRemark()) ? backEntity.getBackRemark() : "回退到" + backTask.getName() + "节点");
			setBackTag("1");
		}});
		
		//8、插入下一任务流程日志
		actProcessLogService.doInsertProcessLog(new ProcessLog(){{
			setOrderId(businessId);
			setProcInstId(backEntity.getProcInstId());
			setRemark(StringUtils.isNotEmpty(backEntity.getBackRemark()) ? backEntity.getBackRemark() : backTask.getName());
		}});
	}
	
	
	/**
	 * 回退流程参数校验
	 * 
	 * @param startEntity
	 */
	private void preCheck(BackEntity backEntity){
		if(StringUtils.isEmpty(backEntity.getProcInstId())) {
			throw new ActivitiException("参数：procInstId不能为空！");
		}
		if(StringUtils.isEmpty(backEntity.getBackUserId())) {
			throw new ActivitiException("参数：backUserId不能为空！");
		}
	}
	
	/**
	 * 根据流程实例去日志表找回退任务节点定义KEY
	 * 
	 * @param procInstId
	 * @return
	 */
	@SuppressWarnings("serial")
	private String findPrevTaskDefinitionKey(final String procInstId){
		String backTaskDefinitionKey = null;
		List<ActUdProcessLog> logList = dao.selectList(new ActUdProcessLog(){{
			setProcInstId(procInstId).asCondition();
			setCreateTime(null).asOrderByAsc();//按创建时间正序排列
		}});
		if(CollectionUtils.isEmpty(logList)){
			return null;
		}
		
		//倒序查找
		String currTaskDefinitionKey = null;
		int lastSameIndex = logList.size() - 1;
		for (int i = logList.size() - 1; i >=0 ; i--) {
			ActUdProcessLog pLog = logList.get(i);
			//先找到当前节点
			if(i == logList.size() - 1 && StringUtils.isEmpty(pLog.getEndState())){
				currTaskDefinitionKey = pLog.getBeginTaskDefKey();
				continue;
			}
			
			//先从历史里找与自己相同正常状态的节点且是最后一个出现的节点，记录索引值
			if("0".equals(pLog.getBackTag()) && currTaskDefinitionKey.equals(pLog.getBeginTaskDefKey())){
				lastSameIndex = i;
			}
		}
		if(lastSameIndex > 0){
			backTaskDefinitionKey = logList.get(lastSameIndex - 1).getBeginTaskDefKey();
		}
		return backTaskDefinitionKey;
	}
}
