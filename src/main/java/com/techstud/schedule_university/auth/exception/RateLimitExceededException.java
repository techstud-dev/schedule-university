package com.techstud.schedule_university.auth.exception;

import lombok.experimental.StandardException;

@StandardException
public class RateLimitExceededException extends Exception {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
