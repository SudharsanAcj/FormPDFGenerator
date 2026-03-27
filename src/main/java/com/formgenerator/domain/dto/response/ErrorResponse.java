package com.formgenerator.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    String errorCode;
    String message;
    List<String> validationErrors;
    String transactionId;
    Instant timestamp;
}
