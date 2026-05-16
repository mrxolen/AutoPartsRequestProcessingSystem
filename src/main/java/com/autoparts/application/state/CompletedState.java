package com.autoparts.application.state;

import com.autoparts.domain.RequestStatus;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class CompletedState implements RequestState {

    @Override
    public RequestStatus getStatus() {
        return RequestStatus.COMPLETED;
    }

    @Override
    public Set<RequestStatus> getAllowedNextStatuses() {
        return Set.of();
    }
}
