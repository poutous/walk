package org.walkframework.data.translate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 可自定义翻译器。translator可指定为翻译器service名，或类名xx.class.getName();
 * 
 * @author shf675
 *
 */
@Target( {ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Translate {
	
	/**
	 * 指定转换器
	 * 根据class
	 * 
	 * @return
	 */
	Class<? extends Translator> translator() default Translator.class;
	
	/**
	 * 指定转换器
	 * 根据class全路径
	 * 
	 * @return
	 */
	String translatorClassName() default "";
}
