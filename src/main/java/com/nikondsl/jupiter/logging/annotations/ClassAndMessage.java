package com.nikondsl.jupiter.logging.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
public @interface ClassAndMessage {
    Class<? extends Throwable> clazz();
    String message();
}
