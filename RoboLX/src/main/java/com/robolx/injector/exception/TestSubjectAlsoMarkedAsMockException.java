package com.robolx.injector.exception;


public class TestSubjectAlsoMarkedAsMockException extends RuntimeException{
    public TestSubjectAlsoMarkedAsMockException() {
        super("Test Subject is also marked as mock");
    }
}
