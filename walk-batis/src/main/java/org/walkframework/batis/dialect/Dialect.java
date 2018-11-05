package org.walkframework.batis.dialect;

import org.walkframework.batis.dao.Dao;

/**
 * 数据库方言
 * 
 * @author shf675
 */
public interface Dialect {

	/**
	 * 获取分页sql
	 * @param sql 
	 * @param start 起始页
	 * @param end  结束页
	 * @return
	 */
	String getPagingSql(String sql, int start, int end);

	/**
	 * 获取总数sql
	 * @param sql 
	 * @return
	 */
	String getCountSql(String sql);

	/**
	 * 获取to_date
	 * 
	 * @return
	 */
	String getToDate(String value);

	/**
	 * 获取日期格式化格式
	 * 
	 * @param value
	 * @return
	 */
	String getDateFormat(String value);

	/**
	 * 获取数据库时间。格式：yyyy-MM-dd HH:mm:ss
	 * 
	 * @param dao
	 * @return
	 */
	String getDbTime(Dao dao);

	/**
	 * 获取序列
	 * 
	 * @param dao
	 * @param sequence
	 * @return
	 */
	String getSequence(Dao dao, String sequence);
}
