package org.walkframework.base.tools.utils;

import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.walkframework.base.tools.spring.SpringContextHolder;

import freemarker.template.Template;

/**
 * FreeMarker工具类
 * @author shf675
 *
 */
public abstract class FreeMarkerUtil {
	
	private static final Logger log = LoggerFactory.getLogger(FreeMarkerUtil.class);

	/**
	 * 根据模板与数据返回数据
	 * 
	 * @param templateName
	 * @param dataModel
	 * @return
	 */
	public static String process(String templateName, Map<String, Object> dataModel, HttpServletRequest request) {
		StringWriter writer = new StringWriter();
		try {
			FreeMarkerConfigurer freemarkerConfigurer = SpringContextHolder.getBean(FreeMarkerConfigurer.class);
			Locale locale = RequestContextUtils.getLocale(request);
			Template template = freemarkerConfigurer.getConfiguration().getTemplate(templateName.replaceAll("\\.", "/") + ".ftl", locale);
			template.process(dataModel, writer);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		writer.flush();
		return writer.toString();
	}
}
