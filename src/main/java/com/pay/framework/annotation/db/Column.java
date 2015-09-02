package com.pay.framework.annotation.db;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义数据库字段名
 * @author PCCW
 *
 */
@Inherited
@Target({ElementType.FIELD})//在属性上起作用
@Retention(RetentionPolicy.RUNTIME) //运行时起作用
@Documented
public @interface Column {
	/**
	 * 字段名
	 * @return
	 */
	String value() default "";
}
