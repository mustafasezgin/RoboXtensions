package com.robolx.injector;


import com.robolx.injector.exception.InvalidViewIdInTestCaseException;
import com.robolx.injector.exception.NoTestSubjectException;
import com.robolx.injector.exception.NoViewIdSpecifiedException;
import com.robolx.injector.exception.TestSubjectAlsoMarkedAsMockException;
import com.robolx.runner.RoboLXTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.robolx.injector.SampleActivityTestObjects.SampleTest;
import static com.robolx.injector.SampleActivityTestObjects.SampleTestWithInvalidViewId;
import static com.robolx.injector.SampleActivityTestObjects.SampleTestWithNoSubject;
import static com.robolx.injector.SampleActivityTestObjects.SampleTestWithNoViewId;
import static com.robolx.injector.SampleActivityTestObjects.SampleTestWithSubjectAlsoMarkedAsMock;
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

    @Test(expected = NoTestSubjectException.class)
    public void shouldThrowExceptionIfNoSubjectIsAnnotated(){
        testFieldInjector.setupTestCase(new SampleTestWithNoSubject());
    }

    @Test(expected = TestSubjectAlsoMarkedAsMockException.class)
    public void shouldThrowErrorIfSubjectIsAlsoMarkedAsMock(){
        testFieldInjector.setupTestCase(new SampleTestWithSubjectAlsoMarkedAsMock());
    }

    @Test
    public void shouldInjectSameViewMembersIntoTestAndActivityAfterOnCreate(){
        testFieldInjector.setupTestCase(sampleTest);
        sampleTest.callActivityOnCreate();
        assertThat(sampleTest.hasSameViewsInjectedIntoActivityAndTest(), is(true));
    }

    @Test(expected = InvalidViewIdInTestCaseException.class)
    public void shouldThrowExceptionIfViewIdInTestDoesNotExistInSubject(){
        testFieldInjector.setupTestCase(new SampleTestWithInvalidViewId());
    }

    @Test(expected = NoViewIdSpecifiedException.class)
    public void shouldThrowExceptionIfTestCaseHasInjectViewNoId(){
        testFieldInjector.setupTestCase(new SampleTestWithNoViewId());
    }

}
