package com.robogx.events;

import com.robogx.events.annotations.OnCreate;
import com.robogx.events.annotations.OnDestroy;
import com.robogx.events.annotations.OnPause;
import com.robogx.events.annotations.OnResume;
import com.robogx.events.annotations.OnStart;
import com.robogx.events.annotations.OnStop;

import java.util.EnumSet;

public class TestSubscriberObjects {

    public static class InvalidTestSubscriber{
        @OnCreate
        private void onCreate(){

        }
    }
    public static class TestSubscriber {
        private EnumSet<LifeCycleEvent> calledEvents = EnumSet.noneOf(LifeCycleEvent.class);

        @OnCreate
        public void onCreate() {
            calledEvents.add(LifeCycleEvent.ON_CREATE);
        }

        public boolean onCreateOnlyCalled() {
            return isOnlyEventCalled(LifeCycleEvent.ON_CREATE);
        }

        @OnDestroy
        public void onDestroy() {
            calledEvents.add(LifeCycleEvent.ON_DESTROY);
        }

        public boolean onDestroyOnlyCalled() {
            return isOnlyEventCalled(LifeCycleEvent.ON_DESTROY);
        }

        @OnPause
        public void onPause() {
            calledEvents.add(LifeCycleEvent.ON_PAUSE);
        }

        public boolean onPauseOnlyCalled() {
            return isOnlyEventCalled(LifeCycleEvent.ON_PAUSE);
        }

        @OnResume
        public void onResume() {
            calledEvents.add(LifeCycleEvent.ON_RESUME);
        }

        public boolean onResumeOnlyCalled() {
            return isOnlyEventCalled(LifeCycleEvent.ON_RESUME);
        }

        @OnStop
        public void onStop() {
            calledEvents.add(LifeCycleEvent.ON_START);
        }

        public boolean onStopOnlyCalled() {
            return isOnlyEventCalled(LifeCycleEvent.ON_STOP);
        }

        @OnStart
        public void onStart() {
            calledEvents.add(LifeCycleEvent.ON_START);
        }

        public boolean onStartOnlyCalled() {
            return isOnlyEventCalled(LifeCycleEvent.ON_START);
        }

        private boolean isOnlyEventCalled(LifeCycleEvent event) {
            return calledEvents.size() == 1 && calledEvents.contains(event);
        }

        public void resetEvents() {
            calledEvents.clear();
        }

        public boolean noEventsCalled() {
            return calledEvents.isEmpty();
        }
    }
}
