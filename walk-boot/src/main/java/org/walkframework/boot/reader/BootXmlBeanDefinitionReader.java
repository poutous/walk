package org.walkframework.boot.reader;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

/**
 * 自定义xml读取
 * 
 * 解决beans:import标签中的expression表达式
 * 
 * @author shf675
 *
 */
public class BootXmlBeanDefinitionReader extends XmlBeanDefinitionReader {

	public BootXmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
		super(registry);
		
		//自定义
		setDocumentReaderClass(BootBeanDefinitionDocumentReader.class);
	}
}
