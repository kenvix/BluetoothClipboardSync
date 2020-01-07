package com.kenvix.utils.android.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Documented
@SuppressWarnings("UnusedDeclaration")
@Target(ElementType.FIELD)
public @interface ViewAutoLoad {
    int value() default 0;
}
