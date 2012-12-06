package com.robolx.injector;


import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.robolx.injector.exception.TestSetupException;
import com.robolx.injector.exception.ViewFieldNotInitialisedException;
import com.robolx.utilities.ReflectionUtilities;
import com.robolx.utilities.ReflectionUtilities.MethodInterceptor;
import com.robolx.utilities.RobolectricUtilities;
import org.apache.commons.lang3.ObjectUtils;
import org.mockito.cglib.proxy.MethodProxy;
import roboguice.activity.RoboActivity;
import roboguice.activity.RoboFragmentActivity;
import roboguice.activity.event.OnContentChangedEvent;
import roboguice.event.EventListener;
import roboguice.event.EventManager;
import roboguice.fragment.RoboFragment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.robolx.injector.exception.TestSetupException.SetupError;
import static org.mockito.Mockito.mock;

public class TestFieldInjector extends MethodInterceptor {
    //Cannot make this static as it will hold state across tests
    private Context currentContextForTest;

    protected static final String TAG = "TAG";

    private TestCase testCase;
    private final RobolectricUtilities robolectricUtilities;
    private final ReflectionUtilities reflectionUtilities;
    private Object testSubjectInstance;

    protected TestFieldInjector(TestCase testCase, RobolectricUtilities robolectricUtilities, ReflectionUtilities reflectionUtilities){
        this.testCase = testCase;
        this.robolectricUtilities = robolectricUtilities;
        this.reflectionUtilities = reflectionUtilities;
        this.currentContextForTest =  new RoboFragmentActivity();
    }

    public TestFieldInjector(){
        this.robolectricUtilities = new RobolectricUtilities();
        this.reflectionUtilities = new ReflectionUtilities();
        this.currentContextForTest =  new RoboFragmentActivity();
    }

    public void setupTestCase(){
        /**
         * Needs to be done first before the injector is retrieved
         */
        setupBindingsForMocks();

        createTestSubjectAndSetIntoTestCase();

        currentContextForTest = reflectionUtilities.objectIsOfAnyType(testSubjectInstance, Context.class) ?
                (Context)testSubjectInstance : currentContextForTest;

        robolectricUtilities.enterContext(currentContextForTest);
        Injector currentContextInjector = robolectricUtilities.getInjector(currentContextForTest);

        setMocksOnTestCase(currentContextInjector);

        setupViewsInTestSubjectIfRequired(currentContextInjector);
    }

    public void setupTestCase(Object testCase) {
        this.testCase = new TestCase(testCase);
        setupTestCase();
    }



    private void setupViewsInTestSubjectIfRequired(Injector injector) {
        if(testSubjectIsActivity()){
            injector.getInstance(EventManager.class).registerObserver(OnContentChangedEvent.class, new ContentChangedListener());
        } else if(testSubjectIsSupportLibraryFragment()){
            /**
             * This restricts usage to android support library fragments :/
             */
            FragmentManager fragmentManager = ((RoboFragmentActivity)currentContextForTest).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add((RoboFragment)testSubjectInstance, TAG).commit();
        }
    }

    private boolean testSubjectIsActivity() {
        return reflectionUtilities.objectIsOfAnyType(testSubjectInstance, RoboActivity.class);
    }

    private void createTestSubjectAndSetIntoTestCase() {
        robolectricUtilities.enterContext(currentContextForTest);
        Injector activityInjector = robolectricUtilities.getInjector(currentContextForTest);

        if(testSubjectIsSupportLibraryFragment()){
            //We do this so we can hook into the onViewCreated lifecycle method to inject views
            testSubjectInstance = reflectionUtilities.createObjectProxy(testCase.getTestSubjectClassType(), this);
        } else {
            testSubjectInstance = activityInjector.getInstance(testCase.getTestSubjectClassType());
        }

        try {
            reflectionUtilities.forceSetValueOnField(testCase.getTestSubjectField(),
                                                     testCase.getTestCaseInstance(),
                                                     testSubjectInstance);
        } catch (IllegalAccessException e) {
            throw new TestSetupException(TestSetupException.SetupError.COULDNT_SET_FIELD_IN_TEST, e);
        }

        robolectricUtilities.exitContext(currentContextForTest);
    }

    private boolean testSubjectIsSupportLibraryFragment() {
        return reflectionUtilities.classIsOfAssignableForm(testCase.getTestSubjectClassType(),RoboFragment.class);
    }

    private void setMocksOnTestCase(Injector currentContextInjector) {

        for (Field mockField : testCase.getFieldsMarkedWithMock()) {
            Object mockInstance = currentContextInjector.getInstance(mockField.getType());
            try {
                reflectionUtilities.forceSetValueOnField(mockField, testCase.getTestCaseInstance(), mockInstance);
            } catch (IllegalAccessException e) {
                throw new TestSetupException(SetupError.COULDNT_SET_FIELD_IN_TEST, e);
            }
        }
    }

    private void setupBindingsForMocks() {
        robolectricUtilities.setGuiceModules(new BindingsForMocksModule());
    }

    @Override
    public Object interceptMethod(Object obj, Method method, Object[] args, MethodProxy proxy) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected class BindingsForMocksModule extends AbstractModule {
        @Override
        @SuppressWarnings("unchecked")
        protected void configure() {
            for (Field field : testCase.getFieldsMarkedWithMock()) {
                bind((Class) field.getType()).toInstance(mock(field.getType()));
            }
        }
    }

    protected class ContentChangedListener implements EventListener {

        @Override
        public void onEvent(Object event) {

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

    public Context getCurrentContextForTest(){
        return currentContextForTest;
    }
}
