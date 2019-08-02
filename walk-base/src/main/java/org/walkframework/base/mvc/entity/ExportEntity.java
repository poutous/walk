package org.walkframework.base.mvc.entity;

import java.io.Serializable;

/**
 * 导出实体
 * 
 * @author shf675
 *
 */
public class ExportEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String exportName;
	
	private String reqUri;
	
	private String params;
	
	private String operateIp;
	
	private String operateStaff;
	
	private String appointmentTime;
	
	public String getExportName() {
		return exportName;
	}

	public void setExportName(String exportName) {
		this.exportName = exportName;
	}

	public String getReqUri() {
		return reqUri;
	}

	public void setReqUri(String reqUri) {
		this.reqUri = reqUri;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getOperateIp() {
		return operateIp;
	}

	public void setOperateIp(String operateIp) {
		this.operateIp = operateIp;
	}

	public String getOperateStaff() {
		return operateStaff;
	}

	public void setOperateStaff(String operateStaff) {
		this.operateStaff = operateStaff;
	}
	
	public String getAppointmentTime() {
		return appointmentTime;
	}

	public void setAppointmentTime(String appointmentTime) {
		this.appointmentTime = appointmentTime;
	}
}
