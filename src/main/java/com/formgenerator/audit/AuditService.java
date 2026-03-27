package com.formgenerator.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class AuditService {

    public void record(AuditEvent event) {
        // In production: persist to an audit table or publish to a message bus
        log.info("[AUDIT] txn={} event={} amc={} status={} detail={}",
                event.getTransactionId(),
                event.getEventType(),
                event.getAmcName(),
                event.getStatus(),
                event.getDetails());
    }

    public AuditEvent buildEvent(String transactionId, String eventType,
                                  com.formgenerator.domain.model.enums.AmcName amcName,
                                  String status, String details) {
        return AuditEvent.builder()
                .transactionId(transactionId)
                .eventType(eventType)
                .amcName(amcName)
                .status(status)
                .details(details)
                .timestamp(Instant.now())
                .build();
    }
}
