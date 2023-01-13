package com.syberry.bakery.exception;


import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler extends Exception {

    private Map<String, List<String>> getErrorsMap(List<String> errors) {
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("errors", errors);
        return errorResponse;
    }

    private Map<String, List<Map<String, Object>>> getErrorsMap(Map<String, Object> errors) {
        Map<String, List<Map<String, Object>>> errorResponse = new HashMap<>();
        errorResponse.put("errors", List.of(errors));
        return errorResponse;
    }

    @ExceptionHandler({DeleteException.class,
            UpdateException.class,
            EntityNotFoundException.class,
            CreateException.class,
            InvalidArgumentTypeException.class})
    public final ResponseEntity<Map<String, List<String>>> customExceptionHandler(Exception ex) {
        List<String> errors = Collections.singletonList(ex.getMessage());
        return new ResponseEntity<>(getErrorsMap(errors), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailException.class)
    public final ResponseEntity<Map<String, List<String>>> mailExceptionHandler(EmailException ex) {
        List<String> errors = Collections.singletonList(ex.getMessage());
        return new ResponseEntity<>(getErrorsMap(errors), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public final ResponseEntity<Map<String, List<String>>> responseStatusExceptionsHandler(ResponseStatusException ex) {
        List<String> errors = Collections.singletonList(ex.getReason());
        return new ResponseEntity<>(getErrorsMap(errors), new HttpHeaders(), ex.getStatus());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public final ResponseEntity<Map<String, List<String>>> dataIntegrityViolationExceptionHandler(DataIntegrityViolationException ex) {
        List<String> errors = new ArrayList<>();
        errors.add(Objects.requireNonNull(ex.getRootCause()).getMessage());
        return new ResponseEntity<>(getErrorsMap(errors), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleParam(ConstraintViolationException e) {

        List<String> errors = new ArrayList<>();
        e.getConstraintViolations().forEach(cV -> {
            errors.add(cV.getPropertyPath().toString());
        });
        return new ResponseEntity<>(Map.of("message", errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<Map<String, Object>>>> validationErrorsHandler(MethodArgumentNotValidException ex) {
        Map<String, String> error = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            error.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        Map<String, Object> response = new HashMap<>();
        response.put("ValidationErrors", error);
        return new ResponseEntity<>(getErrorsMap(response), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IndexOutOfBoundsException.class)
    public final ResponseEntity<Map<String, List<String>>> indexOutOfBoundsExceptionsHandler(IndexOutOfBoundsException ex) {
        List<String> errors = Collections.singletonList(ex.getMessage());
        return new ResponseEntity<>(getErrorsMap(errors), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(value = TokenRefreshException.class)
    public ResponseEntity<Map<String, List<String>>> tokenRefreshExceptionHandler(TokenRefreshException ex) {
        List<String> errors = Collections.singletonList(ex.getMessage());
        return new ResponseEntity<>(getErrorsMap(errors), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(AccessException.class)
    public ResponseEntity<String> accessExceptionHandler(AccessException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

}

