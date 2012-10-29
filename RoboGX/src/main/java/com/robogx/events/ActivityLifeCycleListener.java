package com.robogx.events;


import android.content.Context;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.robogx.events.exceptions.EventProcessingException;
import roboguice.RoboGuice;
import roboguice.event.EventListener;
import roboguice.event.EventManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public class ActivityLifeCycleListener implements EventListener {

    private EventManager eventManager;
    private ArrayListMultimap<LifeCycleEvent, SubscriberReference> subscribersByEvents;
    private Map<Object, SubscriberReference> subscribersByObject;

    public ActivityLifeCycleListener(Context context) {
        this(RoboGuice.getInjector(context).getInstance(EventManager.class));
    }

    protected ActivityLifeCycleListener(EventManager eventManager){
        this.eventManager = eventManager;
        this.subscribersByEvents = ArrayListMultimap.create();
        this.subscribersByObject = Maps.newHashMap();
        for(LifeCycleEvent event : LifeCycleEvent.values()){
            eventManager.registerObserver(event.getRoboguiceEventClass(), this);
        }
    }


    public void register(Object subscriber){
        Preconditions.checkNotNull(subscriber, "Cannot register null subscriber");
        SubscriberReference subscriberReference = new SubscriberReference(subscriber);
        Collection<LifeCycleEvent> lifeCycleEvents = subscriberReference.getEventsBeingListenedTo();

        subscribersByObject.put(subscriber, subscriberReference);

        for (LifeCycleEvent event : lifeCycleEvents){
            subscribersByEvents.put(event, subscriberReference);
        }

    }

    public void unregister(Object subscriber){
        SubscriberReference subscriberReference = subscribersByObject.get(subscriber);
        Preconditions.checkNotNull(subscriberReference, "Cannot unregister subscriber which has not been registered");
        for(LifeCycleEvent event : subscriberReference.getEventsBeingListenedTo()){
            subscribersByEvents.remove(event, subscriberReference);
        }

        subscribersByObject.remove(subscriber);
    }

    @Override
    public void onEvent(Object roboguiceEvent) {
        LifeCycleEvent lifeCycleEvent = LifeCycleEvent.eventForRoboguiceEventClass(roboguiceEvent);
        for(SubscriberReference subscriber : subscribersByEvents.get(lifeCycleEvent)){
            subscriber.callEventMethod(lifeCycleEvent);
        }
        if(LifeCycleEvent.ON_DESTROY.equals(lifeCycleEvent)){
            unregisterFromEventManager();
            clearAllSubscribers();
        }
    }

    private void clearAllSubscribers() {
        subscribersByEvents.clear();
        subscribersByObject.clear();
    }

    @SuppressWarnings("unchecked")
    private void unregisterFromEventManager() {
        for(LifeCycleEvent event : LifeCycleEvent.values()){
            eventManager.unregisterObserver(event.getRoboguiceEventClass(), this);
        }
    }

    private static class SubscriberReference{
        private final Object subscriber;
        private final EnumMap<LifeCycleEvent, Method> eventMethods;
        
        public SubscriberReference(Object subscriber){
            this.subscriber = subscriber;
            this.eventMethods= new EnumMap<LifeCycleEvent, Method>(LifeCycleEvent.class);
            extractAnnotatedMethods();
        }

        private void extractAnnotatedMethods() {
            /**
             * This will not pickup inherited methods
             */
            for(Method method : subscriber.getClass().getDeclaredMethods()) {
                for(Annotation annotation : method.getDeclaredAnnotations()){
                    if(LifeCycleEvent.isLifeCycleAnnotation(annotation)){
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
}
