package com.github.binary2bean.annotation;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ FIELD })
@Documented
public @interface NBits {

	/** 需要预读的字节数 */
	short bytes() default 1;

	/** 开始比特位 */
	short offset() default 1;

	/** 结束比特位 */
	short len();
}
