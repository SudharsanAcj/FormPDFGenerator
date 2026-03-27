package com.formgenerator.service.pdf.impl;

import com.formgenerator.domain.model.*;
import com.formgenerator.domain.model.enums.*;
import com.formgenerator.mapping.AmcFieldMapping;
import com.formgenerator.mapping.FieldDefinition;
import com.formgenerator.mapping.FieldType;
import com.formgenerator.service.pdf.impl.PdfFormFillerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class PdfFormFillerServiceTest {

    private PdfFormFillerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PdfFormFillerServiceImpl();
    }

    @Test
    void resolveValue_simpleField() {
        InvestorDetails investor = buildSampleInvestor();
        Object value = service.resolveValue(investor, "firstName");
        assertThat(value).isEqualTo("John");
    }

    @Test
    void resolveValue_nestedField() {
        InvestorDetails investor = buildSampleInvestor();
        Object value = service.resolveValue(investor, "bankDetails.accountNumber");
        assertThat(value).isEqualTo("123456789");
    }

    @Test
    void resolveValue_listIndexed() {
        InvestorDetails investor = buildSampleInvestor();
        Object value = service.resolveValue(investor, "nominees[0].nomineeName");
        assertThat(value).isEqualTo("Jane Doe");
    }

    @Test
    void resolveValue_outOfBoundsListIndex_returnsNull() {
        InvestorDetails investor = buildSampleInvestor();
        Object value = service.resolveValue(investor, "nominees[5].nomineeName");
        assertThat(value).isNull();
    }

    @Test
    void resolveValue_nullNestedObject_returnsNull() {
        InvestorDetails investor = InvestorDetails.builder()
                .firstName("John")
                .build();
        Object value = service.resolveValue(investor, "bankDetails.accountNumber");
        assertThat(value).isNull();
    }

    private InvestorDetails buildSampleInvestor() {
        return InvestorDetails.builder()
                .amcName(AmcName.BLACKROCK)
                .firstName("John")
                .middleName("K")
                .lastName("Doe")
                .pan("ABCDE1234F")
                .dateOfBirth(LocalDate.of(1985, 6, 15))
                .gender(Gender.MALE)
                .status(ApplicantStatus.INDIVIDUAL)
                .modeOfHolding(ModeOfHolding.SINGLE)
                .correspondenceAddress(Address.builder()
                        .line1("123 Main Street")
                        .city("Mumbai")
                        .state("Maharashtra")
                        .pinCode("400001")
                        .build())
                .mobile("9876543210")
                .email("john.doe@example.com")
                .bankDetails(BankDetails.builder()
                        .bankName("HDFC Bank")
                        .accountNumber("123456789")
                        .accountType(AccountType.SAVINGS)
                        .ifscCode("HDFC0001234")
                        .micrCode("400240002")
                        .build())
                .paymentDetails(PaymentDetails.builder()
                        .paymentMode(PaymentMode.CHEQUE)
                        .chequeOrDdNumber("123456")
                        .amount(new BigDecimal("15000000.00"))
                        .build())
                .investmentDetails(InvestmentDetails.builder()
                        .schemeName("DSP BlackRock FTP Series 13 15M")
                        .planType("Growth")
                        .investmentAmount(new BigDecimal("15000000.00"))
                        .build())
                .nominees(List.of(NomineeDetails.builder()
                        .nomineeName("Jane Doe")
                        .allocationPercentage(100)
                        .relationship("Spouse")
                        .build()))
                .distributorArn("ARN-12345")
                .euin("E-123456")
                .build();
    }
}
