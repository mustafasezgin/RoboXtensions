package com.robolx.injector.exception;


public class TestSetupException extends RuntimeException {
    public enum SetupError{
        COULDNT_SET_FIELD_IN_TEST("Problem when trying to set field in test case");

        private String msg; //NOPMD
        private SetupError(String msg) {
            this.msg = msg;
        }
    }
    public TestSetupException(SetupError error, Throwable throwable) {
        super(error.msg, throwable);
    }
}
