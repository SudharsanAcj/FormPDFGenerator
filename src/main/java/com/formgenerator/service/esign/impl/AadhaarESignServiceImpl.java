package com.formgenerator.service.esign.impl;

import com.formgenerator.config.AadhaarESignProperties;
import com.formgenerator.esign.model.ESignRequest;
import com.formgenerator.esign.model.ESignResponse;
import com.formgenerator.esign.xml.ESignXmlBuilder;
import com.formgenerator.esign.xml.ESignXmlParser;
import com.formgenerator.exception.ESignException;
import com.formgenerator.service.esign.AadhaarESignService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AadhaarESignServiceImpl implements AadhaarESignService {

    private final ESignXmlBuilder xmlBuilder;
    private final ESignXmlParser xmlParser;
    private final AadhaarESignProperties eSignProperties;
    @Qualifier("eSignWebClient")
    private final WebClient eSignWebClient;

    @Override
    @CircuitBreaker(name = "esignGateway", fallbackMethod = "eSignFallback")
    public String initiateESign(ESignRequest request) {
        log.info("Initiating Aadhaar eSign for txn: {}", request.getTransactionId());

        String signedXml = xmlBuilder.buildSignedXml(request);

        // POST signed XML to CDAC gateway — gateway returns redirect URL or HTML
        try {
            String gatewayResponse = eSignWebClient.post()
                    .contentType(MediaType.APPLICATION_XML)
                    .bodyValue(signedXml)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // In live CDAC integration, the gateway returns a redirect URL
            // or the XML itself contains the redirect URL as an attribute.
            // Return the gateway response (redirect URL) to the controller.
            log.info("eSign gateway accepted request for txn: {}", request.getTransactionId());
            return gatewayResponse;

        } catch (WebClientResponseException e) {
            log.error("eSign gateway returned HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ESignException("GATEWAY_HTTP_ERROR",
                    "eSign gateway returned " + e.getStatusCode());
        } catch (Exception e) {
            throw new ESignException("Failed to reach eSign gateway: " + e.getMessage(), e);
        }
    }

    @Override
    public ESignResponse processCallback(String responseXml) {
        log.info("Processing eSign callback response");
        ESignResponse response = xmlParser.parse(responseXml);
        log.info("eSign callback processed — txn: {}, status: {}",
                response.getTransactionId(), response.getStatus());
        return response;
    }

    @SuppressWarnings("unused")
    private String eSignFallback(ESignRequest request, Throwable t) {
        log.error("eSign gateway circuit breaker open for txn: {}. Cause: {}",
                request.getTransactionId(), t.getMessage());
        throw new ESignException("CIRCUIT_OPEN",
                "eSign gateway is temporarily unavailable. Please retry after some time.");
    }
}
