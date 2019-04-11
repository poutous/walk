package org.walkframework.activiti.system.constant;

/**
 * 流程相关常量
 * 
 * @author shf675
 */
public interface ProcessConstants {
	
	String SEQ_LOG_ID = "SEQ_LOG_ID";  //日志序列
	
	String SEQ_ORDER_ID = "SEQ_ORDER_ID";  //工单序列
	
	String SYSTEM_AUTO_STAFF_ID = "systemauto";  //系统自动工号
	
	String PROCESS_BUSINESSID = "businessId";//业务表ID
	String PROCESS_WORKORDERTABLE = "ACT_UD_WORKORDER";//工作流工单表
	
	/**
	 * 流程结束节点的定义
	 */
	String START_NODE_KEY = "START";//开始节点KEY
	String END_NODE_KEY = "END";//结束节点KEY
	String END_NODE_STATE = "end";//结束节点的状态
	
	String PROCESS_ASSIGN_SUBMITOR = "submitor";//工单发起人
	
	
}
