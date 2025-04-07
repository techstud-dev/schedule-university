package com.techstud.schedule_university.auth.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    int limit() default 5;
    int interval() default 60;
    RateLimitKeyType keyType() default RateLimitKeyType.IP;
}
