package org.walkframework.batis.tools.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.walkframework.batis.holder.ResultMapHolder;
import org.walkframework.data.entity.BaseEntity;
import org.walkframework.data.entity.Entity;

/**
 * @author shf675
 *
 */
public abstract class ResultMapUtil {
	
	public static final String MODIFIED_SUFFIX = "-Modified";

	/**
	 * 修改原始resultMaps
	 * 修改条件：xml中未定义的字段映射，从实体类中添加映射关系
	 * @param ms
	 */
	public static void resolveResultMap(MappedStatement ms) {
		if(ResultMapHolder.get() != null){
			return;
		}
		Configuration configuration = ms.getConfiguration();
		List<ResultMap> resultMaps = ms.getResultMaps();
		if (resultMaps.size() > 0) {
			ResultMap resultMap = resultMaps.get(0);
			String id = resultMap.getId();
			Class<?> resultType = resultMap.getType();
			if (Entity.class.isAssignableFrom(resultType)) {
				List<ResultMap> newResultMaps = new ArrayList<ResultMap>();
				List<ResultMapping> newResultMappings = new ArrayList<ResultMapping>();
				Map<String, Field> fields = EntityUtil.getAllColumnFields(resultType);
				boolean modify = false;
				for (String column : fields.keySet()) {
					Field field = fields.get(column);
					ResultMapping resultMapping = getResultMapping(resultMap, column);
					if(resultMapping == null){
						//新增新映射
						ResultMapping.Builder builder = new ResultMapping.Builder(configuration, field.getName(), column, field.getType());
						builder.lazy(configuration.isLazyLoadingEnabled());
						newResultMappings.add(builder.build());
						modify = true;
					} else {
						//添加原始映射
						newResultMappings.add(resultMapping);
					}
				}
				if(modify){
					ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(configuration, id + MODIFIED_SUFFIX, resultType, newResultMappings, null);
					newResultMaps.add(inlineResultMapBuilder.build());
					
					//置入当前线程
					ResultMapHolder.set(newResultMaps);
				}
			}
		}
	}
	
	/**
	 * 修改原始resultMaps
	 * 修改条件：原始resultType与传入实体类型不同时
	 * @param ms
	 * @param clazz
	 */
	public static void resolveResultMapByEntity(MappedStatement ms, Class<? extends BaseEntity> clazz) {
		if(ResultMapHolder.get() != null){
			return;
		}
		Configuration configuration = ms.getConfiguration();
		List<ResultMap> resultMaps = ms.getResultMaps();
		if (resultMaps.size() > 0) {
			ResultMap resultMap = resultMaps.get(0);
			String id = resultMap.getId();
			Class<?> resultType = resultMap.getType();
			if (!clazz.equals(resultType)) {
				List<ResultMap> newResultMaps = new ArrayList<ResultMap>();
				List<ResultMapping> newResultMappings = new ArrayList<ResultMapping>();
				Map<String, Field> fields = EntityUtil.getAllColumnFields(clazz);
				for (String column : fields.keySet()) {
					Field field = fields.get(column);
					ResultMapping.Builder builder = new ResultMapping.Builder(configuration, field.getName(), column, field.getType());
					builder.lazy(configuration.isLazyLoadingEnabled());
					newResultMappings.add(builder.build());
				}
				ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(configuration, id + MODIFIED_SUFFIX, clazz, newResultMappings, null);
				newResultMaps.add(inlineResultMapBuilder.build());
				
				//置入当前线程
				ResultMapHolder.set(newResultMaps);
			}
		}
	}
	
	/**
	 * 修改原始resultMaps
	 * 修改条件：count语句
	 * @param ms
	 */
	public static void resolveResultMapCount(MappedStatement ms) {
		if(ResultMapHolder.get() != null){
			return;
		}
		Configuration configuration = ms.getConfiguration();
		ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(configuration, ms.getId() + "-Inline" + MODIFIED_SUFFIX, Long.class, new ArrayList<ResultMapping>(), null);
		List<ResultMap> resultMaps = new ArrayList<ResultMap>();
		resultMaps.add(inlineResultMapBuilder.build());
		
		//置入当前线程
		ResultMapHolder.set(resultMaps);
	}
	
	/**
	 * 根据column获取已定义的ResultMapping
	 * 
	 * @param resultMaps
	 * @param column
	 * @return
	 */
	private static ResultMapping getResultMapping(ResultMap resultMap, String column) {
		for (ResultMapping resultMapping : resultMap.getResultMappings()) {
			if (column.equals(resultMapping.getColumn())) {
				return resultMapping;
			}
		}
		return null;
	}
}
