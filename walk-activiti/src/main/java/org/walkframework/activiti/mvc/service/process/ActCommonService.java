package org.walkframework.activiti.mvc.service.process;
import java.util.List;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.walkframework.base.mvc.service.base.BaseService;

/**
 * 流程公共服务
 * 
 * @author shf675
 * 
 */
@Service("actCommonService")
public class ActCommonService extends BaseService {
	
	@Autowired
	TaskService taskService;
	
	/**
	 * 获取当前任务候选人列表、候选组列表
	 * 
	 * @param taskId
	 * @return
	 */
	public String[] getTaskCandidates(String procInstId) {
		Task currTask = taskService.createTaskQuery().processInstanceId(procInstId).singleResult();
		if(currTask == null){
			return null;
		}
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
