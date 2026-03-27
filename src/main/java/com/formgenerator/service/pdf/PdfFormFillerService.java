package com.formgenerator.service.pdf;

import com.formgenerator.domain.model.InvestorDetails;
import com.formgenerator.mapping.AmcFieldMapping;

public interface PdfFormFillerService {

    /**
     * Fills the AcroForm fields of a PDF template using investor details
     * and the AMC-specific field mapping.
     *
     * @param pdfTemplateBytes the raw bytes of the blank PDF template
     * @param investor         the canonical investor domain model
     * @param mapping          the AMC-specific field name mapping
     * @return byte array of the filled PDF (form fields remain interactive for eSign)
     */
    byte[] fillForm(byte[] pdfTemplateBytes, InvestorDetails investor, AmcFieldMapping mapping);
}
