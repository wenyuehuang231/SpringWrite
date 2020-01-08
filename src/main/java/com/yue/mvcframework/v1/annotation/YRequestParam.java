package com.yue.mvcframework.v1.annotation;

import java.lang.annotation.*;

/**
 * created by Mr.huang on 2020/1/8
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YRequestParam {
    String value() default "";
}