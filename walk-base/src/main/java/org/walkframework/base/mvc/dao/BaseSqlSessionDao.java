package org.walkframework.base.mvc.dao;

import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.factory.SingletonFactory;
import org.walkframework.batis.dao.SqlSessionDao;

/**
 * 加入sequence等支持
 * 
 * @author shf675
 *
 */
public class BaseSqlSessionDao extends SqlSessionDao {
	
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	protected final static Common common = SingletonFactory.getInstance(Common.class);
	
	public BaseSqlSessionDao(SqlSessionFactory sqlSessionFactory, String dialect) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		super(sqlSessionFactory, dialect);
	}

	public BaseSqlSessionDao(SqlSessionFactory sqlSessionFactory, String dialect, int exportPageSize, int randomRange) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		super(sqlSessionFactory, dialect, exportPageSize, randomRange);
	}
	
	/**
	 * 获取序列 长度：默认
	 * @param sequence
	 * @return String
	 */
	public String getSequence(String sequence) {
		return getDialect().getSequence(this, sequence);
	}
	
	/**
	 * 获取序列 长度：指定
	 * @param sequence
	 * @return String
	 */
	public String getSequence(String sequence, Integer length) {
		return common.lpad(getSequence(sequence), length, "0");
	}
	
	/**
	 * 获取序列 长度：16 格式YYYYMMDD00000001
	 * @param sequence
	 * @return String
	 */
	public String getSequenceL16(String sequence) {
		return common.decodeTimestamp("yyyyMMdd", common.getCurrentTime()).concat(common.lpad(getSequence(sequence), 8, "0"));
	}

	/**
	 * 获取序列 长度：20 格式：YYYYMMDDHH24MISS000001
	 * @param sequence
	 * @return String
	 */
	public String getSequenceL20(String sequence) {
		return common.decodeTimestamp("yyyyMMddHHmmss", common.getCurrentTime()).concat(common.lpad(getSequence(sequence), 6, "0"));
	}
	
	/**
	 * 获取数据库时间。格式：yyyy-MM-dd HH:mm:ss
	 * @return String
	 */
	public String getDbTime() {
		return getDialect().getDbTime(this);
	}
	
	/**
	 * 获取数据库日期。格式：yyyy-MM-dd
	 * @return String
	 */
	public String getDbDate() {
		return getDbTime().substring(0, 10);
	}
}
