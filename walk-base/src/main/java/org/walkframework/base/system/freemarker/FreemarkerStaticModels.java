package org.walkframework.base.system.freemarker;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;

/**
 * freemarker添加静态方法访问
 * 
 * @author shf675
 *
 */
public class FreemarkerStaticModels extends HashMap<String, TemplateModel> implements InitializingBean {
	private static final long serialVersionUID = 1L;
	
	private final static Logger log = LoggerFactory.getLogger(FreemarkerStaticModels.class);
	
	private Map<String, String> staticModels;
	
	private FreeMarkerConfigurer freemarkerConfigurer;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}

	protected void init() {
		if (staticModels != null && !staticModels.isEmpty()) {
			for (Map.Entry<String, String> entry : staticModels.entrySet()) {
				put(entry.getKey(), useStaticPackage(entry.getValue()));
			}
		}
	}

	/**
	 * @param packageName
	 * @return
	 */
	@SuppressWarnings("static-access")
	private TemplateHashModel useStaticPackage(String packageName) {
		try {
			BeansWrapper wrapper = new BeansWrapperBuilder(getFreemarkerConfigurer().getConfiguration().getVersion()).build();
			TemplateHashModel staticModels = wrapper.getStaticModels();
			TemplateHashModel fileStatics = (TemplateHashModel) staticModels.get(packageName);
			return fileStatics;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public FreeMarkerConfigurer getFreemarkerConfigurer() {
		return freemarkerConfigurer;
	}

	public void setFreemarkerConfigurer(FreeMarkerConfigurer freemarkerConfigurer) {
		this.freemarkerConfigurer = freemarkerConfigurer;
	}
	
	public void setStaticModels(Map<String, String> staticModels) {
		this.staticModels = staticModels;
	}
	
	public Map<String, String> getStaticModels() {
		return staticModels;
	}

}
