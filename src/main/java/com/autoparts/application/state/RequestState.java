package com.autoparts.application.state;

import com.autoparts.domain.RequestCase;
import com.autoparts.domain.RequestStatus;
import java.util.Set;

public interface RequestState {

    RequestStatus getStatus();

    Set<RequestStatus> getAllowedNextStatuses();

    default boolean canMoveTo(RequestStatus nextStatus) {
        return getAllowedNextStatuses().contains(nextStatus);
    }

    default void moveTo(RequestCase requestCase, RequestStatus nextStatus) {
        if (!canMoveTo(nextStatus)) {
            throw new InvalidStatusTransitionException(getStatus(), nextStatus);
        }

        requestCase.setStatus(nextStatus);
    }
}
