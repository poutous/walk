package org.walkframework.activiti.mvc.service.process;
import java.util.List;

import javax.annotation.Resource;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.walkframework.activiti.mvc.entity.ActUdProcessLog;
import org.walkframework.activiti.system.constant.ProcessConstants;
import org.walkframework.activiti.system.process.ProcessLog;
import org.walkframework.base.mvc.dao.BaseSqlSessionDao;
import org.walkframework.base.mvc.service.base.BaseService;
import org.walkframework.shiro.authc.principal.BasePrincipal;

/**
 * 流程日志服务
 * 
 * @author shf675
 * 
 */
@Service("actProcessLogService")
public class ActProcessLogService extends BaseService {
	
	@Resource(name = "sqlSessionDao")
	private BaseSqlSessionDao dao;
	
	@Resource(name = "actCommonService")
	ActCommonService actCommonService;
	
	@Resource(name = "actProcessConfigService")
	ActProcessConfigService actProcessConfigService;
	
	@Autowired
	TaskService taskService;
	 
	@Autowired
	RepositoryService repositoryService;
	 
	/**
	 * 查询流程操作日志
	 * @param processInstId 流程实例ID
	 * @return
	 */
	@SuppressWarnings("serial")
	public List<ActUdProcessLog> queryProcessLogList(final String procInstId) {
		return dao.selectList(new ActUdProcessLog(){{
			setProcInstId(procInstId).asCondition();
			setCreateTime(null).asOrderByDesc();//按创建时间倒序排列
		}});
	}
	
	/**
	 * 插入流程流转日志
	 * 
	 * @param processLog
	 */
	public String doInsertProcessLog(ProcessLog processLog) {
		//获取当前节点候选人
		final String[] taskCandidates = actCommonService.getTaskCandidates(processLog.getProcInstId());
		
		Task task = taskService.createTaskQuery().processInstanceId(processLog.getProcInstId()).singleResult();
		ProcessDefinition processDefinition = repositoryService.getProcessDefinition(task.getProcessDefinitionId());
		ActUdProcessLog actUdProcessLog = new ActUdProcessLog();
		actUdProcessLog.setProcInstId(processLog.getProcInstId());
		BeanUtils.copyProperties(processLog, actUdProcessLog);
		
		String procState = actProcessConfigService.getCurrTaskNodeConfig(processLog.getProcInstId()).getNodeStateValue();
		String createStaffId = ProcessConstants.SYSTEM_AUTO_STAFF_ID;
		String userId = ((BasePrincipal)SecurityUtils.getSubject().getPrincipal()).getUserId();
		if(StringUtils.isNotEmpty(userId)){
			createStaffId = userId;
		}
		
		String logId = dao.getSequenceL20(ProcessConstants.SEQ_LOG_ID);
		actUdProcessLog.setLogId(logId);
		actUdProcessLog.setOrderId(processLog.getOrderId());
		actUdProcessLog.setCreateTime(common.getCurrentTime());
		actUdProcessLog.setCreateStaffId(createStaffId);
		actUdProcessLog.setProcDefName(processDefinition.getName());
		actUdProcessLog.setProcDefKey(processDefinition.getKey());
		actUdProcessLog.setProcDevVer(String.valueOf(processDefinition.getVersion()));
		actUdProcessLog.setProcState(procState);
		actUdProcessLog.setRemark(processLog.getRemark());
		actUdProcessLog.setBeginState(procState);
		actUdProcessLog.setBeginTaskDefKey(task.getTaskDefinitionKey());
		actUdProcessLog.setBeginTaskDefName(task.getName());
		if(taskCandidates != null){
			actUdProcessLog.setCandidateUsers(taskCandidates[0]);
			actUdProcessLog.setCandidateGroups(taskCandidates[1]);
		}
		
		dao.insert(actUdProcessLog);
		if(log.isInfoEnabled()){
			log.info("插入流程日志表成功！日志流水：" + logId);
		}
		return logId;
	}
	
	/**
	 * 更新流程流转日志
	 * 
	 * @param processLog
	 */
	@SuppressWarnings("serial")
	public int doUpdateProcessLog(final ProcessLog processLog) {
		final ActUdProcessLog actUdProcessLog = dao.selectOne(new ActUdProcessLog(){{
			setProcInstId(processLog.getProcInstId()).asCondition();
			setEndState(null).asCondition();
		}});
		
		int rows = 0;
		if(actUdProcessLog != null){
			rows = dao.update(new ActUdProcessLog(){{
				setLogId(actUdProcessLog.getLogId()).asCondition();
				setDealStaffId(processLog.getDealStaffId());
				setDealInfo(processLog.getDealInfo());
				setDealTime(common.getCurrentTime());
				
				//获取任务为空表示流程已经结束了
				Task task = taskService.createTaskQuery().processInstanceId(processLog.getProcInstId()).singleResult();
				if(task == null){
					setEndTaskDefKey(ProcessConstants.END_NODE_KEY);
					setEndTaskDefName("结束");
					setEndState(ProcessConstants.END_NODE_STATE);
				} else {
					setEndState(actProcessConfigService.getCurrTaskNodeConfig(processLog.getProcInstId()).getNodeStateValue());
					setEndTaskDefKey(task.getTaskDefinitionKey());
					setEndTaskDefName(task.getName());
				}
				
				//回退标记
				if(StringUtils.isNotEmpty(processLog.getBackTag())) {
					setBackTag(processLog.getBackTag());
				}
			}});
		}
		if(log.isInfoEnabled()){
			if(rows > 0) {
				log.info("更新流程日志表成功！日志流水：" + actUdProcessLog.getLogId());
			}
		}
		return rows;
	}
	
	/**
	 * 更新流程流转备注
	 * 
	 * @param processLog
	 */
	@SuppressWarnings("serial")
	public int doUpdateProcessLogInfo(final String procInstId, final String candidateUsers, final String candidateGroups, final String remark) {
		return dao.update(new ActUdProcessLog(){{
			setProcInstId(procInstId).asCondition();
			setEndState(null).asCondition();
			setCandidateUsers(candidateUsers);
			setCandidateGroups(candidateGroups);
			setRemark(remark);
		}});
	}
}
