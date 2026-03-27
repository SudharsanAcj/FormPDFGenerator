package com.formgenerator.service.rti;

import com.formgenerator.domain.model.InvestorDetails;
import com.formgenerator.domain.model.enums.AmcName;

public interface RtiSubmissionService {

    /**
     * Submits the signed PDF to the AMC's RTA (Registrar & Transfer Agent) endpoint.
     *
     * @param signedPdfBytes the PKCS#7-signed PDF bytes
     * @param investor       investor details for metadata
     * @param amcName        determines which RTI endpoint to use
     * @return RTI acknowledgement reference number
     */
    String submitSignedForm(byte[] signedPdfBytes, InvestorDetails investor, AmcName amcName);
}
