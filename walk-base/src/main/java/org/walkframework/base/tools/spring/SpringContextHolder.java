package org.walkframework.base.tools.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @ClassName: SpringContextHolder
 * @Description: 获取spring配置里面的bean的静态类
 */
public class SpringContextHolder implements ApplicationContextAware {
	/**
	 * @Fields context : Spring上下文容器
	 */
	private static ApplicationContext applicationContext;// 声明一个静态变量保存

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		setContext(context);
	}

	/**
	 * @Title: setContext
	 * @Description: 由于直接在setApplicationContext中对静态变量赋值， findbugs报警告，这个方法只是为了避免findbugs报警告
	 * @param context spring的上下文
	 */
	private static void setContext(final ApplicationContext context) {
		applicationContext = context;
	}

	/**
	 * @Title: getApplicationContext
	 * @Description: 获取Spring上下文
	 * @return Spring上下文
	 */
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * @Title: getBean
	 * @Description: 知道确切bean名称时，根据提供的bean名称得到对应服务类
	 * @param name bean名称
	 * @return Object bean名称对应服务类
	 */
	public static Object getBean(final String name) {
		return applicationContext.getBean(name);
	}

	/**
	 * @Title: getBean
	 * @Description: 根据提供的bean的类型得到对应bean
	 * @param requiredType 需要的bean的类型
	 * @param <T> 指定返回的bean类型
	 * @return Object bean名称对应服务类
	 */
	public static <T> T getBean(final Class<T> requiredType) {
		return applicationContext.getBean(requiredType);
	}

	/**
	 * @Title: getBean
	 * @Description: 根据提供的bean名称得到对应于指定类型的服务类
	 * @param name bean名称
	 * @param clazz 返回的bean类型,若类型不匹配,将抛出异常
	 * @param <T> 指定返回的bean类型
	 * @return T bean名称对应于指定类型的服务类
	 */
	public static <T> T getBean(final String name, final Class<T> clazz) {
		return applicationContext.getBean(name, clazz);
	}

	/**
	 * @Title: containsBean
	 * @Description: 检查bean工厂里是否包含名字为name的bean
	 * @param name bean的名字
	 * @return boolean 是否包含该bean
	 */
	public static boolean containsBean(final String name) {
		return applicationContext.containsBean(name);
	}

	/**
	 * @Title: getProperty
	 * @Description: 根据名称，从Spring启动加载的所有Property中找到指定属性
	 * @param name 属性名称
	 * @return 属性值
	 */
	public static String getProperty(String name) {
		return applicationContext.getEnvironment().getProperty(name);
	}

}
