package org.walkframework.base.system.translate;

import java.util.Map;

import org.springframework.util.StringUtils;
import org.walkframework.base.tools.utils.ParamTranslateUtil;

/**
 * 静态参数表(td_s_static)编码转换器
 * 
 * @author shf675
 *
 */
public class MapStaticTranslator extends AbstractTranslator {
	
	private String by;
	
	private String typeId;

	public MapStaticTranslator(String by, String typeId) {
		this.by = StringUtils.trimWhitespace(by);
		this.typeId = StringUtils.trimWhitespace(typeId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T translate(Object sourceObject) {
		Map<?, ?> mapObject = (Map<?, ?>)sourceObject;
		Object sourceValue = mapObject.get(this.by);
		Object value = ParamTranslateUtil.convertStatic(this.typeId, getStringValue(sourceValue));
		if(StringUtils.isEmpty(value)){
			value = sourceValue;
		}
		return (T) value;
	}
}
