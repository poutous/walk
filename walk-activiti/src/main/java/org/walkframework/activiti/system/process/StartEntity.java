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
	 * 工单发起人
	 */	
	private String submitor;
	
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
	public StartEntity(String procDefKey, String businessId, String submitor) {
		this.procDefKey = procDefKey;
		this.businessId = businessId;
		this.submitor = submitor;
	}
	
	public String getSubmitor() {
		return submitor;
	}

	public void setSubmitor(String submitor) {
		this.submitor = submitor;
	}

	public String getProcDefKey() {
		return procDefKey;
	}


	public String getBusinessId() {
		return businessId;
	}
}
