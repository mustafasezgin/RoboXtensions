package com.robolx.injector.exception;


public class TestSetupException extends RuntimeException {
    public enum SetupError{
        SUBJECT_MARKED_AS_MOCK("Test Subject is also marked as mock"),
        COULDNT_SET_FIELD_IN_TEST("Problem when trying to set field in test case"),
        NO_TEST_SUBJECT("Your test has not subject specified"),
        INVALID_VIEW_ID_IN_TEST_CASE("A view you want to inject into your test does not exist in the subject"),
        VIEW_FIELD_NOT_INITIALISED("View field in test subject was null when trying to set it into the test");

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
