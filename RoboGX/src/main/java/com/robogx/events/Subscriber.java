package com.robogx.events;

import com.google.common.base.Preconditions;
import com.robogx.events.exceptions.EventMethodNotAccessibleException;
import com.robogx.events.exceptions.EventProcessingException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.EnumMap;

/**
 * Represents an object who is subscribing for its activities events via the Event Manager
 */
class Subscriber {
    private final Object subscriber;
    private final EnumMap<LifeCycleEvent, Method> eventMethods;

    public Subscriber(Object subscriber){
        this.subscriber = subscriber;
        this.eventMethods= new EnumMap<LifeCycleEvent, Method>(LifeCycleEvent.class);
        extractAnnotatedMethods();
    }

    private void extractAnnotatedMethods() {
        /**
         * TODO This will not pickup inherited methods
         */
        for(Method method : subscriber.getClass().getDeclaredMethods()) {
            for(Annotation annotation : method.getDeclaredAnnotations()){
                if(LifeCycleEvent.isLifeCycleAnnotation(annotation)){
                    if(method.getModifiers() != Modifier.PUBLIC){
                        throw new EventMethodNotAccessibleException(method, subscriber);
                    }
                    eventMethods.put(LifeCycleEvent.eventForAnnotation(annotation), method);
                }
            }
        }
    }


    public Collection<LifeCycleEvent> getEventsBeingListenedTo() {
        return eventMethods.keySet();
    }
    //TODO Need to handle events with arguments e.g. bundle in onCreateEvent
    public void callEventMethod(LifeCycleEvent lifeCycleEvent) {
        Preconditions.checkArgument(eventMethods.containsKey(lifeCycleEvent), "Cannot find any methods for event " + lifeCycleEvent.name());
        Method eventMethod = eventMethods.get(lifeCycleEvent);
        try {
            eventMethod.invoke(subscriber);
        } catch (Exception e) {
            throw new EventProcessingException(lifeCycleEvent,eventMethod,subscriber, e);
        }
    }
}
