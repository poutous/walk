package org.walkframework.activiti.system.process;


/**
 * 转办流程入参
 * 
 * @author shf675
 */
public class TransferEntity {

	/**
	 * 流程实例ID
	 */
	private String procInstId;
	
	/**
	 * 当前人ID
	 */
	private String currUserId;

	/**
	 * 转办任务给的人列表
	 */
	private String[] transferUserIds;
	
	/**
	 * 转办任务给的组列表
	 */
	private String[] transferGroups;

	/**
	 * 转办备注
	 */
	private String transferRemark;
	
	public TransferEntity(String procInstId, String currUserId){
		this.procInstId = procInstId;
		this.currUserId = currUserId;
	}
	
	public String getProcInstId() {
		return procInstId;
	}
	
	public String getCurrUserId() {
		return currUserId;
	}
	
	public String getTransferRemark() {
		return transferRemark;
	}
	
	public String[] getTransferUserIds() {
		return transferUserIds;
	}

	public TransferEntity setTransferUserIds(String... transferUserIds) {
		this.transferUserIds = transferUserIds;
		return this;
	}

	public String[] getTransferGroups() {
		return transferGroups;
	}

	public TransferEntity setTransferGroups(String... transferGroups) {
		this.transferGroups = transferGroups;
		return this;
	}

	public TransferEntity setTransferRemark(String transferRemark) {
		this.transferRemark = transferRemark;
		return this;
	}
	
}
