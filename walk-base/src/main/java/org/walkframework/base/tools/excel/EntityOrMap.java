package org.walkframework.base.tools.excel;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.base.system.editor.EntityOrMapIsNullException;
import org.walkframework.data.entity.EntityHelper;

/**
 * @author shf675
 * 
 * 实体类或map对象
 *
 * @param <K>
 * @param <V>
 */
public class EntityOrMap {

	private final static Logger log = LoggerFactory.getLogger(EntityOrMap.class);

	private Object nativeObject;
	private MetaObject nativeObjectMeta;

	private int size;

	public EntityOrMap(Object nativeObject) {
		if (nativeObject == null) {
			throw new EntityOrMapIsNullException();
		}
		this.nativeObject = nativeObject;
		this.nativeObjectMeta = SystemMetaObject.forObject(this.nativeObject);
	}

	public EntityOrMap(Class<?> nativeClazz) {
		try {
			this.nativeObject = nativeClazz.newInstance();
			this.nativeObjectMeta = SystemMetaObject.forObject(this.nativeObject);
		} catch (InstantiationException e) {
			log.error(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			log.error(e.getMessage(), e);
		}
	}

	public void set(String name, Object value) {
		Class<?> getterType = this.nativeObjectMeta.getGetterType(name);
		if(value != null && !(value.getClass().equals(getterType) || getterType.isAssignableFrom(value.getClass()))){
			value = EntityHelper.parseString(value.toString(), getterType);
		}
		this.nativeObjectMeta.setValue(name, value);
		this.size++;
	}

	public Object get(String name, String defaultValue) {
		Object v = get(name);
		return v == null ? defaultValue : v;
	}

	public Object get(String name) {
		return this.nativeObjectMeta.getValue(name);
	}

	public int size() {
		return this.size;
	}

	@SuppressWarnings("unchecked")
	public <T> T getNativeObject() {
		return (T) this.nativeObject;
	}
}
