package org.walkframework.batis.dialect;

import org.walkframework.batis.dao.Dao;

/**
 * Oracle实现的方言
 * 
 * @author shf675
 * 
 */
public class OracleDialect implements Dialect {
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
		str.append("SELECT * FROM (SELECT ROW_.*, ROWNUM ROWNUM_ FROM (");
		str.append(sql);
		str.append(") ROW_ WHERE ROWNUM <= " + end + ") WHERE ROWNUM_ > " + start);
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
		return new StringBuilder().append("SELECT COUNT(1) CNT FROM (").append(sql).append(")").toString();
	}

	/**
	 * 获取to_date
	 * 
	 * @return
	 */
	public String getToDate(String value) {
		return new StringBuilder().append("TO_DATE('").append(value).append("','").append(getDateFormat(value)).append("')").toString();
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
			return "YYYY";
		case 6:
			return "YYYYMM";
		case 7:
			return "YYYY-MM";
		case 8:
			return "YYYYMMDD";
		case 10:
			return "YYYY-MM-DD";
		case 13:
			return "YYYY-MM-DD HH24";
		case 16:
			return "YYYY-MM-DD HH24:MI";
		case 19:
			return "YYYY-MM-DD HH24:MI:SS";
		case 21:
			return "YYYY-MM-DD HH:MM:SS.S";
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
		return dao.selectOne("EntitySQL.selectDbTime");
	}
	
	/**
	 * 获取序列
	 * 
	 * @param dao
	 * @param sequence
	 * @return
	 */
	public String getSequence(Dao dao, String sequence) {
		return dao.selectOne("EntitySQL.selectSequence", sequence);
	}
}
