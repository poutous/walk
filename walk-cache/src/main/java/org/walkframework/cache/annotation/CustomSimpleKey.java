package org.walkframework.cache.annotation;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 自定义缓存key对象
 * 
 * key组织形式：方法名+方法所有参数类型+方法所有参数值
 * 
 * @author shf675
 * 
 */
@SuppressWarnings("serial")
public class CustomSimpleKey implements Serializable {

	protected static final SerializerFeature[] serializerFeatures = new SerializerFeature[] { SerializerFeature.WriteDateUseDateFormat, SerializerFeature.DisableCircularReferenceDetect };

	private Method method;

	private Object[] params;

	private int hashCode;

	public CustomSimpleKey(Method method, Object... elements) {
		this.method = method;
		this.params = new Object[elements.length];
		System.arraycopy(elements, 0, this.params, 0, elements.length);
		this.hashCode = Arrays.deepHashCode(this.params);
	}

	@Override
	public boolean equals(Object obj) {
		return (this == obj || (obj instanceof CustomSimpleKey && Arrays.deepEquals(this.params, ((CustomSimpleKey) obj).params)));
	}

	@Override
	public final int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append("_");
		sb.append(this.method.getName());
		sb.append("(");
		sb.append(arrayToDelimitedString(this.method.getParameterTypes(), ","));
		sb.append(")");
		sb.append("[");
		sb.append(paramArrayToDelimitedString(this.params, ","));
		sb.append("]");
		return sb.toString();
	}

	public String paramArrayToDelimitedString(Object[] arr, String delim) {
		if (ObjectUtils.isEmpty(arr)) {
			return "";
		}
		if (arr.length == 1) {
			return ObjectUtils.nullSafeToString(arr[0]);
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			if (i > 0) {
				sb.append(delim);
			}
			sb.append(JSON.toJSONString(arr[i], serializerFeatures));
		}
		return sb.toString();
	}

	/**
	 * 
	 * 
	 * @param arr
	 * @param delim
	 * @return
	 */
	private String arrayToDelimitedString(Class<?>[] arr, String delim) {
		if (ObjectUtils.isEmpty(arr)) {
			return "";
		}
		if (arr.length == 1) {
			return ObjectUtils.nullSafeToString(arr[0]);
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			if (i > 0) {
				sb.append(delim);
			}
			sb.append(arr[i].getName());
		}
		return sb.toString();
	}
}
