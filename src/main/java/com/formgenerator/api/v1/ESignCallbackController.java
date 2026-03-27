package com.formgenerator.api.v1;

import com.formgenerator.esign.model.ESignResponse;
import com.formgenerator.service.InvestmentOrchestrationService;
import com.formgenerator.service.esign.AadhaarESignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/esign")
@RequiredArgsConstructor
public class ESignCallbackController {

    private final AadhaarESignService eSignService;
    private final InvestmentOrchestrationService orchestrationService;

    /**
     * Called by the CDAC eSign gateway after the investor completes Aadhaar OTP authentication.
     * The gateway POSTs the signed XML response to this endpoint.
     *
     * The transactionId is carried in the eSign response XML itself (txn attribute).
     */
    @PostMapping("/callback")
    public ResponseEntity<Void> handleESignCallback(@RequestBody String responseXml) {
        log.info("Received eSign callback from gateway");

        ESignResponse eSignResponse = eSignService.processCallback(responseXml);
        String transactionId = eSignResponse.getTransactionId();

        log.info("eSign callback — txn: {}, status: {}", transactionId, eSignResponse.getStatus());
        orchestrationService.completeAfterESign(transactionId, eSignResponse);

        // Return 200 OK to the gateway to acknowledge receipt
        return ResponseEntity.ok().build();
    }
}
