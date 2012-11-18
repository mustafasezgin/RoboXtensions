package com.robolx.injector;


import android.app.Activity;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.util.Modules;
import com.robolx.injector.exception.TestSetupException;
import com.robolx.injector.exception.ViewFieldNotInitialisedException;
import com.xtremelabs.robolectric.Robolectric;
import org.apache.commons.lang3.ObjectUtils;
import roboguice.RoboGuice;
import roboguice.activity.RoboActivity;
import roboguice.activity.event.OnContentChangedEvent;
import roboguice.event.EventListener;
import roboguice.event.EventManager;
import roboguice.inject.ContextScope;

import java.lang.reflect.Field;

import static com.robolx.injector.exception.TestSetupException.SetupError;
import static org.mockito.Mockito.mock;

public class TestFieldInjector {
    private static final RoboActivity ACTIVITY_CONTEXT = new RoboActivity();

    private TestCase testCase;


    public void setupTestCase(Object testCase) {
        this.testCase = new TestCase(testCase);

        /**
         * Needs to be done first before the injector is retrieved
         */
        setupBindingsForMocks();

        createTestSubjectAndSetIntoTestCase();

        Activity activityTestSubject = this.testCase.getActivityTestSubject();
        Injector activityInjector = RoboGuice.getInjector(activityTestSubject);
        activityInjector.getInstance(ContextScope.class).enter(activityTestSubject);

        setMocksOnTestCase(activityInjector);

        setupOnCreateListener(activityInjector);

    }



    private void setupOnCreateListener(Injector injector) {
        injector.getInstance(EventManager.class).registerObserver(OnContentChangedEvent.class, new ContentChangedListener());

    }

    private void createTestSubjectAndSetIntoTestCase() {
        Injector activityInjector = RoboGuice.getInjector(ACTIVITY_CONTEXT);
        activityInjector.getInstance(ContextScope.class).enter(ACTIVITY_CONTEXT);

        Object testSubjectInstance = activityInjector.getInstance(testCase.getTestSubjectClassType());
        Field testSubjectField = testCase.getTestSubjectField();
        testSubjectField.setAccessible(true);

        try {
            testSubjectField.set(testCase.getTestCaseInstance(), testSubjectInstance);
        } catch (IllegalAccessException e) {
            throw new TestSetupException(SetupError.COULDNT_SET_FIELD_IN_TEST, e);
        }
        testCase.setTestSubjectInstance(testSubjectInstance);
        activityInjector.getInstance(ContextScope.class).exit(ACTIVITY_CONTEXT);

    }

    private void setMocksOnTestCase(Injector activityInjector) {

        for (Field mockField : testCase.getFieldsMarkedWithMock()) {
            Object instance = activityInjector.getInstance(mockField.getType());
            mockField.setAccessible(true);
            try {
                mockField.set(testCase.getTestCaseInstance(), instance);
            } catch (IllegalAccessException e) {
                throw new TestSetupException(SetupError.COULDNT_SET_FIELD_IN_TEST, e);
            }
        }
    }

    private void setupBindingsForMocks() {
        RoboGuice.setBaseApplicationInjector(Robolectric.application, Stage.DEVELOPMENT,
                Modules.override(new BindingsForMocksModule()).with(RoboGuice.newDefaultRoboModule(Robolectric.application)));
    }

    private class BindingsForMocksModule extends AbstractModule {
        @Override
        protected void configure() {
            for (Field field : testCase.getFieldsMarkedWithMock()) {
                bind((Class) field.getType()).toInstance(mock(field.getType()));
            }
        }
    }

    private class ContentChangedListener implements EventListener {

        @Override
        public void onEvent(Object event) {
            Object testSubjectInstance = testCase.getTestSubjectInstance();

            for(Integer viewIdInTestCase : testCase.getViewIds()){

                Field viewFieldInTestCase = testCase.getFieldForViewWithIdInTestCase(viewIdInTestCase);
                Field viewFieldInSubject = testCase.getFieldForViewWithIdInTestSubject(viewIdInTestCase);

                viewFieldInTestCase.setAccessible(true);
                viewFieldInSubject.setAccessible(true);

                try {
                    Object view = viewFieldInSubject.get(testSubjectInstance);
                    if(ObjectUtils.equals(view, null)){
                        throw new ViewFieldNotInitialisedException(viewFieldInSubject);
                    }
                    viewFieldInTestCase.set(testCase.getTestCaseInstance(), view);
                } catch (IllegalAccessException e) {
                    throw new TestSetupException(SetupError.COULDNT_SET_FIELD_IN_TEST, e);
                }
            }
        }
    }
}
