package org.walkframework.base.system.webflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.cglib.core.ReflectUtils;

/**
 * @author shf675
 *
 */
public class WebFlowConfigNamespaceHandler extends NamespaceHandlerSupport {
	private static final Logger log = LoggerFactory.getLogger(WebFlowConfigNamespaceHandler.class);

	private static final String FLOW_EXECUTOR_CLASS_NAME = "org.springframework.webflow.config.FlowExecutorBeanDefinitionParser";
	private static final String FLOW_EXECUTION_LISTENERS_CLASS_NAME = "org.springframework.webflow.config.FlowExecutionListenerLoaderBeanDefinitionParser";
	private static final String FLOW_BUILDER_SERVICES_CLASS_NAME = "org.springframework.webflow.config.FlowBuilderServicesBeanDefinitionParser";

	public void init() {
		try {
			registerBeanDefinitionParser("flow-executor", (BeanDefinitionParser) ReflectUtils.newInstance(Class.forName(FLOW_EXECUTOR_CLASS_NAME)));
			registerBeanDefinitionParser("flow-execution-listeners", (BeanDefinitionParser) ReflectUtils.newInstance(Class.forName(FLOW_EXECUTION_LISTENERS_CLASS_NAME)));
			registerBeanDefinitionParser("flow-builder-services", (BeanDefinitionParser) ReflectUtils.newInstance(Class.forName(FLOW_BUILDER_SERVICES_CLASS_NAME)));

			//自定义
			registerBeanDefinitionParser("flow-registry", new DelegateFlowRegistryBeanDefinitionParser());

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
