package com.plantdisease.backend.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/** Turns exceptions into clear JSON errors (RFC 9457 "problem details"). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidImageException.class)
    public ProblemDetail invalidImage(InvalidImageException e) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid image", e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail tooLarge(MaxUploadSizeExceededException e) {
        return problem(HttpStatusCode.valueOf(413), "Image too large", "The image must be 5 MB or less.");
    }

    @ExceptionHandler(MlServiceUnavailableException.class)
    public ProblemDetail mlUnavailable(MlServiceUnavailableException e) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Analysis service unavailable", e.getMessage());
    }

    private static ProblemDetail problem(HttpStatusCode status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        return pd;
    }
}
