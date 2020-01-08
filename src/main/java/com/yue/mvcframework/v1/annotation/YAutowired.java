package com.yue.mvcframework.v1.annotation;

import java.lang.annotation.*;

/**
 * created by Mr.huang on 2020/1/8
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YAutowired {
    String value() default "";
}
