package org.walkframework.activiti.mvc.service.process;
import org.walkframework.activiti.system.process.BackEntity;
import org.walkframework.activiti.system.process.CompleteEntity;
import org.walkframework.activiti.system.process.StartEntity;
import org.walkframework.activiti.system.process.TransferEntity;

/**
 * 流程流转服务接口
 * 
 * @author shf675
 * 
 */
public interface ActProcessService {
	
	/**
	 * 启动流程
	 * 
	 * @param startEntity
	 * @return
	 */
	public String doStart(final StartEntity startEntity);
	
	/**
	 * 提交流程
	 * 
	 * @param completeEntity
	 * @return
	 */
	public void doComplete(final CompleteEntity completeEntity);
	
	/**
	 * 回退流程
	 * 
	 * @param backEntity
	 * @return
	 */
	public void doBack(final BackEntity backEntity);
	
	/**
	 * 转办任务
	 * 
	 * @param transferEntity
	 * @return
	 */
	public void doTransfer(final TransferEntity transferEntity);
}
