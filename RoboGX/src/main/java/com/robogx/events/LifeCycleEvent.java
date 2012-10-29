package com.robogx.events;


import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.robogx.events.annotations.OnCreate;
import com.robogx.events.annotations.OnDestroy;
import com.robogx.events.annotations.OnPause;
import com.robogx.events.annotations.OnResume;
import com.robogx.events.annotations.OnStart;
import com.robogx.events.annotations.OnStop;
import roboguice.activity.event.OnCreateEvent;
import roboguice.activity.event.OnDestroyEvent;
import roboguice.activity.event.OnPauseEvent;
import roboguice.activity.event.OnResumeEvent;
import roboguice.activity.event.OnStartEvent;
import roboguice.activity.event.OnStopEvent;

import java.lang.annotation.Annotation;
import java.util.Map;

public enum LifeCycleEvent {


    ON_CREATE(OnCreate.class, OnCreateEvent.class),
    ON_DESTROY(OnDestroy.class, OnDestroyEvent.class),
    ON_PAUSE(OnPause.class, OnPauseEvent.class),
    ON_RESUME(OnResume.class, OnResumeEvent.class),
    ON_START(OnStart.class, OnStartEvent.class),
    ON_STOP(OnStop.class, OnStopEvent.class);

    private static final Map<Class<?>, LifeCycleEvent> LIFECYCLES_EVENTS_BY_CLASS = Maps.newHashMap();

    private static final Map<Class<? extends Annotation>, LifeCycleEvent> LIFECYCLES_EVENTS_BY_ANNOTATION = Maps.newHashMap();
    static {
        LIFECYCLES_EVENTS_BY_CLASS.put(OnCreateEvent.class, LifeCycleEvent.ON_CREATE);
        LIFECYCLES_EVENTS_BY_CLASS.put(OnDestroyEvent.class, LifeCycleEvent.ON_DESTROY);
        LIFECYCLES_EVENTS_BY_CLASS.put(OnResumeEvent.class, LifeCycleEvent.ON_RESUME);
        LIFECYCLES_EVENTS_BY_CLASS.put(OnPauseEvent.class, LifeCycleEvent.ON_PAUSE);
        LIFECYCLES_EVENTS_BY_CLASS.put(OnStartEvent.class, LifeCycleEvent.ON_START);
        LIFECYCLES_EVENTS_BY_CLASS.put(OnStopEvent.class, LifeCycleEvent.ON_STOP);

        for(LifeCycleEvent event : values()){
            LIFECYCLES_EVENTS_BY_ANNOTATION.put(event.getEventAnnotationClass(), event);
        }
    }

    private final Class<? extends Annotation> annotationClass;
    private final Class<?> roboguiceEventClass;

    LifeCycleEvent(Class<? extends Annotation> annotationClass, Class<?> roboguiceEventClass) {
        this.annotationClass = annotationClass;
        this.roboguiceEventClass = roboguiceEventClass;
    }

    public Class<?> getRoboguiceEventClass(){
        return roboguiceEventClass;
    }

    public Class<? extends Annotation> getEventAnnotationClass(){
        return annotationClass;
    }

    public static LifeCycleEvent eventForRoboguiceEventClass(Object event){
       Preconditions.checkArgument(isRegisteredRoboguiceEventClass(event), "Roboguice Event class not recognised");
       return LIFECYCLES_EVENTS_BY_CLASS.get(event.getClass());
    }

    public static boolean isRegisteredRoboguiceEventClass(Object event){
        return LIFECYCLES_EVENTS_BY_CLASS.containsKey(event.getClass());
    }

    public static boolean isLifeCycleAnnotation(Annotation annotation){
        return LIFECYCLES_EVENTS_BY_ANNOTATION.containsKey(annotation.annotationType());
    }

    public static LifeCycleEvent eventForAnnotation(Annotation annotation){
        Preconditions.checkArgument(isLifeCycleAnnotation(annotation), "Annotation is not a LifeCycle Annotation");
        return LIFECYCLES_EVENTS_BY_ANNOTATION.get(annotation.annotationType());
    }
}
