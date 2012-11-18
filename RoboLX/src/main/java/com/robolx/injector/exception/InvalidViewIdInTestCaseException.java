package com.robolx.injector.exception;


public class InvalidViewIdInTestCaseException extends RuntimeException {

    public static final String MSG_FORMAT = "A view (id : %d) you want to inject into your test does not exist in the subject";

    public InvalidViewIdInTestCaseException(int viewId) {
        super(String.format(MSG_FORMAT, viewId));
    }
}
