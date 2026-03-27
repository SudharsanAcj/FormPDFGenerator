package com.formgenerator.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class InvestmentRequest {

    @NotBlank(message = "AMC name is required")
    private String amcName;

    private String transactionChargeType;
    private String existingFolioNumber;

    // Primary Applicant
    private String title;

    @NotBlank(message = "First name is required")
    private String firstName;

    private String middleName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateOfBirth;

    private String gender;
    private String status;
    private String occupation;

    @NotBlank(message = "PAN is required")
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "Invalid PAN format")
    private String pan;

    private String kycNumber;

    // Minor / POA
    private boolean minorInvestment;
    private String guardianName;
    private String guardianRelation;
    private String guardianPan;
    private String poaHolderName;
    private String poaPan;

    // Contact
    @Valid
    private AddressDto correspondenceAddress;

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
    private String mobile;

    private String stdCode;
    private String officePhone;
    private String residencePhone;
    private boolean wantOnlinePin;

    // Joint applicants
    private List<@Valid JointApplicantDto> jointApplicants;
    private String modeOfHolding;

    // Bank Details
    @NotNull(message = "Bank details are required")
    @Valid
    private BankDetailsDto bankDetails;

    // Payment Details
    @NotNull(message = "Payment details are required")
    @Valid
    private PaymentDetailsDto paymentDetails;

    // Investment Details
    @NotNull(message = "Investment details are required")
    @Valid
    private InvestmentDetailsDto investmentDetails;

    // Nominees
    private List<@Valid NomineeDto> nominees;

    private String unitHoldingOption;
    private String distributorArn;
    private String subBrokerCode;
    private String euin;
    private boolean euinDeclaration;

    /**
     * Aadhaar number or Virtual ID for eSign.
     * Not mapped to domain model — used only for eSign flow.
     */
    @Pattern(regexp = "^[0-9]{12}$|^[0-9]{16}$", message = "Invalid Aadhaar/VID format")
    private String aadhaarOrVid;

    // ── Nested DTOs ──────────────────────────────────────────────────────

    @Data
    public static class AddressDto {
        @NotBlank private String line1;
        private String line2;
        private String line3;
        @NotBlank private String city;
        @NotBlank private String state;
        private String country;
        @NotBlank @Pattern(regexp = "^[1-9][0-9]{5}$") private String pinCode;
    }

    @Data
    public static class BankDetailsDto {
        @NotBlank private String bankName;
        @NotBlank private String accountNumber;
        @NotBlank private String accountType;
        @NotBlank @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$") private String ifscCode;
        private String micrCode;
        private String branchName;
        private AddressDto branchAddress;
    }

    @Data
    public static class PaymentDetailsDto {
        @NotBlank private String paymentMode;
        private String chequeOrDdNumber;
        @JsonFormat(pattern = "dd/MM/yyyy") private LocalDate chequeOrDdDate;
        @NotNull @Positive private BigDecimal amount;
        private BigDecimal ddCharges;
        private BigDecimal totalAmount;
        private String payInAccountNumber;
        private String bankName;
        private String branch;
        private String paymentFromAccountNumber;
        private String paymentFromAccountType;
    }

    @Data
    public static class InvestmentDetailsDto {
        @NotBlank private String schemeName;
        private String schemeCode;
        @NotBlank private String planType;
        private String dividendOption;
        @Positive private BigDecimal investmentAmount;
        private String switchToSchemeName;
        private String switchPlanType;
    }

    @Data
    public static class JointApplicantDto {
        private String title;
        @NotBlank private String firstName;
        private String middleName;
        @NotBlank private String lastName;
        @JsonFormat(pattern = "dd/MM/yyyy") private LocalDate dateOfBirth;
        private String gender;
        @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}") private String pan;
        private String kycNumber;
    }

    @Data
    public static class NomineeDto {
        @NotBlank private String nomineeName;
        @Min(1) @Max(100) private int allocationPercentage;
        @JsonFormat(pattern = "dd/MM/yyyy") private LocalDate dateOfBirth;
        private String relationship;
        private String guardianName;
        private AddressDto address;
    }
}
