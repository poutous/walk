package org.walkframework.tools.jndi;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariJNDIFactory;
import com.zaxxer.hikari.util.PropertyElf;

/**
 * HikariCP密码处理
 * 
 * @author shf675
 *
 */
public class SecurityHikariJNDIFactory extends HikariJNDIFactory {

	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
		// We only know how to deal with <code>javax.naming.Reference</code> that specify a class name of "javax.sql.DataSource"
		if (!(obj instanceof Reference)) {
			return null;
		}

		Reference ref = (Reference) obj;
		if (!"javax.sql.DataSource".equals(ref.getClassName())) {
			throw new NamingException(ref.getClassName() + " is not a valid class name/type for this JNDI factory.");
		}

		Set<String> hikariPropSet = PropertyElf.getPropertyNames(HikariConfig.class);

		Properties properties = new Properties();
		Enumeration<RefAddr> enumeration = ref.getAll();
		while (enumeration.hasMoreElements()) {
			RefAddr element = enumeration.nextElement();
			String type = element.getType();
			if (type.startsWith("dataSource.") || hikariPropSet.contains(type)) {

				//加密的密码进行解密
				String propertyValue = element.getContent().toString();
				if ("password".equals(type) && propertyValue.toUpperCase().startsWith("{DES}")) {
					propertyValue = DataSourcePasswordGenerator.decryptByDES(propertyValue.substring(5));
				}
				properties.setProperty(type, propertyValue);
			}
		}
		return invoke(this, HikariJNDIFactory.class, "createDataSource", new Object[] { properties, nameCtx }, new Class[] { Properties.class, Context.class });
	}

	/**
	 * 反射执行方法
	 * @param obj
	 * @param targetClass
	 * @param methodName
	 * @param params
	 * @param paramsClass
	 * @return
	 * @throws Exception
	 */
	private static Object invoke(Object obj, Class<?> targetClass, String methodName, Object[] params, Class<?>[] paramsClass) throws Exception {
		Method method = targetClass.getDeclaredMethod(methodName, paramsClass);
		method.setAccessible(true);
		return method.invoke(obj, params);
	}
}
