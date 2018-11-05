package org.walkframework.cache.annotation;

import java.lang.reflect.Method;

import org.springframework.cache.interceptor.SimpleKeyGenerator;

/**
 * 自定义缓存key生成器
 * 
 * @author shf675
 * 
 */
public class CustomSimpleKeyGenerator extends SimpleKeyGenerator {

	@Override
	public Object generate(Object target, Method method, Object... params) {
		return new CustomSimpleKey(method, params);
	}
}
