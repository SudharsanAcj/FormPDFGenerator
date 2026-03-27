package com.formgenerator.service.rti.impl;

import com.formgenerator.config.RtiProperties;
import com.formgenerator.domain.model.InvestorDetails;
import com.formgenerator.domain.model.enums.AmcName;
import com.formgenerator.exception.RtiSubmissionException;
import com.formgenerator.service.rti.RtiSubmissionService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RtiSubmissionServiceImpl implements RtiSubmissionService {

    private final RtiProperties rtiProperties;

    @Qualifier("rtiWebClient")
    private final WebClient rtiWebClient;

    @Override
    @CircuitBreaker(name = "rtiSubmission", fallbackMethod = "rtiSubmissionFallback")
    @Retry(name = "rtiSubmission")
    public String submitSignedForm(byte[] signedPdfBytes, InvestorDetails investor, AmcName amcName) {
        log.info("Submitting signed form to RTI for AMC: [{}]", amcName.getCode());

        RtiProperties.RtiEndpointConfig endpointConfig = getRtiConfig(amcName);

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", new ByteArrayResource(signedPdfBytes) {
            @Override
            public String getFilename() {
                return "fresh_purchase_" + amcName.getCode() + "_" + investor.getPan() + ".pdf";
            }
        }).contentType(MediaType.APPLICATION_PDF);

        bodyBuilder.part("metadata", Map.of(
                "amcCode", amcName.getCode(),
                "investorPan", investor.getPan(),
                "investorName", investor.getFirstName() + " " + investor.getLastName(),
                "investmentAmount", investor.getPaymentDetails().getAmount().toPlainString(),
                "schemeName", investor.getInvestmentDetails().getSchemeName()
        ));

        try {
            WebClient.RequestBodySpec requestSpec = rtiWebClient.post()
                    .uri(endpointConfig.getUrl())
                    .contentType(MediaType.MULTIPART_FORM_DATA);

            // Apply authentication
            if ("API_KEY".equalsIgnoreCase(endpointConfig.getAuthType())) {
                requestSpec = requestSpec.header("X-API-Key", endpointConfig.getApiKey());
            } else if ("OAUTH2".equalsIgnoreCase(endpointConfig.getAuthType())) {
                String token = fetchOAuthToken(endpointConfig.getOauth2());
                requestSpec = requestSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }

            String acknowledgement = requestSpec
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("RTI submission successful for AMC [{}] — ack: {}", amcName.getCode(), acknowledgement);
            return acknowledgement;

        } catch (WebClientResponseException e) {
            log.error("RTI endpoint returned HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RtiSubmissionException(amcName.getCode(),
                    "HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
        } catch (RtiSubmissionException e) {
            throw e;
        } catch (Exception e) {
            throw new RtiSubmissionException(amcName.getCode(), e.getMessage(), e);
        }
    }

    private RtiProperties.RtiEndpointConfig getRtiConfig(AmcName amcName) {
        RtiProperties.RtiEndpointConfig config = rtiProperties.getEndpoints().get(amcName.getCode());
        if (config == null || config.getUrl() == null) {
            throw new RtiSubmissionException(amcName.getCode(),
                    "No RTI endpoint configured for AMC: " + amcName.getCode());
        }
        return config;
    }

    private String fetchOAuthToken(RtiProperties.RtiEndpointConfig.OAuth2Config oauth2) {
        // Token fetch via client_credentials grant
        return rtiWebClient.post()
                .uri(oauth2.getTokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", oauth2.getClientId())
                        .with("client_secret", oauth2.getClientSecret())
                        .with("scope", oauth2.getScope() != null ? oauth2.getScope() : ""))
                .retrieve()
                .bodyToMono(Map.class)
                .map(resp -> (String) resp.get("access_token"))
                .block();
    }

    @SuppressWarnings("unused")
    private String rtiSubmissionFallback(byte[] pdfBytes, InvestorDetails investor, AmcName amcName, Throwable t) {
        log.error("RTI submission circuit breaker open for AMC [{}]. Cause: {}", amcName.getCode(), t.getMessage());
        throw new RtiSubmissionException(amcName.getCode(),
                "RTI endpoint is temporarily unavailable. The signed PDF has been retained for retry.");
    }
}
