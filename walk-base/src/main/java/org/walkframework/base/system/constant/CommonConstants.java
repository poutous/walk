package org.walkframework.base.system.constant;
/**
 * 常量
 * 
 */
public interface CommonConstants {
	
	//动作类型
	String ACTION_TYPE = "__actionType";
	
	String ACTION_TYPE_EXPORT = "export";
	
	String ACTION_TYPE_ASYN_EXPORT = "asynExport";
	
	String ACTION_TYPE_EXPAND = "expand";
	
	String PAGINATION_ROWS = "rows";
	
	String PAGINATION_PAGE = "page";
	
	String HEADER_JSON_NAME = "headerJSON";
	
	String EXPORT_NAME = "exportName";
	
	String EXPORT_LOG_CODE = "exportLogCode";
	
	String ASYN_EXPORT_ID = "asynExportId";
	
	String ASYN_EXPORT_NAME = "asynExportName";
	
	String ASYN_EXPORT_WAY = "asynExportWay";
	
	String ASYN_EXPORT_APPOINTMENT_TIME = "asynExportAppointmentTime";
	
	String EXPORT_XML_NAME = "__EXPORT_XML_NAME";
	
	String EXPORT_TOTAL = "__EXPORT_TOTAL";
	
	String FILE_COVER_IF_EXIST = "coverIfExist";
	
	/**导出模式*/
	String EXPORT_MODE_SYNC = "1";//同步
	String EXPORT_MODE_ASYN = "2";//异步
	
	/**导出方式*/
	String EXPORT_WAY_IMMEDIATE = "1";//即时
	String EXPORT_WAY_TIMING = "2";//定时
	
	/**导出状态*/
	String EXPORT_STATE_EXECUTING = "0";//执行中
	String EXPORT_STATE_SUCCESS = "1";//成功
	String EXPORT_STATE_FAILURE = "2";//失败
	String EXPORT_STATE_APPOINTMENT = "3";//预约导出中
}