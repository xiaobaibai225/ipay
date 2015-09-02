package com.pay.framework.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在 controller类标注{@link LoginRequired} 表示该controller 需要用户登录才能访问 
 * 或者在某方法标注 {@link LoginRequired} 表示该方法必须登录才能访问
 * @author PCCW
 *
 */
@Inherited
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoginRequired {
	
	//只要加上LoginReqired 标签 ，默认是
	public boolean check() default true;
}
