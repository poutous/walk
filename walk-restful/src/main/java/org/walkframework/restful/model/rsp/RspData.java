package org.walkframework.restful.model.rsp;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 出参模型基类
 * 
 * @author shf675
 */
public abstract class RspData implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final Map<String, Object> mapping = new HashMap<String, Object>();

	/**
	 * 注册属性翻译器参数对象
	 * 
	 * 向目标属性的翻译器传递参数
	 * 
	 * @param propertyName
	 * @param sourceObject
	 */
	public void registerPropertyTranslatorSourceObject(String propertyName, Object sourceObject) {
		mapping.put(getKey(propertyName), sourceObject);
	}

	/**
	 * 获取属性翻译器参数对象
	 * 
	 * @param propertyName
	 * @return
	 */
	public Object gainPropertyTranslatorSourceObject(String propertyName) {
		return mapping.get(getKey(propertyName));
	}

	private String getKey(String propertyName) {
		return this.getClass().getName() + "_" + propertyName;
	}
}
