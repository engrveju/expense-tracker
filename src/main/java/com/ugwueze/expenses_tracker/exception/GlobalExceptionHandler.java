package com.ugwueze.expenses_tracker.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        log.debug("Resource not found: {}", ex.getMessage());
        ErrorResponse body = new ErrorResponse()
                .setTimestamp(OffsetDateTime.now())
                .setStatus(HttpStatus.NOT_FOUND.value())
                .setError(HttpStatus.NOT_FOUND.getReasonPhrase())
                .setMessage(ex.getMessage())
                .setPath(req.getRequestURI());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex, HttpServletRequest req) {
        log.debug("Duplicate resource: {}", ex.getMessage());
        ErrorResponse body = new ErrorResponse()
                .setTimestamp(OffsetDateTime.now())
                .setStatus(HttpStatus.CONFLICT.value())
                .setError(HttpStatus.CONFLICT.getReasonPhrase())
                .setMessage(ex.getMessage())
                .setPath(req.getRequestURI());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        ErrorResponse body = new ErrorResponse()
                .setTimestamp(OffsetDateTime.now())
                .setStatus(HttpStatus.CONFLICT.value())
                .setError(HttpStatus.CONFLICT.getReasonPhrase())
                .setMessage("Database constraint violation")
                .setPath(req.getRequestURI())
                .addError(ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }
        String msg = "Validation failed for request";
        log.debug("{} - {}", msg, errors);
        ErrorResponse body = new ErrorResponse()
                .setTimestamp(OffsetDateTime.now())
                .setStatus(HttpStatus.BAD_REQUEST.value())
                .setError(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .setMessage(msg)
                .setPath(req.getRequestURI())
                .setErrors(errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        List<String> errors = violations.stream()
                .map(v -> {
                    String p = v.getPropertyPath() == null ? "" : v.getPropertyPath().toString();
                    return p + ": " + v.getMessage();
                })
                .collect(Collectors.toList());
        String msg = "Validation failed for request parameters";
        log.debug("{} - {}", msg, errors);
        ErrorResponse body = new ErrorResponse()
                .setTimestamp(OffsetDateTime.now())
                .setStatus(HttpStatus.BAD_REQUEST.value())
                .setError(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .setMessage(msg)
                .setPath(req.getRequestURI())
                .setErrors(errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        log.debug("Malformed request body: {}", ex.getMessage());
        ErrorResponse body = new ErrorResponse()
                .setTimestamp(OffsetDateTime.now())
                .setStatus(HttpStatus.BAD_REQUEST.value())
                .setError(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .setMessage("Malformed request body: " + ex.getMostSpecificCause().getMessage())
                .setPath(req.getRequestURI());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("Access denied: {}", ex.getMessage());
        ErrorResponse body = new ErrorResponse()
                .setTimestamp(OffsetDateTime.now())
                .setStatus(HttpStatus.FORBIDDEN.value())
                .setError(HttpStatus.FORBIDDEN.getReasonPhrase())
                .setMessage("Access denied")
                .setPath(req.getRequestURI());
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception while processing request {} {}", req.getMethod(), req.getRequestURI(), ex);
        ErrorResponse body = new ErrorResponse()
                .setTimestamp(OffsetDateTime.now())
                .setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .setMessage("An unexpected error occurred")
                .setPath(req.getRequestURI())
                .addError(ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}