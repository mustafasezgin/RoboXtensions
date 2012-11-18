package com.robolx.injector.exception;

import java.lang.reflect.Field;


public class ViewFieldNotInitialisedException extends RuntimeException {

    public static final String MSG_FORMAT = "View field in test subject (%s) was null when trying to set it into the test";

    public ViewFieldNotInitialisedException(Field field) {
        super(String.format(MSG_FORMAT, field.getName()));

    }
}
