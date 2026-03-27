package com.formgenerator.domain.model;

import com.formgenerator.domain.model.enums.AccountType;
import com.formgenerator.domain.model.enums.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetails {
    private PaymentMode paymentMode;
    private String chequeOrDdNumber;
    private LocalDate chequeOrDdDate;
    private BigDecimal amount;
    private BigDecimal ddCharges;
    private BigDecimal totalAmount;
    // For online payments
    private String payInAccountNumber;
    private String bankName;
    private String branch;
    // Investor's bank account for NEFT/RTGS
    private String paymentFromAccountNumber;
    private AccountType paymentFromAccountType;
}
