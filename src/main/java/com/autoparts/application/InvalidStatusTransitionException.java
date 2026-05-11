package com.autoparts.application;

import com.autoparts.domain.RequestStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(RequestStatus currentStatus, RequestStatus nextStatus) {
        super("Invalid request status transition: " + currentStatus + " -> " + nextStatus);
    }
}
