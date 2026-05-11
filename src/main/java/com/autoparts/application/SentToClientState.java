package com.autoparts.application;

import com.autoparts.domain.RequestStatus;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SentToClientState implements RequestState {

    @Override
    public RequestStatus getStatus() {
        return RequestStatus.SENT_TO_CLIENT;
    }

    @Override
    public Set<RequestStatus> getAllowedNextStatuses() {
        return Set.of(RequestStatus.ACCEPTED, RequestStatus.REJECTED);
    }
}
