package com.formgenerator.audit;

import com.formgenerator.domain.model.enums.AmcName;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class AuditEvent {
    String transactionId;
    String eventType;
    AmcName amcName;
    String status;
    String details;
    Instant timestamp;
}
