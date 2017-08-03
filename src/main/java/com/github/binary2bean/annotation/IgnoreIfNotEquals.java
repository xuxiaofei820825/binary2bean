package com.github.binary2bean.annotation;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * property指定的属性的值与value值相等时，才做解码。否则忽略该属性。
 * 
 * @author xiaofei.xu
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(FIELD)
@Documented
public @interface IgnoreIfNotEquals {
	/** 属性名 */
	String property() default "";

	/** 属性比较的值 */
	int value() default 0;
}
