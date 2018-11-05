package org.walkframework.base.system.jndi;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jndi.JndiObjectFactoryBean;

/**
 * 扩展org.springframework.jndi.JndiObjectFactoryBean
 * 
 * 初始化连接
 *
 */
public class JndiObjectFactoryBeanExt extends JndiObjectFactoryBean implements SmartLifecycle {

	private static final Log logger = LogFactory.getLog(JndiObjectFactoryBeanExt.class);

	private boolean lookupOnStartup = false;

	private boolean resourceRef = true;

	private Class<?>[] proxyInterfaces = new Class[] { DataSource.class };
	
	@Override
	public void afterPropertiesSet() throws IllegalArgumentException, NamingException {
		setResourceRef(this.resourceRef);
		setLookupOnStartup(this.lookupOnStartup);
		setProxyInterfaces(this.proxyInterfaces);
		super.afterPropertiesSet();
	}

	@Override
	public boolean isAutoStartup() {
		return !this.lookupOnStartup;
	}

	/**
	 * 容器加载完成后初始化一下数据库连接
	 * 
	 * @see org.springframework.context.Lifecycle#start()
	 */
	@Override
	public void start() {
		try {
			DataSource dataSource = (DataSource) getObject();
			DataSourceUtils.releaseConnection(DataSourceUtils.getConnection(dataSource), dataSource);
			if (logger.isDebugEnabled()) {
				logger.debug("jndi " + getJndiName() + " initialization completion.");
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
	}
	
	@Override
	public void setResourceRef(boolean resourceRef) {
		this.resourceRef = resourceRef;
		super.setResourceRef(this.resourceRef);
	}

	public void setLookupOnStartup(boolean lookupOnStartup) {
		this.lookupOnStartup = lookupOnStartup;
		super.setLookupOnStartup(this.lookupOnStartup);
	}

	@Override
	public void setProxyInterfaces(Class<?>... proxyInterfaces) {
		this.proxyInterfaces = proxyInterfaces;
		super.setProxyInterfaces(this.proxyInterfaces);
	}

	@Override
	public void stop(Runnable callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPhase() {
		return -1;
	}

}