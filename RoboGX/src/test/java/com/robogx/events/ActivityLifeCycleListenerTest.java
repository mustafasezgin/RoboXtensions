package com.robogx.events;

import com.robogx.events.exceptions.EventMethodNotAccessibleException;
import com.robogx.runner.RoboGXTestRunner;
import com.xtremelabs.robolectric.Robolectric;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import roboguice.RoboGuice;
import roboguice.activity.event.OnCreateEvent;
import roboguice.activity.event.OnDestroyEvent;
import roboguice.activity.event.OnPauseEvent;
import roboguice.activity.event.OnResumeEvent;
import roboguice.event.EventManager;

import static com.robogx.events.TestSubscriberObjects.InvalidTestSubscriber;
import static com.robogx.events.TestSubscriberObjects.TestSubscriber;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RoboGXTestRunner.class)
public class ActivityLifeCycleListenerTest {

    private ActivityLifeCycleListener activityLifeCycleListener;
    private EventManager eventManager;
    private TestSubscriber testSubscriber;

    @Before
    public void setUp() {
        initMocks(this);
        testSubscriber = new TestSubscriber();
        activityLifeCycleListener = new ActivityLifeCycleListener(Robolectric.application);
        eventManager = RoboGuice.getInjector(Robolectric.application).getInstance(EventManager.class);
    }

    @Test
    public void shouldCallOnCreateAnnotatedMethodWhenOnCreateEventFired() throws Exception {
        activityLifeCycleListener.register(testSubscriber);
        eventManager.fire(new OnCreateEvent(null));
        assertThat(testSubscriber.onCreateOnlyCalled(), is(true));
    }

    @Test
    public void shouldCallOnResumeAnnotatedMethodWhenOnResumeEventFired() throws Exception {
        activityLifeCycleListener.register(testSubscriber);
        eventManager.fire(new OnResumeEvent());
        assertThat(testSubscriber.onResumeOnlyCalled(), is(true));
    }

    @Test
    public void shouldCallOnPauseAnnotatedMethodWhenOnPauseEventFired() throws Exception {
        activityLifeCycleListener.register(testSubscriber);
        eventManager.fire(new OnPauseEvent());
        assertThat(testSubscriber.onPauseOnlyCalled(), is(true));
    }

    @Test
    public void shouldCallOnDestroyAnnotatedMethodWhenOnDestroyEventFired() throws Exception {
        activityLifeCycleListener.register(testSubscriber);
        eventManager.fire(new OnDestroyEvent());
        assertThat(testSubscriber.onDestroyOnlyCalled(), is(true));
    }

    @Test
    public void shouldUnregisterFromEventManagerWhenOnDestroyIsCalled(){
        activityLifeCycleListener.register(testSubscriber);
        eventManager.fire(new OnDestroyEvent());
        testSubscriber.resetEvents();
        eventManager.fire(new OnCreateEvent(null));
        eventManager.fire(new OnDestroyEvent());
        assertThat(testSubscriber.noEventsCalled(), is(true));
    }

    @Test
    public void shouldOnlyCallExpectedMethodsWithMultipleSubscribers(){
        TestSubscriber testSubscriberTwo = new TestSubscriber();
        activityLifeCycleListener.register(testSubscriber);
        activityLifeCycleListener.register(testSubscriberTwo);
        eventManager.fire(new OnPauseEvent());
        assertThat(testSubscriber.onPauseOnlyCalled(), is(true));
        assertThat(testSubscriberTwo.onPauseOnlyCalled(), is(true));
    }

    @Test
    public void shouldUnregisterObject(){
        activityLifeCycleListener.register(testSubscriber);
        activityLifeCycleListener.unregister(testSubscriber);
        eventManager.fire(new OnCreateEvent(null));
        eventManager.fire(new OnDestroyEvent());
        eventManager.fire(new OnPauseEvent());
        eventManager.fire(new OnResumeEvent());
        assertThat(testSubscriber.noEventsCalled(), is(true));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionIfTryingToUnregisterObjectWhichHasntBeenRegistered(){
        activityLifeCycleListener.unregister(testSubscriber);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionIfTryingToRegisterNullSubscriber(){
        activityLifeCycleListener.register(null);
    }

    @Test(expected = EventMethodNotAccessibleException.class)
    public void shouldThrowExceptionIfTryingToRegisterSubscriberWithPrivateMethods(){
        activityLifeCycleListener.register(new InvalidTestSubscriber());
    }
}
