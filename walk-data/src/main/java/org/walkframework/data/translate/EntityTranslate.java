package org.walkframework.data.translate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实体类翻译注解
 * 
 * @author shf675
 *
 */
@Target( {ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityTranslate {
	
	/**
	 * 条件表达式
	 * @return
	 */
	String conditions();
	
	/** 缓存秒数
	 * 
	 * @return
	 */
	int cacheSeconds() default 0 ;
}
