package com.kenvix.utils.android.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoLoadOption {
    boolean enabled() default true;
    boolean suppressError() default false;
}
