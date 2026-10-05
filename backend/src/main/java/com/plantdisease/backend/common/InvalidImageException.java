package com.plantdisease.backend.common;

/** The uploaded file is missing, empty or not a readable image. Returned as HTTP 400. */
public class InvalidImageException extends RuntimeException {
    public InvalidImageException(String message) {
        super(message);
    }
}
