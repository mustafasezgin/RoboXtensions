package com.robolx.runner;


import com.xtremelabs.robolectric.RobolectricConfig;
import com.xtremelabs.robolectric.bytecode.ClassHandler;
import com.xtremelabs.robolectric.bytecode.RobolectricClassLoader;
import org.junit.runners.model.InitializationError;
import roboguice.test.RobolectricRoboTestRunner;

import java.io.File;
import java.lang.reflect.Field;

public class RoboLXRunner extends RobolectricRoboTestRunner {
    public RoboLXRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    public RoboLXRunner(Class<?> testClass, File androidManifestPath, File resourceDirectory) throws InitializationError {
        super(testClass, androidManifestPath, resourceDirectory);
    }

    public RoboLXRunner(Class<?> testClass, File androidProjectRoot) throws InitializationError {
        super(testClass, androidProjectRoot);
    }

    public RoboLXRunner(Class<?> testClass, ClassHandler classHandler, RobolectricClassLoader classLoader, RobolectricConfig robolectricConfig) throws InitializationError {
        super(testClass, classHandler, classLoader, robolectricConfig);
    }

    public RoboLXRunner(Class<?> testClass, ClassHandler classHandler, RobolectricConfig robolectricConfig) throws InitializationError {
        super(testClass, classHandler, robolectricConfig);
    }

    public RoboLXRunner(Class<?> testClass, RobolectricConfig robolectricConfig) throws InitializationError {
        super(testClass, robolectricConfig);
    }

    @Override
    public void prepareTest(Object test) {
        for(Field field : test.getClass().getDeclaredFields()){

        }
    }
}
