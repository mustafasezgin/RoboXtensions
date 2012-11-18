package com.robolx.injector.exception;

import java.lang.reflect.Field;


public class NoViewIdSpecifiedException extends RuntimeException {
    private static final String MSG_FORMAT = "Field %s in test case has not specified a view Id in its annotation";
    public NoViewIdSpecifiedException(Field field) {
        super(String.format(MSG_FORMAT, field.getName()));
    }
}
