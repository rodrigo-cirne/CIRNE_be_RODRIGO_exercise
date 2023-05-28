package com.ecore.roles.exception;

import static java.lang.String.format;

public class InvalidArgumentException extends RuntimeException {

    public <T> InvalidArgumentException(Class<T> resource) {
        super(format("Invalid '%s' object", resource.getSimpleName()));
    }

    // new constructor to provide detailed error message
    public <T> InvalidArgumentException(Class<T> resource, String detailedMessage) {
        super(format("Invalid '%s' object. %s", resource.getSimpleName(), detailedMessage));
    }
}
