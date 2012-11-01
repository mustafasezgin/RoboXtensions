package com.robolx.injector;


import com.google.inject.Inject;
import com.robolx.annotations.Subject;
import org.mockito.Mock;
import roboguice.activity.RoboActivity;

public class SampleTestObjects {

    public static class SampleTestWithNoSubject {
        private Object object;
    }

    public static class SampleTestWithSameSubjectAsMock {
        @Mock
        private Object object;
    }

    public static  class SampleTest{

        @Subject
        private SampleActivity activity;
        @Mock
        private Object mock;


        public boolean hasActivityInjected() {
            return activity != null;
        }

        public boolean hasSameMockInjectedIntoTestAndActivity() {
            return mock != null && activity.mock != null && mock == activity.mock;
        }
    }

    private static class SampleActivity extends RoboActivity{
        @Inject
        private Object mock;
    }


}
