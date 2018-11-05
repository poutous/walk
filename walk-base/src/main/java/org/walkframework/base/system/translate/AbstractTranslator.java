package org.walkframework.base.system.translate;

import java.lang.annotation.Annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.base.tools.utils.ReflectionUtils;
import org.walkframework.data.translate.Translator;

/**
 * @author shf675
 *
 */
public abstract class AbstractTranslator implements Translator {
	
	protected static final Logger log = LoggerFactory.getLogger(StaticTranslator.class);
	
	@Override
	public <T> T translate(Object sourceObject, String translatedField) {
		return translate(sourceObject);
	}
	
	/**
	 * 获取注解
	 * 
	 * @param <T>
	 * @param target
	 * @param targetField
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Annotation> T getTranslateAnnotation(Object sourceObject, String translatedField, Class<T> translateAnnotationType){
		return ReflectionUtils.getDeclaredField(sourceObject, translatedField).getAnnotation(translateAnnotationType);
	}
	
	/**
	 * getStringValue
	 * 
	 * @param value
	 * @return
	 */
	protected String getStringValue(Object value){
		return value == null ? null: value.toString();
	}
	
}
