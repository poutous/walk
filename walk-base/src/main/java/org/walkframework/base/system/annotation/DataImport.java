package org.walkframework.base.system.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * 数据导入注解
 * 
 * @author shf675
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DataImport {
	
	/**
	 * 导入文件对象name
	 * 
	 * @return
	 */
	String fileName();
	
	/**
	 * xml文件路径
	 * @return
	 */
	String xml();
	
	/**
	 * List中元素类型。默认为Map
	 * 
	 * @return
	 */
	Class<?> type() default Map.class;

}