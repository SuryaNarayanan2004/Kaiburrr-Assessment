package com.kaiburr.taskmanager.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> details = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fe) {
                        return fe.getField() + ": " + fe.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.toList());
        ApiError apiError = new ApiError(status.value(), HttpStatus.valueOf(status.value()).getReasonPhrase(), "Validation failed", request.getDescription(false), details);
        return new ResponseEntity<>(apiError, status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex, WebRequest request) {
        log.warn("Bad request: {}", ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiError apiError = new ApiError(status.value(), status.getReasonPhrase(), ex.getMessage(), request.getDescription(false), null);
        return new ResponseEntity<>(apiError, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiError apiError = new ApiError(status.value(), status.getReasonPhrase(), ex.getMessage(), request.getDescription(false), null);
        return new ResponseEntity<>(apiError, status);
    }
}


