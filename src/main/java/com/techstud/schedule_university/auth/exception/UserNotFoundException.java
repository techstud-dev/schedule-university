package com.techstud.schedule_university.auth.exception;

import lombok.experimental.StandardException;

@StandardException
public class UserNotFoundException extends Exception {

    private static final String STANDARD_MESSAGE = "User with these credentials not found";

    public UserNotFoundException() {
        super(STANDARD_MESSAGE);
    }
}
