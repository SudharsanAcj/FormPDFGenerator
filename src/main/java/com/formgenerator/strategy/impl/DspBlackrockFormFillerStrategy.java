package com.formgenerator.strategy.impl;

import com.formgenerator.domain.model.InvestorDetails;
import com.formgenerator.domain.model.enums.AmcName;
import com.formgenerator.mapping.AmcFieldMapping;
import com.formgenerator.service.pdf.PdfFormFillerService;
import com.formgenerator.strategy.FormFillerStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DspBlackrockFormFillerStrategy implements FormFillerStrategy {

    private final PdfFormFillerService pdfFormFillerService;

    @Override
    public AmcName getSupportedAmc() {
        return AmcName.BLACKROCK;
    }

    @Override
    public byte[] fill(byte[] pdfTemplateBytes, InvestorDetails investor, AmcFieldMapping mapping) {
        log.debug("Filling DSP BlackRock FTP form for investor PAN: {}",
                maskPan(investor.getPan()));
        // Delegate to the generic PDFBox filler; add DSP-specific overrides below if needed
        return pdfFormFillerService.fillForm(pdfTemplateBytes, investor, mapping);
    }

    private String maskPan(String pan) {
        if (pan == null || pan.length() < 4) return "****";
        return "****" + pan.substring(pan.length() - 4);
    }
}
