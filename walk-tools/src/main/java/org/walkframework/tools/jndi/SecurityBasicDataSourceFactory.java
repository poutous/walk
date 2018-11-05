package org.walkframework.tools.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;

/**
 * dbcp数据源密码处理
 * 
 * @author shf675
 *
 */
public class SecurityBasicDataSourceFactory extends BasicDataSourceFactory {
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) throws Exception {
		BasicDataSource dataSource = (BasicDataSource)super.getObjectInstance(obj, name, nameCtx, environment);
		String password = dataSource.getPassword();
		if(password.toUpperCase().startsWith("{DES}")){
			dataSource.setPassword(DataSourcePasswordGenerator.decryptByDES(password.substring(5)));
		}
		return dataSource;
	}
}
