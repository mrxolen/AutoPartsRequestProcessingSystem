package com.autoparts.application;

import com.autoparts.domain.RequestStatus;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class RejectedState implements RequestState {

    @Override
    public RequestStatus getStatus() {
        return RequestStatus.REJECTED;
    }

    @Override
    public Set<RequestStatus> getAllowedNextStatuses() {
        return Set.of();
    }
}
