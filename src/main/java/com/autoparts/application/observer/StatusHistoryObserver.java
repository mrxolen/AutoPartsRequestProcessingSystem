package com.autoparts.application.observer;

import com.autoparts.domain.RequestCase;
import com.autoparts.domain.RequestStatus;
import com.autoparts.domain.StatusHistoryEntry;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class StatusHistoryObserver implements RequestStatusObserver {

    @Override
    public void onStatusChanged(RequestCase requestCase, RequestStatus oldStatus, RequestStatus newStatus) {
        StatusHistoryEntry historyEntry = new StatusHistoryEntry();
        historyEntry.setRequestCase(requestCase);
        historyEntry.setOldStatus(oldStatus);
        historyEntry.setNewStatus(newStatus);
        historyEntry.setChangedDate(LocalDateTime.now());

        requestCase.getStatusHistory().add(historyEntry);
    }
}
