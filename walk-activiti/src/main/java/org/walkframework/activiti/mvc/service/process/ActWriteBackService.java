package org.walkframework.activiti.mvc.service.process;
import javax.annotation.Resource;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.walkframework.activiti.mvc.entity.ActUdWorkorder;
import org.walkframework.activiti.system.process.WriteBackEntity;
import org.walkframework.base.mvc.dao.BaseSqlSessionDao;
import org.walkframework.base.mvc.service.base.BaseService;

import com.alibaba.fastjson.JSON;

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
	
	@Resource(name = "actCommonService")
	ActCommonService actCommonService;
	
	@Resource(name = "actProcessConfigService")
	ActProcessConfigService actProcessConfigService;
	
	@Autowired
	TaskService taskService;
	
	@Autowired
	RuntimeService runtimeService;

	
	/**
	 * 回写业务表
	 * 
	 * @return
	 */
	@SuppressWarnings({ "serial"})
	public void writeBackBusinessTable(final WriteBackEntity writeBackEntity) {
		//获取当前节点候选人
		final String[] taskCandidates = actCommonService.getTaskCandidates(writeBackEntity.getProcInstId());
		
		//查询流程表
		final ActUdWorkorder actUdWorkorder = dao.selectOne(new ActUdWorkorder(){{
			setOrderId(writeBackEntity.getBusinessId()).asCondition();
		}});
		int row = 0;
		if(actUdWorkorder == null){
			//插入工作流工单表。启动工作流时做插入操作
			row = dao.insert(new ActUdWorkorder(){{
				setOrderId(writeBackEntity.getBusinessId());
				setProcInstId(writeBackEntity.getProcInstId());
				setProcDefKey(writeBackEntity.getProcDefKey());
				setProcState(writeBackEntity.getProcState());
				setProcTaskDefKey(writeBackEntity.getProcTaskDefKey());
				setBusinessTable(writeBackEntity.getBusinessTable());
				setBusinessPrimaryKey(writeBackEntity.getBusinessIdPrimaryKey());
				setOrderDesc(writeBackEntity.getBusinessDesc());
				setSubmitor(writeBackEntity.getSubmitor());
				setSubmitTime(common.getCurrentTime());
				setCreateTime(common.getCurrentTime());
				setCreator(writeBackEntity.getOperator());
				if(taskCandidates != null){
					setCurrCandidateUsers(taskCandidates[0]);
					setCurrCandidateGroups(taskCandidates[1]);
				}
				
				if(log.isInfoEnabled()){
					log.info("插入工作流工单表成功！ " + JSON.toJSONString(actUdWorkorder));
				}
			}});
		} else {
			//从工作流工单表中获取业务表及主键名称
			writeBackEntity.setBusinessTable(actUdWorkorder.getBusinessTable());
			writeBackEntity.setBusinessIdPrimaryKey(actUdWorkorder.getBusinessPrimaryKey());
			
			//更新工作流工单表
			actUdWorkorder.setProcState(writeBackEntity.getProcState());
			actUdWorkorder.setProcTaskDefKey(writeBackEntity.getProcTaskDefKey());
			actUdWorkorder.setUpdateTime(common.getCurrentTime());
			actUdWorkorder.setUpdateStaffId(writeBackEntity.getOperator());
			if(taskCandidates != null){
				actUdWorkorder.setCurrCandidateUsers(taskCandidates[0]);
				actUdWorkorder.setCurrCandidateGroups(taskCandidates[1]);
			}
			actUdWorkorder.setProcInstId(actUdWorkorder.getProcInstId()).asCondition();
			row = dao.update(actUdWorkorder);
			if(row > 0 && log.isInfoEnabled()){
				log.info("更新工作流工单表成功！ " + JSON.toJSONString(actUdWorkorder));
			}
		}
		
		//回写业务表
		row = dao.update("ActProcessSQL.writeBackBusinessTable", writeBackEntity);
		if(row > 0 && log.isInfoEnabled()){
			log.info("回写业务表成功！ " + JSON.toJSONString(writeBackEntity));
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
}
