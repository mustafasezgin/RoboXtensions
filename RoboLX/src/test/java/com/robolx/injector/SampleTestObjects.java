package com.robolx.injector;


import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.google.inject.Inject;
import com.robolx.R;
import com.robolx.annotations.Subject;
import org.mockito.Mock;
import org.mockito.internal.util.MockUtil;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

import java.util.Date;

public class SampleTestObjects {

    public static class SampleTestWithNoSubject {
        private Object object;
    }

    public static class SampleTestWithSameSubjectAsMock {
        @Mock
        private Object object;
    }

    public static class SampleTestWithInvalidViewId {
        @Subject
        private SampleActivity activity;

        @InjectView(R.id.button)
        private Button button;
    }

    public static  class SampleTest{
        private MockUtil mockUtils = new MockUtil();
        @Subject
        private SampleActivity activity;
        @Mock
        private Object mock;
        @Mock
        private Date date;
        @InjectView(R.id.textview)
        private TextView textView;

        public boolean hasActivityInjected() {
            return activity != null;
        }

        public boolean hasSameMocksInjectedIntoTestAndActivity() {
            return hasSameNonNullObjectMock() && hasSameNonNullDateMock();
        }

        private boolean hasSameNonNullObjectMock() {
            return mock != null && activity.mock != null && mock == activity.mock;
        }

        private boolean hasSameNonNullDateMock() {
            return date != null && activity.date != null && date == activity.date;
        }

        public boolean hasSameViewsInjectedIntoActivityAndTest() {
            return textView != null && activity.textView != null && textView == activity.textView;
        }

        public boolean hasMocksInjected() {
            return mockUtils.isMock(mock) && mockUtils.isMock(date);
        }

        public boolean hasMocksInjectedIntoActivity(){
            return mockUtils.isMock(activity.mock) && mockUtils.isMock(activity.date);
        }

        public void callActivityOnCreate() {
            activity.onCreate(null);
        }
    }

    public static class SampleActivity extends RoboActivity{
        @Inject
        private Object mock;
        @Inject
        private Date date;
        @InjectView(R.id.textview)
        private TextView textView;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.example);
        }
    }


}
