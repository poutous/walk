package org.walkframework.base.system.translate;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.util.StringUtils;
import org.walkframework.base.tools.utils.ParamTranslateUtil;
import org.walkframework.data.translate.TableTranslate;

/**
 * 编码转换器：根据指定表翻译
 * 
 * @author shf675
 * 
 */
public class TableTranslator extends AbstractTranslator {
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T translate(Object sourceObject, String translatedField) {
		TableTranslate translate = getTranslateAnnotation(sourceObject, translatedField, TableTranslate.class);
		if (translate != null) {
			String sourceField = StringUtils.trimWhitespace(translate.by());
			String translationPath = translate.path().replaceAll("\\s*", "");
			MetaObject targetMeta = SystemMetaObject.forObject(sourceObject);
			Object sourceValue = targetMeta.getValue(sourceField);
			if (!StringUtils.isEmpty(sourceValue)) {
				return (T) ParamTranslateUtil.getTranslateValue(getStringValue(sourceValue), translationPath);
			}
		}
		return null;
	}

	@Override
	public <T> T translate(Object sourceObject) {
		return null;
	}
}
