package com.robogx.events.exceptions;

import java.lang.reflect.Method;

public class EventMethodNotAccessibleException extends RuntimeException{
    private static String MSG_FORMAT = "Method %s on %s is not public, can only register publicly accessible methods";

    public EventMethodNotAccessibleException(Method method, Object subscriber) {
        super(String.format(MSG_FORMAT, method.getName(), subscriber.getClass().getName()));

    }
}
