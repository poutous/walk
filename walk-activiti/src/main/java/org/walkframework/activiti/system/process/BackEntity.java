package org.walkframework.activiti.system.process;


/**
 * 回退流程入参
 * 
 * @author shf675
 */
public class BackEntity {

	/**
	 * 流程实例ID
	 */
	private String procInstId;

	/**
	 * 回退人ID
	 */
	private String backUserId;

	/**
	 * 回退备注
	 */
	private String backRemark;
	
	/**
	 * 指定回退目标任务节点定义key
	 * 
	 * 非必填。不填默认是回退到上一节点
	 */
	private String backTaskDefKey;
	
	public BackEntity(String procInstId, String backUserId){
		this.procInstId = procInstId;
		this.backUserId = backUserId;
	}
	
	public BackEntity(String procInstId, String backUserId, String backRemark){
		this.procInstId = procInstId;
		this.backUserId = backUserId;
		this.backRemark = backRemark;
	}
	
	public String getProcInstId() {
		return procInstId;
	}

	public String getBackUserId() {
		return backUserId;
	}

	public String getBackRemark() {
		return backRemark;
	}
	
	public BackEntity setBackRemark(String backRemark) {
		this.backRemark = backRemark;
		return this;
	}
	
	public String getBackTaskDefKey() {
		return backTaskDefKey;
	}

	public BackEntity setBackTaskDefKey(String backTaskDefKey) {
		this.backTaskDefKey = backTaskDefKey;
		return this;
	}
	
}
