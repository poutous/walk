package org.walkframework.base.tools.utils;

import java.io.Serializable;

public class ExportXml implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String fileName;
	
	private String headerJSON;
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getHeaderJSON() {
		return headerJSON;
	}

	public void setHeaderJSON(String headerJSON) {
		this.headerJSON = headerJSON;
	}
}
