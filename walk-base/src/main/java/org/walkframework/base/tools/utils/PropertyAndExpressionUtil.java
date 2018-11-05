package org.walkframework.base.tools.utils;

import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.env.Environment;
import org.walkframework.base.tools.spring.SpringContextHolder;

/**
 * 属性与表达式工具类工具类
 * 
 * @author shf675
 *
 */
public abstract class PropertyAndExpressionUtil {
	
	private static ConfigurableBeanFactory beanFactory;
	
	private static BeanExpressionContext beanExpressionContext;
	
	private static StandardBeanExpressionResolver expression;
	
	/**
	 * 替换文本中的属性
	 * 
	 * @param text
	 * @return
	 */
	public static String resolvePropertys(String text){
		return resolvePropertys(SpringContextHolder.getApplicationContext().getEnvironment(), text);
	}
	
	/**
	 * 替换文本中的属性
	 * 
	 * @param environment
	 * @param text
	 * @return
	 */
	public static String resolvePropertys(Environment environment, String text){
		return environment.resolveRequiredPlaceholders(text);
	}
	
	/**
	 * 替换文本中的属性与表达式
	 * 
	 * @param text
	 * @return
	 */
	public static String resolvePropertysAndExpression(String text){
		return doEvaluate(resolvePropertys(text));
	}
	
	/**
	 * 替换文本中的属性与表达式
	 * 
	 * @param environment
	 * @param text
	 * @return
	 */
	public static String resolvePropertysAndExpression(Environment environment, String text){
		return doEvaluate(resolvePropertys(environment, text));
	}
	
	/**
	 * 解析SpEL表达式
	 * 
	 * @param value
	 * @return
	 */
	public static String doEvaluate(String text) {
		if(beanFactory == null){
			beanFactory = new DefaultListableBeanFactory();
		}
		if(beanExpressionContext == null){
			beanExpressionContext = new BeanExpressionContext(beanFactory, null);
		}
		if(expression == null){
			expression = new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader());
		}
		Object evaluated = expression.evaluate(text, beanExpressionContext);
		return (evaluated != null ? evaluated.toString() : null);
	}
}
