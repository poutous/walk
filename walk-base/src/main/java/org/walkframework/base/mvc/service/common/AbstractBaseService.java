package org.walkframework.base.mvc.service.common;

import org.walkframework.base.mvc.service.base.BaseService;
import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.base.tools.spring.SpringPropertyHolder;
import org.walkframework.batis.dao.SqlSessionDao;

/**
 * 取默认dao
 * 
 * @author shf675
 *
 */
public abstract class AbstractBaseService extends BaseService {
	
	private SqlSessionDao dao;

	public SqlSessionDao setDao(SqlSessionDao dao) {
		this.dao = dao;
		return this.dao;
	}
	
	public SqlSessionDao dao() {
		if(dao == null){
			dao = SpringContextHolder.getBean(SpringPropertyHolder.getContextProperty("walkbatis.defaultSqlSessionDaoName", "sqlSessionDao"), SqlSessionDao.class);
		}
		return dao;
	}
}
