package com.robogx.events.exceptions;

import com.robogx.events.LifeCycleEvent;

import java.lang.reflect.Method;

public class EventProcessingException extends RuntimeException {
    private static String MSG_FORMAT = "Could not process event %s by calling method %s on subscriber %s";
    public EventProcessingException(LifeCycleEvent event, Method method, Object subscriber, Exception e) {
        super(String.format(MSG_FORMAT, event.name(), method.getName(), subscriber.getClass().getName()),e);
    }
}
