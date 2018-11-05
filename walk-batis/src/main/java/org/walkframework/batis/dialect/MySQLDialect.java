package org.walkframework.batis.dialect;

import java.util.Random;

import org.walkframework.batis.dao.Dao;
import org.walkframework.batis.dao.SqlSessionDao;
import org.walkframework.data.util.DataMap;
import org.walkframework.data.util.IData;

/**
 * Mysql实现的方言
 * 
 * @author shf675
 * 
 */
public class MySQLDialect implements Dialect {
	
	private static final Random random = new Random();
	
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
		str.append(" LIMIT " + start + ", " + (end - start));
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
		return dao.selectOne("CommonSQL.selectDbTime_mysql");
	}
	
	/**
	 * 获取序列
	 *
	 * 创建序列表：可以用统一的序列，但为了避免业务表之间相互影响，原则是针对每张业务表都创建一张序列表，例如某日志表：
		CREATE TABLE seq_log_id(
		  ID BIGINT NOT NULL AUTO_INCREMENT,
		  PRIMARY KEY (ID)
		)AUTO_INCREMENT=10001;
		
	 * @param dao
	 * @param sequence
	 * @return
	 */
	public String getSequence(Dao dao, String sequence) {
		IData<String, Object> param = new DataMap<String, Object>();
		param.put("seq_name", sequence);
		
		//1、向序列表中插入数据
		dao.insert("CommonSQL.selectSequence_mysql", param);
		
		//2、清空序列表：为提高性能，不需要每次都清空序列表。在一定范围内取随机数，取到1时就删。
		if(random.nextInt(((SqlSessionDao)dao).getRandomRange()) == 1){
			dao.delete("CommonSQL.clearSeqTable", param);
		}
		//3、获取序列值
		return param.getString("id");
	}
}
