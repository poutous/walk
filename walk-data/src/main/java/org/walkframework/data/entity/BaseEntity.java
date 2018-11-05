package org.walkframework.data.entity;

import com.alibaba.fastjson.JSON;

/**
 * 实体基类，所有实体都继承此基类
 * 
 * 注意：每个实体由工具生成
 * @author shf675
 *
 */
public abstract class BaseEntity extends AbstractEntity {
	
	@SuppressWarnings("unused")
	private boolean _IMPORT_RESULT = true;
	
	@SuppressWarnings("unused")
	private String _IMPORT_ERROR;
	
	@Override
	public String toString() {
		return JSON.toJSONString(this, serializerFeatures);
	}
}