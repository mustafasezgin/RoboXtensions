package com.robolx.injector;


import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.robolx.injector.TestFieldInjector.ContentChangedListener;
import com.robolx.runner.RoboLXTestRunner;
import com.robolx.utilities.ReflectionUtilities;
import com.robolx.utilities.RobolectricUtilities;
import com.xtremelabs.robolectric.tester.android.util.TestFragmentManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import roboguice.activity.RoboActivity;
import roboguice.activity.event.OnContentChangedEvent;
import roboguice.event.EventManager;
import roboguice.fragment.RoboFragment;

import java.lang.reflect.Field;
import java.util.Date;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RoboLXTestRunner.class)
public class TestFieldInjectorTest {
    private TestFieldInjector testFieldInjector;
    @Mock
    private TestCase testCase;
    @Mock
    private RobolectricUtilities roboUtils;
    @Mock
    private Injector injector;
    @Mock
    private EventManager eventManager;
    @Mock
    private ReflectionUtilities reflectionUtils;
    @Mock
    private Object testCaseInstance;
    @Mock
    private RoboActivity activity;

    private RoboFragment fragment = new RoboFragment(){};


    @Before
    public void setUp() {
        initMocks(this);
        testFieldInjector = new TestFieldInjector(testCase,roboUtils, reflectionUtils);

        when(roboUtils.getInjector(any(Context.class))).thenReturn(injector);
        when(injector.getInstance(EventManager.class)).thenReturn(eventManager);

    }

    @Test
    public void shouldSetupGuiceBindingsBeforeEnteringContextAndGettingInjector(){
        testFieldInjector.setupTestCase();
        InOrder inOrder = inOrder(roboUtils);
        inOrder.verify(roboUtils).setGuiceModules(isA(TestFieldInjector.BindingsForMocksModule.class));
        inOrder.verify(roboUtils).enterContext(any(Context.class));
        inOrder.verify(roboUtils).getInjector(any(Context.class));
    }

    @Test
    public void shouldCreateFragmentProxyWhenTestSubjectIsFragment(){
        when(testCase.getTestSubjectClassType()).thenReturn(RoboFragment.class);
        when(reflectionUtils.classIsOfAssignableForm(RoboFragment.class, RoboFragment.class)).thenReturn(true);
        when(reflectionUtils.createObjectProxy(RoboFragment.class, testFieldInjector)).thenReturn(fragment);
        testFieldInjector.setupTestCase();
        verify(reflectionUtils).createObjectProxy(RoboFragment.class, testFieldInjector);

    }

    @Test
    public void shouldInjectFragmentIntoTestCaseMarkedAsSubject() throws NoSuchFieldException, IllegalAccessException {
        when(testCase.getTestSubjectClassType()).thenReturn(RoboFragment.class);
        when(testCase.getTestSubjectField()).thenReturn(getSampleField());
        when(testCase.getTestCaseInstance()).thenReturn(testCaseInstance);
        when(reflectionUtils.classIsOfAssignableForm(RoboFragment.class, RoboFragment.class)).thenReturn(true);
        when(reflectionUtils.createObjectProxy(RoboFragment.class, testFieldInjector)).thenReturn(fragment);
        testFieldInjector.setupTestCase();
        verify(reflectionUtils).forceSetValueOnField(getSampleField(), testCaseInstance, fragment);
    }

    @Test
    public void shouldInjectActivityIntoTestCaseMarkedAsSubject() throws NoSuchFieldException, IllegalAccessException {
        when(testCase.getTestSubjectClassType()).thenReturn(Activity.class);
        when(testCase.getTestSubjectField()).thenReturn(getSampleField());
        when(testCase.getTestCaseInstance()).thenReturn(testCaseInstance);
        when(injector.getInstance(Activity.class)).thenReturn(activity);
        testFieldInjector.setupTestCase();
        verify(reflectionUtils).forceSetValueOnField(getSampleField(), testCaseInstance, activity);
    }

    @Test
    public void shouldInjectAnyTypeIntoTestCaseMarkedAsSubject() throws NoSuchFieldException, IllegalAccessException {
        when(testCase.getTestSubjectClassType()).thenReturn(TestFieldInjector.class);
        when(testCase.getTestSubjectField()).thenReturn(getSampleField());
        when(testCase.getTestCaseInstance()).thenReturn(testCaseInstance);
        when(injector.getInstance(TestFieldInjector.class)).thenReturn(testFieldInjector);
        testFieldInjector.setupTestCase();
        verify(reflectionUtils).forceSetValueOnField(getSampleField(), testCaseInstance, testFieldInjector);
    }

    @Test
    public void shouldEnterIntoDummyActivityContextBeforeInjectingTestSubject(){
        when(testCase.getTestSubjectClassType()).thenReturn(Object.class);
        testFieldInjector.setupTestCase();
        InOrder inOrder = inOrder(roboUtils, injector);
        inOrder.verify(roboUtils).enterContext(any(FragmentActivity.class));
        inOrder.verify(roboUtils).getInjector(any(FragmentActivity.class));
        inOrder.verify(injector).getInstance(Object.class);

    }

    @Test
    public void shouldExitDummyActivityContextAfterInjectingTestSubject(){
        testFieldInjector.setupTestCase();
        InOrder inOrder = inOrder(roboUtils);
        inOrder.verify(roboUtils).enterContext(any(FragmentActivity.class));
        inOrder.verify(roboUtils).exitContext(any(FragmentActivity.class));

    }

    @Test
    public void shouldEnterIntoNewContextOnlyAfterExitingOutOfDummyActivityContext(){
        testFieldInjector.setupTestCase();
        InOrder inOrder = inOrder(roboUtils);
        inOrder.verify(roboUtils).enterContext(any(FragmentActivity.class));
        inOrder.verify(roboUtils).exitContext(any(FragmentActivity.class));
        inOrder.verify(roboUtils).enterContext(any(FragmentActivity.class));
    }

    @Test
    public void shouldEnterIntoDummyContextIfTestSubjectIsNotAContext(){
        when(reflectionUtils.objectIsOfAnyType(any(), eq(Context.class))).thenReturn(false);
        testFieldInjector.setupTestCase();
        verify(roboUtils, times(2)).enterContext(any(FragmentActivity.class));
    }

    @Test
    public void shouldEnterIntoTestSubjectContextIfTestSubjectIsAContext(){
        when(testCase.getTestSubjectClassType()).thenReturn(Activity.class);
        when(injector.getInstance(Activity.class)).thenReturn(activity);
        when(reflectionUtils.objectIsOfAnyType(activity, Context.class)).thenReturn(true);
        when(roboUtils.getInjector(activity)).thenReturn(injector);

        testFieldInjector.setupTestCase();
        verify(roboUtils).enterContext(activity);
    }

    @Test
    public void shouldGetTheInjectorForTheCurrentContextOnlyAfterEnteringCurrentContext(){
        when(testCase.getTestSubjectClassType()).thenReturn(Activity.class);
        when(injector.getInstance(Activity.class)).thenReturn(activity);
        when(reflectionUtils.objectIsOfAnyType(activity, Context.class)).thenReturn(true);
        when(roboUtils.getInjector(activity)).thenReturn(injector);

        testFieldInjector.setupTestCase();
        InOrder inOrder = inOrder(roboUtils);
        inOrder.verify(roboUtils).enterContext(activity);
        inOrder.verify(roboUtils).getInjector(activity);
    }

    @Test
    public void shouldSetMocksIntoTestCase() throws NoSuchFieldException, IllegalAccessException {
        Object mockOne = mock(Object.class);
        Object mockTwo = mock(Object.class);
        when(testCase.getTestCaseInstance()).thenReturn(testCaseInstance);
        when(testCase.getFieldsMarkedWithMock()).thenReturn(Lists.newArrayList(getSampleField(), getSecondSampleField()));
        when(injector.getInstance(getSampleField().getType())).thenReturn(mockOne);
        when(injector.getInstance(getSecondSampleField().getType())).thenReturn(mockTwo);
        testFieldInjector.setupTestCase();
        verify(reflectionUtils).forceSetValueOnField(getSampleField(), testCaseInstance, mockOne);
        verify(reflectionUtils).forceSetValueOnField(getSecondSampleField(), testCaseInstance, mockTwo);
    }

    @Test
    public void shouldNotListenToContentChangesIfTestSubjectIsNotAnActivity(){
        Date testSubject = new Date();
        when(testCase.getTestSubjectClassType()).thenReturn(Date.class);
        when(injector.getInstance(Date.class)).thenReturn(testSubject);
        when(reflectionUtils.objectIsOfAnyType(testSubject, Activity.class)).thenReturn(false);
        testFieldInjector.setupTestCase();
        verify(injector, never()).getInstance(EventManager.class);
    }

    @Test
    public void shouldListenToContentChangesIfTestSubjectIsAnActivity(){
        when(testCase.getTestSubjectClassType()).thenReturn(RoboActivity.class);
        when(injector.getInstance(RoboActivity.class)).thenReturn(activity);
        when(reflectionUtils.objectIsOfAnyType(activity, RoboActivity.class)).thenReturn(true);
        testFieldInjector.setupTestCase();
        verify(injector).getInstance(EventManager.class);
        verify(eventManager).registerObserver(eq(OnContentChangedEvent.class), isA(ContentChangedListener.class));
    }

    @Test
    public void shouldUseFragmentTransactionToAddFragmentToDummyActivityIfSubjectIsAnFragment(){
        when(testCase.getTestSubjectClassType()).thenReturn(RoboFragment.class);
        when(reflectionUtils.classIsOfAssignableForm(RoboFragment.class,RoboFragment.class)).thenReturn(true);
        when(reflectionUtils.createObjectProxy(RoboFragment.class, testFieldInjector)).thenReturn(fragment);
        when(reflectionUtils.objectIsOfAnyType(fragment, RoboFragment.class)).thenReturn(true);
        testFieldInjector.setupTestCase();
        TestFragmentManager fragmentManager = (TestFragmentManager)shadowOf((FragmentActivity)testFieldInjector.getCurrentContextForTest()).getSupportFragmentManager();
        assertThat(fragmentManager.getCommittedTransactions().size(), is(1));
        assertThat((RoboFragment)fragmentManager.getCommittedTransactions().get(0).getFragment(), is(fragment));
    }

    private Field getSampleField() throws NoSuchFieldException {
        return getClass().getDeclaredField("testFieldInjector");
    }

    private Field getSecondSampleField() throws NoSuchFieldException {
        return getClass().getDeclaredField("testCase");
    }

}
