package com.autoparts.application.exception;

public class RequestCaseNotFoundException extends RuntimeException {

    public RequestCaseNotFoundException(Long requestCaseId) {
        super("Request case not found with id: " + requestCaseId);
    }
}
