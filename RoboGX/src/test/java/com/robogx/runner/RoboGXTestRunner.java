package com.robogx.runner;


import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.runners.model.InitializationError;

import java.io.File;

public class RoboGXTestRunner extends RobolectricTestRunner{
    public RoboGXTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass,new File("./src/test/resources"));
    }
}
