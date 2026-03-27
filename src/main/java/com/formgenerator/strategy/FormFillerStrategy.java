package com.formgenerator.strategy;

import com.formgenerator.domain.model.InvestorDetails;
import com.formgenerator.domain.model.enums.AmcName;
import com.formgenerator.mapping.AmcFieldMapping;

public interface FormFillerStrategy {

    /**
     * The AMC this strategy handles.
     */
    AmcName getSupportedAmc();

    /**
     * Fills the PDF template and returns the filled PDF bytes.
     * Implementations may add AMC-specific pre/post processing around the
     * standard PDFBox form filling.
     */
    byte[] fill(byte[] pdfTemplateBytes, InvestorDetails investor, AmcFieldMapping mapping);
}
