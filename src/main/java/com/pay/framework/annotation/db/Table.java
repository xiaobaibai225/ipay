package com.pay.framework.annotation.db;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义数据库表名
 * @author PCCW
 *
 */
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME) //运行时起作用
@Documented
public @interface Table {
	/**
	 * 表名
	 * @return
	 */
	String value() default "";
}
