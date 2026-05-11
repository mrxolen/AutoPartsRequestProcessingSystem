package com.autoparts.application;

import com.autoparts.domain.RequestStatus;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class OfferReadyState implements RequestState {

    @Override
    public RequestStatus getStatus() {
        return RequestStatus.OFFER_READY;
    }

    @Override
    public Set<RequestStatus> getAllowedNextStatuses() {
        return Set.of(RequestStatus.SENT_TO_CLIENT);
    }
}
