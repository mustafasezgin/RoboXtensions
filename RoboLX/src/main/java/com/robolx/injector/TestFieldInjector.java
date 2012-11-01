package com.robolx.injector;


import com.google.common.collect.Lists;
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
import roboguice.inject.ContextScope;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;

import static com.robolx.injector.exception.TestSetupException.SetupError;
import static org.mockito.Mockito.mock;

public class TestFieldInjector {
    private static final RoboActivity ACTIVITY_CONTEXT = new RoboActivity();

    private Field subjectField;
    private final Collection<Field> mockFields;

    public TestFieldInjector() {
        mockFields = Lists.newArrayList();
    }

    public void setupTestCase(Object testCase) {
        extractAnnotatedFields(testCase);

        injectSubjectIntoTest(testCase);

    }

    private void injectSubjectIntoTest(Object testCase) {

        setupBindingsForMocks();

        Injector activityInjector = RoboGuice.getInjector(ACTIVITY_CONTEXT);
        activityInjector.getInstance(ContextScope.class).enter(ACTIVITY_CONTEXT);

        instantiateAndSetTestSubject(testCase, activityInjector);
        setMocksOnTestCase(testCase, activityInjector);
    }

    private void setMocksOnTestCase(Object testCase, Injector activityInjector) {
        for(Field mockField : mockFields){
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

    private void instantiateAndSetTestSubject(Object testCase, Injector activityInjector) {
        Object testSubjectInstance = activityInjector.getInstance(subjectField.getType());
        subjectField.setAccessible(true);

        try {
            subjectField.set(testCase, testSubjectInstance);
        } catch (IllegalAccessException e) {
            throw new TestSetupException(SetupError.COULDNT_SET_FIELD_IN_TEST, e);
        }
    }

    private void extractAnnotatedFields(Object test) {
        Field[] fields = test.getClass().getDeclaredFields();
        for (Field field : fields) {
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                if (Subject.class.equals(annotation.annotationType())) {
                    subjectField = field;
                }
                if (Mock.class.equals(annotation.annotationType())) {
                    mockFields.add(field);
                }
            }

        }
        if(ObjectUtils.equals(subjectField,null)){
            throw new TestSetupException(SetupError.NO_TEST_SUBJECT);
        }

        if (mockFields.contains(subjectField)) {
            throw new TestSetupException(SetupError.SUBJECT_MARKED_AS_MOCK);
        }
    }

    private class BindingsForMocksModule extends AbstractModule {
        @Override
        protected void configure() {
            for (Field field : mockFields) {
                bind((Class)field.getType()).toInstance(mock(field.getType()));
            }
        }
    }
}
