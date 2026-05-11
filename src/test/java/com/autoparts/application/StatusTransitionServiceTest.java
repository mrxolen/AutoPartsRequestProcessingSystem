package com.autoparts.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.autoparts.domain.RequestCase;
import com.autoparts.domain.RequestStatus;
import java.util.List;
import org.junit.jupiter.api.Test;

class StatusTransitionServiceTest {

    private final StatusTransitionService statusTransitionService = new StatusTransitionService(List.of(
            new NewState(),
            new SearchingState(),
            new OfferReadyState(),
            new SentToClientState(),
            new AcceptedState(),
            new RejectedState(),
            new CompletedState()
    ));

    @Test
    void allowsValidWorkflowUntilCompleted() {
        RequestCase requestCase = requestCaseWithStatus(RequestStatus.NEW);

        statusTransitionService.changeStatus(requestCase, RequestStatus.SEARCHING);
        statusTransitionService.changeStatus(requestCase, RequestStatus.OFFER_READY);
        statusTransitionService.changeStatus(requestCase, RequestStatus.SENT_TO_CLIENT);
        statusTransitionService.changeStatus(requestCase, RequestStatus.ACCEPTED);
        statusTransitionService.changeStatus(requestCase, RequestStatus.COMPLETED);

        assertThat(requestCase.getStatus()).isEqualTo(RequestStatus.COMPLETED);
    }

    @Test
    void allowsSentToClientToRejected() {
        RequestCase requestCase = requestCaseWithStatus(RequestStatus.SENT_TO_CLIENT);

        statusTransitionService.changeStatus(requestCase, RequestStatus.REJECTED);

        assertThat(requestCase.getStatus()).isEqualTo(RequestStatus.REJECTED);
    }

    @Test
    void rejectsInvalidTransition() {
        RequestCase requestCase = requestCaseWithStatus(RequestStatus.NEW);

        assertThatThrownBy(() -> statusTransitionService.changeStatus(requestCase, RequestStatus.COMPLETED))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessage("Invalid request status transition: NEW -> COMPLETED");

        assertThat(requestCase.getStatus()).isEqualTo(RequestStatus.NEW);
    }

    @Test
    void rejectsTransitionFromFinalStatus() {
        RequestCase requestCase = requestCaseWithStatus(RequestStatus.REJECTED);

        assertThatThrownBy(() -> statusTransitionService.changeStatus(requestCase, RequestStatus.SEARCHING))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessage("Invalid request status transition: REJECTED -> SEARCHING");

        assertThat(requestCase.getStatus()).isEqualTo(RequestStatus.REJECTED);
    }

    private RequestCase requestCaseWithStatus(RequestStatus status) {
        RequestCase requestCase = new RequestCase();
        requestCase.setStatus(status);
        return requestCase;
    }
}
