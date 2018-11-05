package org.walkframework.base.system.initializer;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * @author shf675
 *
 */
public class WalkXmlWebApplicationContext extends XmlWebApplicationContext {
	
	/* (non-Javadoc)
	 * @see org.springframework.web.context.support.XmlWebApplicationContext#initBeanDefinitionReader(org.springframework.beans.factory.xml.XmlBeanDefinitionReader)
	 */
	@Override
	protected void initBeanDefinitionReader(XmlBeanDefinitionReader beanDefinitionReader) {
		//设置自定义BeanDefinitionDocumentReader
		beanDefinitionReader.setDocumentReaderClass(WalkBeanDefinitionDocumentReader.class);
		super.initBeanDefinitionReader(beanDefinitionReader);
	}
	
}
