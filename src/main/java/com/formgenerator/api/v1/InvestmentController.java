package com.formgenerator.api.v1;

import com.formgenerator.domain.dto.request.InvestmentRequest;
import com.formgenerator.domain.dto.response.InvestmentResponse;
import com.formgenerator.domain.dto.response.TransactionStatusResponse;
import com.formgenerator.service.InvestmentOrchestrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/investments")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentOrchestrationService orchestrationService;

    /**
     * Initiates the fresh purchase flow.
     * Returns the eSign redirect URL for the user to authenticate via Aadhaar OTP.
     */
    @PostMapping("/fresh-purchase")
    public ResponseEntity<InvestmentResponse> initiateFreshPurchase(
            @Valid @RequestBody InvestmentRequest request) {

        log.info("Received fresh purchase request for AMC: [{}]", request.getAmcName());
        InvestmentResponse response = orchestrationService.initiateFreshPurchase(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Returns the current status of a fresh purchase transaction.
     */
    @GetMapping("/{transactionId}/status")
    public ResponseEntity<TransactionStatusResponse> getStatus(
            @PathVariable String transactionId) {

        return ResponseEntity.ok(orchestrationService.getTransactionStatus(transactionId));
    }
}
