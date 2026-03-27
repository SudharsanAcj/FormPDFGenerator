package com.formgenerator.service.esign;

import com.formgenerator.esign.model.ESignRequest;
import com.formgenerator.esign.model.ESignResponse;

public interface AadhaarESignService {

    /**
     * Builds the eSign XML request, signs it with the ASP private key,
     * and POSTs it to the CDAC eSign gateway.
     *
     * @return the eSign gateway redirect URL for the user's browser
     */
    String initiateESign(ESignRequest request);

    /**
     * Parses and verifies the eSign callback response XML from the gateway.
     *
     * @param responseXml the raw XML POSTed by the gateway to the callback URL
     * @return parsed and verified ESignResponse
     */
    ESignResponse processCallback(String responseXml);
}
