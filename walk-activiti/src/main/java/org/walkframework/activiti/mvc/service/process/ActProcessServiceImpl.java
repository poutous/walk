package org.walkframework.activiti.mvc.service.process;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.walkframework.activiti.system.process.BackEntity;
import org.walkframework.activiti.system.process.CompleteEntity;
import org.walkframework.activiti.system.process.StartEntity;
import org.walkframework.activiti.system.process.TransferEntity;
import org.walkframework.base.mvc.service.base.BaseService;

/**
 * 流程流转服务
 * 
 * @author shf675
 * 
 */
@Service("actProcessServiceImpl")
public class ActProcessServiceImpl extends BaseService implements ActProcessService {
	
	@Resource(name = "actStartProcessService")
	ActStartProcessService actStartProcessService;
	
	@Resource(name = "actCompleteProcessService")
	ActCompleteProcessService actCompleteProcessService;
	
	@Resource(name = "actBackProcessService")
	ActBackProcessService actBackProcessService;
	
	@Resource(name = "actTransferProcessService")
	ActTransferProcessService actTransferProcessService;
	
	/**
	 * 启动流程
	 * 
	 * @param startEntity
	 * @return
	 */
	@Override
	public String doStart(final StartEntity startEntity) {
		return actStartProcessService.doStart(startEntity);
	}
	
	/**
	 * 提交流程
	 * 
	 * @param completeEntity
	 * @return
	 */
	@Override
	public void doComplete(final CompleteEntity completeEntity) {
		actCompleteProcessService.doComplete(completeEntity);
	}

	/**
	 * 回退流程。默认回退到上一节点，也可指定回退到某节点backEntity.setBackTaskDefKey(backTaskDefKey)
	 * 
	 * @param backEntity
	 * @return
	 */
	@Override
	public void doBack(BackEntity backEntity) {
		actBackProcessService.doBack(backEntity);
	}

	/**
	 * 转办任务
	 * 
	 * @param transferEntity
	 * @return
	 */
	@Override
	public void doTransfer(TransferEntity transferEntity) {
		actTransferProcessService.doTransfer(transferEntity);
	}
}
