package org.walkframework.data.translate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 翻译静态表td_s_static。
 * @author shf675
 *
 */
@Target( {ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface StaticTranslate {
	
	/**
	 * 源值字段。根据此字段取值
	 * @return
	 */
	String by();
	
	/**
	 * 转换规则
	 * 
	 * 填写TD_S_STATIC表的TYPE_ID
	 * @return
	 */
	String typeId();
}
