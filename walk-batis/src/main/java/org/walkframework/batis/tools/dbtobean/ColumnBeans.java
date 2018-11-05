package org.walkframework.batis.tools.dbtobean;

public class ColumnBeans{  
	private String cName;
	private int cType;
	private String className;
	private String name;
	private String type;
	private int leng;
	private boolean isNull;
	private boolean isPrimary;
	private boolean isAutoIncrement;

	private String comments;
 
	public String getComments() {
		return this.comments; 
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getLeng() {
		return this.leng;
	}

	public void setLeng(int leng) {
		this.leng = leng;
	}

	public boolean isNull() {
		return this.isNull;
	}

	public void setNull(int isNull) {
		if (isNull == 0)
			this.isNull = false;
		else
			this.isNull = true;
	}

	public boolean isPrimary() {
		return this.isPrimary;
	}

	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}
	
	public boolean isAutoIncrement() {
		return isAutoIncrement;
	}

	public void setAutoIncrement(boolean isAutoIncrement) {
		this.isAutoIncrement = isAutoIncrement;
	}

	public String getCName() {
		return this.cName;
	}

	public void setCName(String name) {
		this.cName = name;
	}

	public int getCType() {
		return this.cType;
	}

	public void setCType(int type) {
		this.cType = type;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
}
