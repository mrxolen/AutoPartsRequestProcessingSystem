package com.autoparts.application.observer;

import com.autoparts.domain.RequestCase;
import com.autoparts.domain.RequestStatus;

public interface RequestStatusObserver {

    void onStatusChanged(RequestCase requestCase, RequestStatus oldStatus, RequestStatus newStatus);
}
