package com.robolx.injector.exception;

public class NoTestSubjectException extends RuntimeException {
    public NoTestSubjectException() {
        super("Your test has no subject specified");
    }
}
