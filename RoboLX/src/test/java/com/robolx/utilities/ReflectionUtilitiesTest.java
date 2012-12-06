package com.robolx.utilities;

import android.app.Activity;
import android.support.v4.app.Fragment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.cglib.proxy.MethodProxy;
import roboguice.fragment.RoboFragment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.Timer;
import java.util.TreeSet;

import static com.robolx.utilities.ReflectionUtilities.MethodInterceptor;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ReflectionUtilitiesTest {
    private ReflectionUtilities reflectionUtilities;

    @Before
    public void setUp() {
        reflectionUtilities = new ReflectionUtilities();
    }

    @Test
    public void shouldReturnTrueIfObjectIsOfSpecifiedType(){
        assertThat(reflectionUtilities.objectIsOfAnyType(new Date(), Date.class), is(true));
    }

    @Test
    public void shouldReturnFalseIfObjectIsNotOfSpecifiedType(){
        assertThat(reflectionUtilities.objectIsOfAnyType(new Date(), Field.class), is(false));
    }

    @Test
    public void shouldReturnTrueIfObjectImplementsSpecifiedInterface(){
        assertThat(reflectionUtilities.objectIsOfAnyType(new ArrayList(), Collection.class), is(true));
    }

    @Test
    public void shouldReturnFalseIfObjectDoesNotImplementSpecifiedInterface(){
        assertThat(reflectionUtilities.objectIsOfAnyType(new ArrayList(), Set.class), is(false));
    }

    @Test
    public void shouldReturnTrueIfObjectIsSubclassOfSpecifiedClass(){
        assertThat(reflectionUtilities.objectIsOfAnyType(new TreeSet(), AbstractCollection.class), is(true));
    }

    @Test
    public void shouldReturnFalseIfObjectIsNotSubclassOfSpecifiedClass(){
        assertThat(reflectionUtilities.objectIsOfAnyType(new TreeSet(), Date.class), is(false));
    }

    @Test
    public void shouldReturnFalseIfObjectIsOfAnyTypeSpecified(){
        assertThat(reflectionUtilities.objectIsOfAnyType(new TreeSet(), Timer.class, Date.class), is(false));
    }

    @Test
    public void shouldReturnTrueIfObjectIsOfAnyTypeSpecified(){
        assertThat(reflectionUtilities.objectIsOfAnyType(new TreeSet(), AbstractSet.class, Date.class), is(true));
    }

    @Test
    public void shouldReturnTrueIfClassIsSubclassOfAnother(){
        assertThat(reflectionUtilities.classIsOfAssignableForm(RoboFragment.class, Fragment.class), is(true));
    }

    @Test
    public void shouldReturnTrueIfClassIsSameAsAnother(){
        assertThat(reflectionUtilities.classIsOfAssignableForm(RoboFragment.class, Fragment.class), is(true));
    }

    @Test
    public void shouldReturnFalseIfClassIsNotSubclassOfAnother(){
        assertThat(reflectionUtilities.classIsOfAssignableForm(Activity.class, Fragment.class), is(false));
    }

    @Test
    public void shouldCreateProxyThatNotifiesCallBack(){
        TestMethodInterceptor methodInterceptor = new TestMethodInterceptor();
        reflectionUtilities.createObjectProxy(Object.class, methodInterceptor).toString();
        assertThat(methodInterceptor.called, is(true));
    }

    private static class TestMethodInterceptor extends MethodInterceptor {
        boolean called = false;
        @Override
        public Object interceptMethod(Object obj, Method method, Object[] args, MethodProxy proxy) {
            called = true;
            return null;
        }
    };

}
