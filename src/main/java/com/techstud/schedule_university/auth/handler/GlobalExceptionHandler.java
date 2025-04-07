package com.techstud.schedule_university.auth.handler;

import com.techstud.schedule_university.auth.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${spring.application.name}")
    private String systemName;

    @Value("${spring.application.systemName}")
    private String applicationName;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return getMapResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex) {

        List<String> errors = ex.getAllErrors().stream()
                .map(error -> {
                    if (error instanceof ParameterValidationResult pvr) {
                        return pvr.getResolvableErrors().stream()
                                .map(oe -> String.format("%s: %s",
                                        pvr.getMethodParameter().getParameterName(),
                                        oe.getDefaultMessage()))
                                .collect(Collectors.joining(", "));
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.toList());

        return getMapResponseEntity(errors);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        return getMapResponseEntity(errors);
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException e) {
        log.error("Bad credentials exception", e);
        return getMapResponseEntity(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(InvalidCodeException.class)
    public ResponseEntity<Map<String, String>> handleException(InvalidCodeException e) {
        log.error("Invalid code exception", e);
        return getMapResponseEntity(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException e) {
        log.error("User already exists exception", e);
        return getMapResponseEntity(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(UserExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String, String>> handleUserExistsException(UserExistsException e) {
        log.error("User already exists exception", e);
        return getMapResponseEntity(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleRateLimitExceededException(RateLimitExceededException e) {
        log.error("Rate limit exceeded exception", e);
        return getMapResponseEntity(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(InvalidJwtTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Map<String, String>> handleInvalidJwtTokenException(InvalidJwtTokenException e) {
        log.error("Jwt token is invalid", e);
        return getMapResponseEntity(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    // SUPP METHODS

    private ResponseEntity<Map<String, Object>> getMapResponseEntity(List<String> errors) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("systemName", systemName);
        response.put("applicationName", applicationName);
        response.put("timestamp", LocalDateTime.now());
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Map<String, String>> getMapResponseEntity
            (HttpStatus status, String message) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("systemName", systemName);
        response.put("applicationName", applicationName);
        response.put("error", message);
        return new ResponseEntity<>(response, status);
    }
}
