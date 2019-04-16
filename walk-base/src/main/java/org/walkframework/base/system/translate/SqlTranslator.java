package org.walkframework.base.system.translate;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.walkframework.base.tools.utils.ReflectionUtils;
import org.walkframework.data.translate.SqlTranslate;

/**
 * 通过sqlId翻译对象
 * 
 * @author shf675
 * 
 */
public class SqlTranslator extends AbstractTranslator {
	
	@SuppressWarnings({ "unchecked"})
	@Override
	public <T> T translate(Object sourceObject, String translatedField) {
		SqlTranslate translate = getTranslateAnnotation(sourceObject, translatedField, SqlTranslate.class);
		if (translate == null) {
			return null;
		}
		String sqlId = translate.sqlId();
		if(StringUtils.isEmpty(sqlId)){
			return null;
		}
		
		//获取翻译目标字段
		Field targetField = ReflectionUtils.getDeclaredField(sourceObject, translatedField);
		
		//查询
		List<?> list = dao().selectList(sqlId, new BeanMap(sourceObject));
		if(List.class.isAssignableFrom(targetField.getType())) {
			return (T) list;
		} else {
			if (list.size() == 1) {
				return (T) list.get(0);
			} else if (list.size() > 1) {
				throw new TooManyResultsException("Multiple data returned, found: " + list.size());
			} else {
				return null;
			}
		}
	}
	
	@Override
	public <T> T translate(Object sourceObject) {
		return null;
	}
	
}
