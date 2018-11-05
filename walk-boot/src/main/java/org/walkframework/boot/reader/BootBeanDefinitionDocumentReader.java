package org.walkframework.boot.reader;

import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * 扩展org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader
 * 
 * 
 * @author shf675
 *
 */
public class BootBeanDefinitionDocumentReader extends DefaultBeanDefinitionDocumentReader {
	
	private ConfigurableBeanFactory beanFactory;
	
	private BeanExpressionContext beanExpressionContext;
	
	private StandardBeanExpressionResolver expression;
	
	/* 解决beans:import标签中的expression表达式
	 * 
	 * @see org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader#importBeanDefinitionResource(org.w3c.dom.Element)
	 */
	@Override
	protected void importBeanDefinitionResource(Element ele) {
		String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
		if (!StringUtils.hasText(location)) {
			getReaderContext().error("Resource location must not be empty", ele);
			return;
		}

		//重新设置location
		ele.setAttribute(RESOURCE_ATTRIBUTE, resolvePropertysAndExpression(getReaderContext().getEnvironment(), location));
		
		//执行父类逻辑
		super.importBeanDefinitionResource(ele);
	}
	
	/**
	 * 替换文本中的属性
	 * 
	 * @param environment
	 * @param text
	 * @return
	 */
	public String resolvePropertys(Environment environment, String text){
		return environment.resolveRequiredPlaceholders(text);
	}
	
	/**
	 * 替换文本中的属性与表达式
	 * 
	 * @param environment
	 * @param text
	 * @return
	 */
	public String resolvePropertysAndExpression(Environment environment, String text){
		return doEvaluate(resolvePropertys(environment, text));
	}
	
	/**
	 * 解析SpEL表达式
	 * 
	 * @param value
	 * @return
	 */
	public String doEvaluate(String text) {
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
