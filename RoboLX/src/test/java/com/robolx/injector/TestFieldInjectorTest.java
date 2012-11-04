package com.robolx.injector;


import com.robolx.injector.exception.TestSetupException;
import com.robolx.runner.RoboLXTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.robolx.injector.SampleTestObjects.SampleTest;
import static com.robolx.injector.SampleTestObjects.SampleTestWithInvalidViewId;
import static com.robolx.injector.SampleTestObjects.SampleTestWithNoSubject;
import static com.robolx.injector.SampleTestObjects.SampleTestWithSameSubjectAsMock;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RoboLXTestRunner.class)
public class TestFieldInjectorTest {

    private TestFieldInjector testFieldInjector;
    private SampleTest sampleTest;


    @Before
    public void setUp() {
        initMocks(this);
        sampleTest = new SampleTest();
        testFieldInjector = new TestFieldInjector();

    }

    @Test
    public void shouldInjectActivityIntoTestCaseMarkedAsSubject(){
        testFieldInjector.setupTestCase(sampleTest);
        assertThat(sampleTest.hasActivityInjected(), is(true));
    }

    @Test
    public void shouldInjectMocksIntoActivityIrrespectiveOfType(){
        testFieldInjector.setupTestCase(sampleTest);
        assertThat(sampleTest.hasMocksInjectedIntoActivity(), is(true));
    }

    @Test
    public void shouldInjectMocksIntoTestIrrespectiveOfType(){
        testFieldInjector.setupTestCase(sampleTest);
        assertThat(sampleTest.hasMocksInjected(), is(true));
    }

    @Test
    public void shouldInjectSameInstanceOfMockIntoActivityAndTest(){
        testFieldInjector.setupTestCase(sampleTest);
        assertThat(sampleTest.hasSameMocksInjectedIntoTestAndActivity(), is(true));
    }

    @Test(expected = TestSetupException.class)
    public void shouldThrowExceptionIfNoSubjectIsAnnotated(){
        testFieldInjector.setupTestCase(new SampleTestWithNoSubject());
    }

    @Test(expected = TestSetupException.class)
    public void shouldThrowErrorIfSubjectIsAlsoMarkedAsMock(){
        testFieldInjector.setupTestCase(new SampleTestWithSameSubjectAsMock());
    }

    @Test
    public void shouldInjectSameViewMembersIntoTestAndActivityAfterOnCreate(){
        testFieldInjector.setupTestCase(sampleTest);
        sampleTest.callActivityOnCreate();
        assertThat(sampleTest.hasSameViewsInjectedIntoActivityAndTest(), is(true));
    }

    @Test(expected = TestSetupException.class)
    public void shouldThrowExceptionIfViewIdInTestDoesNotExistInSubject(){
        testFieldInjector.setupTestCase(new SampleTestWithInvalidViewId());
    }

}
