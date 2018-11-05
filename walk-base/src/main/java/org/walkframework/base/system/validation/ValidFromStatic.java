package org.walkframework.base.system.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * 从td_s_static表校验
 * 
 * @ValidStatic("USER_TYPE")
 * @ValidStatic(value="USER_TYPE", allow=false)
 * @ValidStatic(value={"USER_TYPE1", "USER_TYPE2"})
 * 
 * @author shf675
 *
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {ValidFromStaticValidator.class})
public @interface ValidFromStatic {

	/**
	 * td_s_static.type_id
	 * 
	 * @return
	 */
	String[] value();
	
	/**
	 * true时表示为白名单，列表项中的值允许，默认为true
	 * false表示为黑名单，列表项中的值禁止
	 * 
	 * @return
	 */
	boolean allow() default true;
	
	/**
	 * @return
	 */
	String message() default "该值不在允许的列表值中";
	
	/**
	 * @return the groups the constraint belongs to
	 */
	Class<?>[] groups() default { };

	/**
	 * @return the payload associated to the constraint
	 */
	Class<? extends Payload>[] payload() default { };
}
