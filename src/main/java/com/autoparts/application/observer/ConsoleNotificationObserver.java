package com.autoparts.application.observer;

import com.autoparts.domain.RequestCase;
import com.autoparts.domain.RequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConsoleNotificationObserver implements RequestStatusObserver {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleNotificationObserver.class);

    @Override
    public void onStatusChanged(RequestCase requestCase, RequestStatus oldStatus, RequestStatus newStatus) {
        logger.info("Request status changed: {} -> {}", oldStatus, newStatus);
    }
}
