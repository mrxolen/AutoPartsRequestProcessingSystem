package com.autoparts.application;

import com.autoparts.domain.RequestCase;
import com.autoparts.domain.RequestStatus;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class StatusTransitionService {

    private final Map<RequestStatus, RequestState> states = new EnumMap<>(RequestStatus.class);

    public StatusTransitionService(List<RequestState> requestStates) {
        for (RequestState requestState : requestStates) {
            states.put(requestState.getStatus(), requestState);
        }
    }

    public RequestCase changeStatus(RequestCase requestCase, RequestStatus nextStatus) {
        if (requestCase == null) {
            throw new IllegalArgumentException("Request case is required.");
        }

        if (nextStatus == null) {
            throw new IllegalArgumentException("Next status is required.");
        }

        RequestState currentState = states.get(requestCase.getStatus());
        if (currentState == null) {
            throw new IllegalArgumentException("Unknown request status: " + requestCase.getStatus());
        }

        currentState.moveTo(requestCase, nextStatus);
        return requestCase;
    }

    public boolean canMoveTo(RequestStatus currentStatus, RequestStatus nextStatus) {
        RequestState currentState = states.get(currentStatus);
        return currentState != null && currentState.canMoveTo(nextStatus);
    }
}
