package org.walkframework.data.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	String name() default "";
	
	int type() default 12;

	boolean nullable() default true;

	int length() default 255;

	int precision() default 0;

	int scale() default 0;
	
	boolean isAutoIncrement() default false;
}
