package com.robogx.events;


import android.content.Context;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import roboguice.RoboGuice;
import roboguice.event.EventListener;
import roboguice.event.EventManager;

import java.util.Collection;
import java.util.Map;

public class ActivityLifeCycleListener implements EventListener {

    private EventManager eventManager;
    private ArrayListMultimap<LifeCycleEvent, Subscriber> subscribersByEvents;
    private Map<Object, Subscriber> subscribersByObject;

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
        Subscriber subscriberReference = new Subscriber(subscriber);
        Collection<LifeCycleEvent> lifeCycleEvents = subscriberReference.getEventsBeingListenedTo();

        subscribersByObject.put(subscriber, subscriberReference);

        for (LifeCycleEvent event : lifeCycleEvents){
            subscribersByEvents.put(event, subscriberReference);
        }
    }

    public void unregister(Object subscriber){
        Subscriber subscriberReference = subscribersByObject.get(subscriber);
        Preconditions.checkNotNull(subscriberReference, "Cannot unregister subscriber which has not been registered");
        for(LifeCycleEvent event : subscriberReference.getEventsBeingListenedTo()){
            subscribersByEvents.remove(event, subscriberReference);
        }

        subscribersByObject.remove(subscriber);
    }

    @Override
    public void onEvent(Object roboguiceEvent) {
        LifeCycleEvent lifeCycleEvent = LifeCycleEvent.eventForRoboguiceEventClass(roboguiceEvent);
        for(Subscriber subscriber : subscribersByEvents.get(lifeCycleEvent)){
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
}
