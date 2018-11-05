package org.walkframework.data.translate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 翻译普通表，根据某字段翻译某字段。
 * @author shf675
 *
 */
@Target( {ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface TableTranslate {
	
	/**
	 * 源值字段。根据此字段取值
	 * @return
	 */
	String by();
	
	/**
	 * 转换规则
	 * 
	 * 规则：TD_M_STAFF.STAFF_ID.STAFF_NAME。表示从TD_M_STAFF表根据STAFF_ID翻译STAFF_NAME
	 * @return
	 */
	String path();
}
