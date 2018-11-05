package org.walkframework.base.system.security;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.walkframework.batis.dao.SqlSessionDao;
import org.walkframework.shiro.bean.UrlFilter;
import org.walkframework.shiro.service.AbstractUrlFilterLoader;

/**
 * 默认的URL权限加载器
 * 
 * 需在工程的sql目录下定义UrlSQL.xml文件，并实现各sql语句，如不满足需要可继承DefaultUrlFilterLoader类重写
 * 
 * @author shf675
 * 
 */
public class DefaultUrlFilterLoader extends AbstractUrlFilterLoader implements SmartLifecycle {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private boolean hasLoaded;
	
	private SqlSessionDao dao;

	/**
	 * 加载url权限数据
	 * 
	 */
	@Override
	public void loadUrlFiltersData() {
		if (!isHasLoaded()) {
			try {
				List<UrlFilter> urlFiltersData = dao.selectList("UrlSQL.loadUrlFiltersData");
				if (urlFiltersData.size() > 0) {
					addUrlFilters(urlFiltersData);
				}
				setHasLoaded(true);
			} catch (Exception e) {
				log.warn("Not loaded to URL permissions data. The possible reason is that the loadUrlFiltersData statement is not implemented in UrlSQL.xml");
			}
		}
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public void start() {
		loadUrlFiltersData();
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPhase() {
		return Integer.MAX_VALUE;
	}
	
	public void setDao(SqlSessionDao dao) {
		this.dao = dao;
	}
	
	public SqlSessionDao getDao() {
		return dao;
	}
	
	public boolean isHasLoaded() {
		return hasLoaded;
	}

	public void setHasLoaded(boolean hasLoaded) {
		this.hasLoaded = hasLoaded;
	}
}