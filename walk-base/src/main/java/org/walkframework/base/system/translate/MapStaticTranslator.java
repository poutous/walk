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
		return (T) ParamTranslateUtil.convertStatic(this.typeId, getStringValue(mapObject.get(this.by)));
	}
}
