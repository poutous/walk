package org.walkframework.base.system.initializer;

import org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.walkframework.base.tools.utils.PropertyAndExpressionUtil;

/**
 * 扩展org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader
 * 
 * @author shf675
 *
 */
public class WalkBeanDefinitionDocumentReader extends DefaultBeanDefinitionDocumentReader {
	
	/* (non-Javadoc)
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
		ele.setAttribute(RESOURCE_ATTRIBUTE, PropertyAndExpressionUtil.resolvePropertysAndExpression(getReaderContext().getEnvironment(), location));
		
		//执行父类逻辑
		super.importBeanDefinitionResource(ele);
	}
}
