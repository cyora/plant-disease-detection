package com.plantdisease.backend.common;

/** The FastAPI service cannot be reached or failed. Returned as HTTP 503. */
public class MlServiceUnavailableException extends RuntimeException {
    public MlServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
