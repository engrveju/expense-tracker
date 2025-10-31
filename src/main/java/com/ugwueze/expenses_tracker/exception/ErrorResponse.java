package com.ugwueze.expenses_tracker.exception;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class ErrorResponse {

    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> errors = new ArrayList<>();

    public ErrorResponse() {
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public ErrorResponse setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public ErrorResponse setStatus(int status) {
        this.status = status;
        return this;
    }

    public String getError() {
        return error;
    }

    public ErrorResponse setError(String error) {
        this.error = error;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ErrorResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getPath() {
        return path;
    }

    public ErrorResponse setPath(String path) {
        this.path = path;
        return this;
    }

    public List<String> getErrors() {
        return errors;
    }

    public ErrorResponse setErrors(List<String> errors) {
        this.errors = errors == null ? new ArrayList<>() : errors;
        return this;
    }

    public ErrorResponse addError(String e) {
        if (e != null) {
            this.errors.add(e);
        }
        return this;
    }
}