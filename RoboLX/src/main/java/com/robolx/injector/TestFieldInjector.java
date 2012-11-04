package com.robolx.injector;


import android.app.Activity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.util.Modules;
import com.robolx.annotations.Subject;
import com.robolx.injector.exception.TestSetupException;
import com.xtremelabs.robolectric.Robolectric;
import org.apache.commons.lang3.ObjectUtils;
import org.mockito.Mock;
import roboguice.RoboGuice;
import roboguice.activity.RoboActivity;
import roboguice.activity.event.OnContentChangedEvent;
import roboguice.event.EventListener;
import roboguice.event.EventManager;
import roboguice.inject.ContextScope;
import roboguice.inject.InjectView;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import static com.robolx.injector.exception.TestSetupException.SetupError;
import static org.mockito.Mockito.mock;

public class TestFieldInjector {
    private static final RoboActivity ACTIVITY_CONTEXT = new RoboActivity();

    private Field testSubjectField;
    private final Collection<Field> mockFields;
    private Object testCase;
    private Object testSubjectInstance;
    private final Map<Integer, Field> viewFieldsInSubject;
    private final Map<Integer, Field> viewFieldsInTestCase;

    public TestFieldInjector() {
        mockFields = Lists.newArrayList();
        viewFieldsInSubject = Maps.newHashMap();
        viewFieldsInTestCase = Maps.newHashMap();
    }

    public void setupTestCase(Object testCase) {
        this.testCase = testCase;

        extractAnnotatedFieldsFromTestCase();
        extractAnnotatedFieldsFromSubject();
        /**
         * Needs to be done first before the injector is retrieved
         */
        setupBindingsForMocks();

        injectSubjectIntoTest();

        Injector activityInjector = RoboGuice.getInjector((Activity)testSubjectInstance);
        activityInjector.getInstance(ContextScope.class).enter((Activity)testSubjectInstance);

        setMocksOnTestCase(activityInjector);

        setupOnCreateListener(activityInjector);

    }

    private void extractAnnotatedFieldsFromSubject() {
        Field[] fields = testSubjectField.getType().getDeclaredFields();
        for (Field field : fields) {
            InjectView injectViewAnnotation = field.getAnnotation(InjectView.class);
            if (ObjectUtils.notEqual(injectViewAnnotation, null)) {
                viewFieldsInSubject.put(injectViewAnnotation.value(), field);
            }
        }

        for(Integer viewIdInTestCase : viewFieldsInTestCase.keySet()){
            if(!viewFieldsInSubject.containsKey(viewIdInTestCase)){
                throw new TestSetupException(SetupError.INVALID_VIEW_ID_IN_TEST_CASE);
            }
        }
    }

    private void setupOnCreateListener(Injector injector) {
        injector.getInstance(EventManager.class).registerObserver(OnContentChangedEvent.class, new ContentChangedListener());

    }

    private void injectSubjectIntoTest() {
        Injector activityInjector = RoboGuice.getInjector(ACTIVITY_CONTEXT);
        activityInjector.getInstance(ContextScope.class).enter(ACTIVITY_CONTEXT);

        testSubjectInstance = activityInjector.getInstance(testSubjectField.getType());
        testSubjectField.setAccessible(true);

        try {
            testSubjectField.set(testCase, testSubjectInstance);
        } catch (IllegalAccessException e) {
            throw new TestSetupException(SetupError.COULDNT_SET_FIELD_IN_TEST, e);
        }

        activityInjector.getInstance(ContextScope.class).exit(ACTIVITY_CONTEXT);

    }

    private void setMocksOnTestCase(Injector activityInjector) {
        for (Field mockField : mockFields) {
            Object instance = activityInjector.getInstance(mockField.getType());
            mockField.setAccessible(true);
            try {
                mockField.set(testCase, instance);
            } catch (IllegalAccessException e) {
                throw new TestSetupException(SetupError.COULDNT_SET_FIELD_IN_TEST, e);
            }
        }
    }

    private void setupBindingsForMocks() {
        RoboGuice.setBaseApplicationInjector(Robolectric.application, Stage.DEVELOPMENT,
                Modules.override(new BindingsForMocksModule()).with(RoboGuice.newDefaultRoboModule(Robolectric.application)));
    }

    private void extractAnnotatedFieldsFromTestCase() {
        Field[] fields = testCase.getClass().getDeclaredFields();
        for (Field field : fields) {
            Subject subjectAnnotation = field.getAnnotation(Subject.class);
            if (ObjectUtils.notEqual(subjectAnnotation, null)) {
                testSubjectField = field;
            }

            /**
             * TODO extract fields to mock from the subject rather
             * than testcase
             */
            Mock mockAnnotation = field.getAnnotation(Mock.class);
            if (ObjectUtils.notEqual(mockAnnotation, null)) {
                mockFields.add(field);
            }

            InjectView injectViewAnnotation = field.getAnnotation(InjectView.class);
            if(ObjectUtils.notEqual(injectViewAnnotation, null)){
                viewFieldsInTestCase.put(injectViewAnnotation.value(), field);
            }

        }

        if (ObjectUtils.equals(testSubjectField, null)) {
            throw new TestSetupException(SetupError.NO_TEST_SUBJECT);
        }

        if (mockFields.contains(testSubjectField)) {
            throw new TestSetupException(SetupError.SUBJECT_MARKED_AS_MOCK);
        }

    }

    private class BindingsForMocksModule extends AbstractModule {
        @Override
        protected void configure() {
            for (Field field : mockFields) {
                bind((Class) field.getType()).toInstance(mock(field.getType()));
            }
        }
    }

    private class ContentChangedListener implements EventListener {

        @Override
        public void onEvent(Object event) {
            for(Integer viewIdInTestCase : viewFieldsInTestCase.keySet()){
                Field viewFieldInTestCase = viewFieldsInTestCase.get(viewIdInTestCase);
                Field viewFieldInSubject = viewFieldsInSubject.get(viewIdInTestCase);
                viewFieldInTestCase.setAccessible(true);
                viewFieldInSubject.setAccessible(true);
                try {
                    Object view = viewFieldInSubject.get(testSubjectInstance);
                    if(ObjectUtils.equals(view, null)){
                        throw new TestSetupException(SetupError.VIEW_FIELD_NOT_INITIALISED);
                    }
                    viewFieldInTestCase.set(testCase, view);
                } catch (IllegalAccessException e) {
                    throw new TestSetupException(SetupError.COULDNT_SET_FIELD_IN_TEST, e);
                }
            }
        }
    }
}
