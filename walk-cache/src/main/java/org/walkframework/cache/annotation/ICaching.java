package org.walkframework.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 替代org.springframework.cache.annotation.Caching
 * 
 * @author shf675
 */
@Target( { ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ICaching {

	ICacheable[] cacheable() default {};

	ICachePut[] put() default {};

	ICacheEvict[] evict() default {};

}