package org.walkframework.base.system.translate;

import java.util.HashMap;
import java.util.Map;

import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.data.translate.Translator;

/**
 * 翻译器工具类
 * 
 * @author shf675
 * 
 */
public class Translators {
	
	public static final Translator EMPTY_TRANSLATOR = new Translator() {
		@Override
		public <T> T translate(Object sourceObject) {
			return null;
		}

		@Override
		public <T> T translate(Object sourceObject, String targetField) {
			return null;
		}
	};

	private static final Map<String, Translator> translators = new HashMap<String, Translator>();

	private static Translators translatorsInstance;

	private Translators(){}
	public static Translators getInstance() {
		if (translatorsInstance == null) {
			translatorsInstance = new Translators();
		}
		return translatorsInstance;
	}

	@SuppressWarnings("unchecked")
	public Translator getTranslator(String annotationClassName, String translatorName) {
		if (translatorName == null || "".equals(translatorName)) {
			return translators.get(annotationClassName);
		}
		return getTranslatorFromContext(translatorName);
	}
	
	@SuppressWarnings("unchecked")
	private Translator getTranslatorFromContext(String translatorName){
		Translator translator = null;
		
		//尝试根据类名获取
		if(translator == null){
			try {
				Class<? extends Translator> clazz = (Class<? extends Translator>) Class.forName(translatorName);
				translator = SpringContextHolder.getBean(clazz);
			} catch (Exception e) {
			}
		}
		
		//从springContext中获取不到尝试new一个
		if(translator == null){
			try {
				translator = (Translator) Class.forName(translatorName).newInstance();
			} catch (Exception e) {
			}
		}
		return translator;
	}

	public Map<String, Translator> getTranslators() {
		return translators;
	}

	public void putTranslator(String annotationClassName, Translator translator) {
		translators.put(annotationClassName, translator);
	}

	public void putTranslators(Map<String, Translator> translators) {
		translators.putAll(translators);
	}
	
	public <T> T translate(Object sourceObject, String translatedField, String annotationClassName, String translatorName){
		return getTranslator(annotationClassName, translatorName).translate(sourceObject, translatedField);
	}

}
