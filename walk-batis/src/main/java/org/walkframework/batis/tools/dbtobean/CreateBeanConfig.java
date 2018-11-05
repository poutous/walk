package org.walkframework.batis.tools.dbtobean;


/**
 * 配置信息
 *
 */
public class CreateBeanConfig {
	//配置信息
	private String driverClassName = "oracle.jdbc.driver.OracleDriver";//数据库驱动 默认oracle
	private String dburl = "";					//数据库url
	private String dbusername = "";				//数据库用户名
	private String dbpassword = "";				//数据库用户密码
	private boolean isOverrideToString = false;	//是否重写toString方法 默认不重写
	private boolean isGetField = false;			//是否生成getField方法 默认不生成
	private boolean forceBigDecimals = false;	//是否强制将数据库类型DECIMAL和NUMERIC转换为BigDecimal类型
	
	public boolean isForceBigDecimals() {
		return forceBigDecimals;
	}
	public void setForceBigDecimals(boolean forceBigDecimals) {
		this.forceBigDecimals = forceBigDecimals;
	}
	public String getDburl() {
		return dburl;
	}
	public void setDburl(String dburl) {
		this.dburl = dburl;
	}
	public String getDbusername() {
		return dbusername;
	}
	public void setDbusername(String dbusername) {
		this.dbusername = dbusername;
	}
	public String getDbpassword() {
		return dbpassword;
	}
	public void setDbpassword(String dbpassword) {
		this.dbpassword = dbpassword;
	}
	public boolean isOverrideToString() {
		return isOverrideToString;
	}
	public void setOverrideToString(boolean isOverrideToString) {
		this.isOverrideToString = isOverrideToString;
	}
	public boolean isGetField() {
		return isGetField;
	}
	public void setGetField(boolean isGetField) {
		this.isGetField = isGetField;
	}
	public String getDriverClassName() {
		return driverClassName;
	}
	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}
}
