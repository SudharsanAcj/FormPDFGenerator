package com.formgenerator.domain.model;

import com.formgenerator.domain.model.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestorDetails {

    // ── Transaction level ──────────────────────────────────────────────
    private AmcName amcName;
    private TransactionChargeType transactionChargeType;
    private String existingFolioNumber;

    // ── Primary applicant ──────────────────────────────────────────────
    private Title title;
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String pan;
    private ApplicantStatus status;
    private String occupation;
    private String kycNumber;

    // ── Minor / POA ────────────────────────────────────────────────────
    private boolean minorInvestment;
    private String guardianName;
    private String guardianRelation;
    private String guardianPan;
    private String poaHolderName;
    private String poaPan;

    // ── Contact ────────────────────────────────────────────────────────
    private Address correspondenceAddress;
    private String email;
    private String mobile;
    private String stdCode;
    private String officePhone;
    private String residencePhone;
    private boolean wantOnlinePin;

    // ── Joint applicants (max 2) ───────────────────────────────────────
    private List<JointApplicant> jointApplicants;
    private ModeOfHolding modeOfHolding;

    // ── Bank ───────────────────────────────────────────────────────────
    private BankDetails bankDetails;

    // ── Payment ────────────────────────────────────────────────────────
    private PaymentDetails paymentDetails;

    // ── Investment ─────────────────────────────────────────────────────
    private InvestmentDetails investmentDetails;

    // ── Nominees (max 3) ───────────────────────────────────────────────
    private List<NomineeDetails> nominees;

    // ── Preferences ────────────────────────────────────────────────────
    private UnitHoldingOption unitHoldingOption;

    // ── Distributor ────────────────────────────────────────────────────
    private String distributorArn;
    private String subBrokerCode;
    private String euin;
    private boolean euinDeclaration;
}
