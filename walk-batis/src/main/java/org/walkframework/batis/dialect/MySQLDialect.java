package org.walkframework.batis.dialect;

import org.walkframework.batis.dao.Dao;

/**
 * Mysql实现的方言
 * 
 * @author shf675
 * 
 */
public class MySQLDialect implements Dialect {
	
	/**
	 * 获取分页sql
	 * 
	 * @param sql
	 * @param start
	 *            起始页
	 * @param end
	 *            结束页
	 * @return
	 */
	@Override
	public String getPagingSql(String sql, int start, int end) {
		StringBuilder str = new StringBuilder();
		str.append(sql);
		str.append(" LIMIT " + start + ", " + end);
		return str.toString();
	}

	/**
	 * 获取总数sql
	 * 
	 * @param sql
	 * @return
	 */
	@Override
	public String getCountSql(String sql) {
		return "SELECT COUNT(1) FROM (" + sql + ") AS CNT";
	}

	/**
	 * 获取to_date
	 * 
	 * @return
	 */
	public String getToDate(String value) {
		return "str_to_date('" + value + "','" + getDateFormat(value) + "')";
	}

	/**
	 * 获取日期格式化格式
	 * 
	 * @param value
	 * @return
	 */
	public String getDateFormat(String value) {
		switch (value.length()) {
		case 4:
			return "%Y";
		case 6:
			return "%Y%m";
		case 7:
			return "%Y-%m";
		case 8:
			return "%Y%m%d";
		case 10:
			return "%Y-%m-%d";
		case 13:
			return "%Y-%m-%d %H";
		case 16:
			return "%Y-%m-%d %H:%i";
		case 19:
			return "%Y-%m-%d %H:%i:%s";
		}
		return "";
	}
	
	/**
	 * 获取数据库时间。格式：yyyy-MM-dd HH:mm:ss
	 * 
	 * @param dao
	 * @return
	 */
	public String getDbTime(Dao dao) {
		return dao.selectOne("EntitySQL.selectDbTime_mysql");
	}
	
	/**
	 * 获取序列
	 *
	 * 创建序列表：可以用统一的序列，但为了避免业务表之间相互影响，原则是针对每张业务表都创建一张序列表，例如某日志表：
		CREATE TABLE SEQ_LOG_ID(
		  ID BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增ID',
		  MAX_ID BIGINT(20) DEFAULT 99998888 NOT NULL COMMENT '自增ID最大值，当超过该值后程序控制自增ID重置',
		  PRIMARY KEY (ID)
		)AUTO_INCREMENT=1;
		
	 * @param dao
	 * @param sequence
	 * @return
	 */
	public String getSequence(Dao dao, String sequence) {
		return new MySQLAsynSequence().getSequenceValue(dao, sequence);
	}
}
