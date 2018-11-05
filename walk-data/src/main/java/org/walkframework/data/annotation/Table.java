package org.walkframework.data.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( { java.lang.annotation.ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
	public abstract String name() default "";

	public abstract String catalog() default "";

	public abstract String schema() default "";

}