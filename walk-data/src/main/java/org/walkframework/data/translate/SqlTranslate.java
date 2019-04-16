package org.walkframework.data.translate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 通过sqlId翻译注解
 * 
 * @author shf675
 *
 */
@Target( {ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SqlTranslate {
	
	/**
	 * mybatis里的sqlId
	 * 
	 * @return
	 */
	String sqlId();
	
}
