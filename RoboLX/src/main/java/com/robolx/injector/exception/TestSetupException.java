package com.robolx.injector.exception;


public class TestSetupException extends RuntimeException {
    public enum SetupError{
        SUBJECT_MARKED_AS_MOCK("Test Subject is also marked as mock"),
        COULDNT_SET_FIELD_IN_TEST("Problem when trying to set field in test case"),
        NO_TEST_SUBJECT("Your test has not subject specified");

        private String msg; //NOPMD
        private SetupError(String msg) {
            this.msg = msg;
        }
    }

    public TestSetupException(SetupError error) {
        super(error.msg);
    }

    public TestSetupException(SetupError error, Throwable throwable) {
        super(error.msg, throwable);
    }
}
