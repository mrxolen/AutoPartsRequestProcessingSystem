package com.autoparts.application;

import com.autoparts.domain.RequestCase;
import com.autoparts.domain.RequestStatus;

public interface RequestStatusObserver {

    void onStatusChanged(RequestCase requestCase, RequestStatus oldStatus, RequestStatus newStatus);
}
