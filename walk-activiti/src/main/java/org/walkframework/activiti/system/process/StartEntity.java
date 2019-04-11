package org.walkframework.activiti.system.process;


/**
 * 启动流程入参
 * 
 * @author shf675
 */
public class StartEntity {

	/**
	 * 流程标识 （流程定义的key）
	 */
	private String procDefKey;
	
	/**
	 * 业务ID
	 */
	private String businessId;
	
	/**
	 * 构造函数
	 * 
	 * @param procDefKey
	 *            流程定义KEY
	 * @param businessTable
	 *            业务表名
	 * @param businessId
	 *            业务ID
	 */
	public StartEntity(String procDefKey, String businessId) {
		this.procDefKey = procDefKey;
		this.businessId = businessId;
	}

	public String getProcDefKey() {
		return procDefKey;
	}


	public String getBusinessId() {
		return businessId;
	}
}
