package org.walkframework.base.system.translate;

import java.util.Map;

import org.springframework.util.StringUtils;
import org.walkframework.base.tools.utils.ParamTranslateUtil;

/**
 * 编码转换器：根据指定表翻译
 * 
 * @author shf675
 *
 */
public class MapTableTranslator extends AbstractTranslator {
	
	private String by;
	
	private String path;
	

	public MapTableTranslator(String by, String path) {
		this.by = by == null ? null : by.trim();
		this.path = path.replaceAll("\\s*", "");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T translate(Object sourceObject) {
		Map<?, ?> mapObject = (Map<?, ?>)sourceObject;
		Object sourceValue = mapObject.get(this.by);
		Object value = ParamTranslateUtil.getTranslateValue(getStringValue(sourceValue), this.path);
		if(StringUtils.isEmpty(value)){
			value = sourceValue;
		}
		return (T) value;
	}
}
