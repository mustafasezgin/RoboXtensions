package com.robogx.runner;


import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.runners.model.InitializationError;

import java.io.File;

public class RoboUtilsTestRunner extends RobolectricTestRunner{
    public RoboUtilsTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass,new File("./src/test/resources"));
    }
}
