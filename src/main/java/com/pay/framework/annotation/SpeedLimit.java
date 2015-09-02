package com.pay.framework.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在 controller类标注{@link SpeedLimit} 
 * 或者在某方法标注 {@link SpeedLimit} 
 * 表示在 period 内 最多访问 times 次
 * 默认是在1秒内最多访问5次
 * @author PCCW
 *
 */
@Inherited
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME) //运行时起作用
@Documented
public @interface SpeedLimit {
//	//时间段（秒） 
	public int period() default 60;
	//时间段内的次数
	public int times() default 1;
}
