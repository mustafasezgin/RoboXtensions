package com.robolx.injector;


import android.app.Activity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.robolx.annotations.Subject;
import com.robolx.injector.exception.InvalidViewIdInTestCaseException;
import com.robolx.injector.exception.NoTestSubjectException;
import com.robolx.injector.exception.NoViewIdSpecifiedException;import com.robolx.injector.exception.TestSubjectAlsoMarkedAsMockException;
import org.apache.commons.lang3.ObjectUtils;
import org.mockito.Mock;
import roboguice.inject.InjectView;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class TestCase {
    private static final int DEFAULT_VIEW_ID = -1;
    private Field testSubjectField;
    private final Collection<Field> mockFields;
    private Object testCaseInstance;
    private Object testSubjectInstance;
    private final Map<Integer, Field> viewFieldsInSubject;
    private final Map<Integer, Field> viewFieldsInTestCase;

    public TestCase(Object testCaseInstance) {
        this.testCaseInstance = testCaseInstance;
        mockFields = Lists.newArrayList();
        viewFieldsInSubject = Maps.newHashMap();
        viewFieldsInTestCase = Maps.newHashMap();
        extractAnnotatedFieldsFromTestCase();
        extractAnnotatedFieldsFromSubject();
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
                throw new InvalidViewIdInTestCaseException(viewIdInTestCase);
            }
        }
    }

    private void extractAnnotatedFieldsFromTestCase() {
        Field[] fields = testCaseInstance.getClass().getDeclaredFields();
        for (Field field : fields) {
            Subject subjectAnnotation = field.getAnnotation(Subject.class);
            if (ObjectUtils.notEqual(subjectAnnotation, null)) {
                testSubjectField = field;
            }

            /**
             * TODO extract fields to mock from the subject rather than testcase
             */
            Mock mockAnnotation = field.getAnnotation(Mock.class);
            if (ObjectUtils.notEqual(mockAnnotation, null)) {
                mockFields.add(field);
            }

            InjectView injectViewAnnotation = field.getAnnotation(InjectView.class);
            if(ObjectUtils.notEqual(injectViewAnnotation, null)){
                if(ObjectUtils.equals(injectViewAnnotation.value(), DEFAULT_VIEW_ID)){
                    throw new NoViewIdSpecifiedException(field);
                }
                viewFieldsInTestCase.put(injectViewAnnotation.value(), field);
            }

        }

        if (ObjectUtils.equals(testSubjectField, null)) {
            throw new NoTestSubjectException();
        }

        if (mockFields.contains(testSubjectField)) {
            throw new TestSubjectAlsoMarkedAsMockException();
        }

    }

    public Class<?> getTestSubjectClassType(){
        return testSubjectField.getType();
    }

    public Field getTestSubjectField(){
        return testSubjectField;
    }

    public Object getTestCaseInstance(){
        return testCaseInstance;
    }

    public Object getTestSubjectInstance() {
        return testSubjectInstance;
    }

    public Activity getActivityTestSubject(){
        return (Activity)testSubjectInstance;
    }

    public void setTestSubjectInstance(Object testSubjectInstance) {
        this.testSubjectInstance = testSubjectInstance;
    }

    public Collection<Field> getFieldsMarkedWithMock() {
        return mockFields;
    }

    public Set<Integer> getViewIds() {
        return viewFieldsInSubject.keySet();
    }

    public Field getFieldForViewWithIdInTestCase(Integer viewId) {
        return viewFieldsInTestCase.get(viewId);
    }

    public Field getFieldForViewWithIdInTestSubject(Integer viewId) {
        return viewFieldsInSubject.get(viewId);
    }
}
