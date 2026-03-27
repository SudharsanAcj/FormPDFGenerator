package com.formgenerator.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentDetails {
    private String schemeName;
    private String schemeCode;
    private String planType;        // Growth / Dividend
    private String dividendOption;  // Reinvestment / Payout
    private BigDecimal investmentAmount;
    // Switch on maturity (for FTPs)
    private String switchToSchemeName;
    private String switchPlanType;
}
