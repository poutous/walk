package org.walkframework.activiti.mvc.service.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.base.mvc.dao.BaseSqlSessionDao;
import org.walkframework.base.tools.spring.SpringContextHolder;

/**
 * activiti流程任务监听基类。任务节点监听、网关节点监听、全局监听
 * 
 * @author shf675
 *
 */
public abstract class ActProcessBaseListener {
	
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	protected BaseSqlSessionDao dao() {
		return SpringContextHolder.getBean("sqlSessionDao", BaseSqlSessionDao.class);
	}
}
