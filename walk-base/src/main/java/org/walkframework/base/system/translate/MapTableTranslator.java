package org.walkframework.base.system.translate;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
		this.by = StringUtils.trim(by);
		this.path = path.replaceAll("\\s*", "");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T translate(Object sourceObject) {
		Map<?, ?> mapObject = (Map<?, ?>)sourceObject;
		return (T) ParamTranslateUtil.getTranslateValue(getStringValue(mapObject.get(this.by)), this.path);
	}
}
