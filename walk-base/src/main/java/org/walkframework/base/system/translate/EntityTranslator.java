package org.walkframework.base.system.translate;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.walkframework.base.tools.utils.ReflectionUtils;
import org.walkframework.batis.bean.WrapParameter;
import org.walkframework.data.annotation.Table;
import org.walkframework.data.bean.PageData;
import org.walkframework.data.bean.Pagination;
import org.walkframework.data.entity.BaseEntity;
import org.walkframework.data.entity.EntityHelper;
import org.walkframework.data.translate.EntityTranslate;

/**
 * 实体类翻译器
 * 
 * @author shf675
 * 
 */
public class EntityTranslator extends AbstractTranslator {
	
	@SuppressWarnings({ "unchecked"})
	@Override
	public <T> T translate(Object sourceObject, String translatedField) {
		EntityTranslate translate = getTranslateAnnotation(sourceObject, translatedField, EntityTranslate.class);
		if (translate == null) {
			return null;
		}
		String conditions = translate.conditions();
		if(StringUtils.isEmpty(conditions)){
			return null;
		}
		//获取翻译目标字段
		Field field = ReflectionUtils.getDeclaredField(sourceObject, translatedField);
		Class<? extends BaseEntity> entityType = null;
		if(List.class.isAssignableFrom(field.getType())){
             //得到泛型里的class类型对象
			entityType = (Class<? extends BaseEntity>)((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
		} else {
			entityType = (Class<? extends BaseEntity>)field.getType();
		}
		if(!BaseEntity.class.isAssignableFrom(entityType)){
			throw new TranslatException("translation objectives are not entity class!");
		}
		
		//查询
		Integer cacheSeconds = translate.cacheSeconds() == 0 ? null : translate.cacheSeconds();
		List<? extends BaseEntity> list = selectList(conditions, sourceObject, entityType, cacheSeconds);
		if(List.class.isAssignableFrom(field.getType())) {
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
	
	/**
	 * 组装sql查询
	 * 
	 * @param entityType
	 * @param conditions
	 * @param sourceObject
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<? extends BaseEntity> selectList(String conditions, Object sourceObject, Class<? extends BaseEntity> entityType, Integer cacheSeconds){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT *");
		sql.append(" FROM " + EntityHelper.findEntity(entityType).getAnnotation(Table.class).name());
		sql.append(" WHERE 1 = 1");
		sql.append(" AND ").append(conditions);
		WrapParameter wrapParameter = new WrapParameter(sourceObject, entityType);
		
		PageData<? extends BaseEntity> pageData = (PageData<? extends BaseEntity>)ReflectionUtils.invoke(dao(), "selectListBySql", new Object[]{sql.toString(), wrapParameter, null, cacheSeconds}, new Class[]{String.class, WrapParameter.class, Pagination.class, Integer.class});
		return pageData.getRows();
	}
	
	@Override
	public <T> T translate(Object sourceObject) {
		return null;
	}
	
}
