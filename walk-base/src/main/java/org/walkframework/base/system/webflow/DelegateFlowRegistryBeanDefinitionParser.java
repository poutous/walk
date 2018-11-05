package org.walkframework.base.system.webflow;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.walkframework.base.tools.utils.PropertyAndExpressionUtil;
import org.walkframework.base.tools.utils.ReflectionUtils;

/**
 * @author shf675
 * 
 * 处理webflow标签中的属性值
 * 
 */
public class DelegateFlowRegistryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	
	private static final Logger log = LoggerFactory.getLogger(DelegateFlowRegistryBeanDefinitionParser.class);

	private static final String FLOW_REGISTRY_BEAN_DEFINITION_CLASS_NAME = "org.springframework.webflow.config.FlowRegistryBeanDefinitionParser";

	private AbstractSingleBeanDefinitionParser delegate;
	
	public DelegateFlowRegistryBeanDefinitionParser() {
		newFlowRegistryBeanDefinitionParser();
	}
	
	@Override
	protected String getBeanClassName(Element element) {
		try {
			return (String)ReflectionUtils.invoke(this.delegate, "getBeanClassName", new Object[] { element }, new Class[] { Element.class});
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder definitionBuilder) {
		try {
			ReflectionUtils.invoke(this.delegate, "doParse", new Object[] { element, parserContext, definitionBuilder }, new Class[] { Element.class, ParserContext.class, BeanDefinitionBuilder.class });
			List locations = (List) definitionBuilder.getBeanDefinition().getPropertyValues().getPropertyValue("flowLocations").getValue();
			if (locations == null) {
				return;
			}
			for (Iterator it = locations.iterator(); it.hasNext();) {
				Object flowLocation = it.next();
				Object path = ReflectionUtils.invoke(flowLocation, "getPath", null, null);
				if (!StringUtils.isEmpty(path)) {
					//解析属性值
					String newPath = PropertyAndExpressionUtil.resolvePropertys(path.toString());
					//替换原值
					ReflectionUtils.setFieldValue(flowLocation, "path", newPath);
				}
			}
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private void newFlowRegistryBeanDefinitionParser() {
		try {
			this.delegate = (AbstractSingleBeanDefinitionParser)ReflectUtils.newInstance(Class.forName(FLOW_REGISTRY_BEAN_DEFINITION_CLASS_NAME));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}