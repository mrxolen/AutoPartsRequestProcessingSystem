package com.autoparts.application.state;

import com.autoparts.domain.RequestStatus;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class NewState implements RequestState {

    @Override
    public RequestStatus getStatus() {
        return RequestStatus.NEW;
    }

    @Override
    public Set<RequestStatus> getAllowedNextStatuses() {
        return Set.of(RequestStatus.SEARCHING);
    }
}
