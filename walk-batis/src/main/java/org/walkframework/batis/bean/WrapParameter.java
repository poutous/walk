package org.walkframework.batis.bean;

import org.walkframework.data.entity.BaseEntity;

/**
 * 参数包装
 * 
 * @author shf675
 *
 */
public class WrapParameter {
	
	private Object parameterObject;
	
	private Class<? extends BaseEntity> entityType;
	
	public WrapParameter(Object parameterObject, Class<? extends BaseEntity> entityType){
		this.parameterObject = parameterObject;
		this.entityType = entityType;
	}
	
	public Object getParameterObject() {
		return parameterObject;
	}

	public void setParameterObject(Object parameterObject) {
		this.parameterObject = parameterObject;
	}
	
	public Class<? extends BaseEntity> getEntityType() {
		return entityType;
	}

	public void setEntityType(Class<? extends BaseEntity> entityType) {
		this.entityType = entityType;
	}

}
