package org.walkframework.base.system.translate;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.util.StringUtils;
import org.walkframework.base.tools.utils.ParamTranslateUtil;
import org.walkframework.data.translate.StaticTranslate;

/**
 * 编码转换器：根据默认静态参数表(td_s_static)翻译
 * 
 * @author shf675
 * 
 */
public class StaticTranslator extends AbstractTranslator {

	@SuppressWarnings("unchecked")
	@Override
	public <T> T translate(Object sourceObject, String translatedField) {
			StaticTranslate translate = getTranslateAnnotation(sourceObject, translatedField, StaticTranslate.class);
			if (translate != null) {
				String sourceField = StringUtils.trimWhitespace(translate.by());
				String typeId = StringUtils.trimWhitespace(translate.typeId());
				MetaObject targetMeta = SystemMetaObject.forObject(sourceObject);
				Object sourceValue = targetMeta.getValue(sourceField);
				if (!StringUtils.isEmpty(sourceValue)) {
					return (T) ParamTranslateUtil.convertStatic(typeId, getStringValue(sourceValue));
				}
			}
		return null;
	}
	
	@Override
	public <T> T translate(Object sourceObject) {
		return null;
	}
}
