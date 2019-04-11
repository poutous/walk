package org.walkframework.activiti.system.process;


/**
 * 提交流程入参
 * 
 * @author shf675
 */
public class CompleteEntity {

	/**
	 * 流程实例ID
	 */
	private String procInstId;

	/**
	 * 当前提交人ID
	 */
	private String completeUserId;

	/**
	 * 提交备注
	 */
	private String completeRemark;
	
	/**
	 * 网关值。用于流程网关判断
	 */
	private GatewayEntity gateway;
	
	/**
	 * 指定下一任务节点处理人(候选人)列表，非必填。如果不指定，通过对每个节点的配置(变量、监听等)也可取出处理人
	 */
	private String[] toUsers;
	
	/**
	 * 指定下一任务节点处理组(候选组)列表，非必填。同上
	 */
	private String[] toGroups;

	public CompleteEntity(String procInstId, String completeUserId) {
		this.procInstId = procInstId;
		this.completeUserId = completeUserId;
	}
	
	public String getProcInstId() {
		return procInstId;
	}

	public String getCompleteUserId() {
		return completeUserId;
	}

	public String getCompleteRemark() {
		return completeRemark;
	}

	public CompleteEntity setCompleteRemark(String completeRemark) {
		this.completeRemark = completeRemark;
		return this;
	}

	public String[] getToUsers() {
		return toUsers;
	}

	public CompleteEntity setToUsers(String... toUsers) {
		this.toUsers = toUsers;
		return this;
	}

	public String[] getToGroups() {
		return toGroups;
	}

	public CompleteEntity setToGroups(String... toGroups) {
		this.toGroups = toGroups;
		return this;
	}
	
	public GatewayEntity getGateway() {
		return gateway;
	}

	public CompleteEntity setGateway(GatewayEntity gateway) {
		this.gateway = gateway;
		return this;
	}

}
