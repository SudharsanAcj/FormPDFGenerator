package com.formgenerator.domain.mapper;

import com.formgenerator.domain.dto.request.InvestmentRequest;
import com.formgenerator.domain.model.*;
import com.formgenerator.domain.model.enums.*;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InvestorDetailsMapper {

    @Mapping(target = "amcName", expression = "java(com.formgenerator.domain.model.enums.AmcName.fromCode(request.getAmcName()))")
    @Mapping(target = "transactionChargeType", expression = "java(mapTransactionChargeType(request.getTransactionChargeType()))")
    @Mapping(target = "title", expression = "java(mapTitle(request.getTitle()))")
    @Mapping(target = "gender", expression = "java(mapGender(request.getGender()))")
    @Mapping(target = "status", expression = "java(mapApplicantStatus(request.getStatus()))")
    @Mapping(target = "modeOfHolding", expression = "java(mapModeOfHolding(request.getModeOfHolding()))")
    @Mapping(target = "unitHoldingOption", expression = "java(mapUnitHoldingOption(request.getUnitHoldingOption()))")
    @Mapping(target = "correspondenceAddress", source = "correspondenceAddress")
    @Mapping(target = "bankDetails", source = "bankDetails")
    @Mapping(target = "paymentDetails", source = "paymentDetails")
    @Mapping(target = "investmentDetails", source = "investmentDetails")
    @Mapping(target = "jointApplicants", source = "jointApplicants")
    @Mapping(target = "nominees", source = "nominees")
    InvestorDetails toInvestorDetails(InvestmentRequest request);

    default Address mapAddress(InvestmentRequest.AddressDto dto) {
        if (dto == null) return null;
        return Address.builder()
                .line1(dto.getLine1()).line2(dto.getLine2()).line3(dto.getLine3())
                .city(dto.getCity()).state(dto.getState())
                .country(dto.getCountry()).pinCode(dto.getPinCode())
                .build();
    }

    default BankDetails mapBankDetails(InvestmentRequest.BankDetailsDto dto) {
        if (dto == null) return null;
        return BankDetails.builder()
                .bankName(dto.getBankName())
                .accountNumber(dto.getAccountNumber())
                .accountType(AccountType.fromCode(dto.getAccountType()))
                .ifscCode(dto.getIfscCode())
                .micrCode(dto.getMicrCode())
                .branchName(dto.getBranchName())
                .branchAddress(mapAddress(dto.getBranchAddress()))
                .build();
    }

    default PaymentDetails mapPaymentDetails(InvestmentRequest.PaymentDetailsDto dto) {
        if (dto == null) return null;
        return PaymentDetails.builder()
                .paymentMode(PaymentMode.fromCode(dto.getPaymentMode()))
                .chequeOrDdNumber(dto.getChequeOrDdNumber())
                .chequeOrDdDate(dto.getChequeOrDdDate())
                .amount(dto.getAmount())
                .ddCharges(dto.getDdCharges())
                .totalAmount(dto.getTotalAmount())
                .payInAccountNumber(dto.getPayInAccountNumber())
                .bankName(dto.getBankName())
                .branch(dto.getBranch())
                .paymentFromAccountNumber(dto.getPaymentFromAccountNumber())
                .paymentFromAccountType(dto.getPaymentFromAccountType() != null
                        ? AccountType.fromCode(dto.getPaymentFromAccountType()) : null)
                .build();
    }

    default InvestmentDetails mapInvestmentDetails(InvestmentRequest.InvestmentDetailsDto dto) {
        if (dto == null) return null;
        return InvestmentDetails.builder()
                .schemeName(dto.getSchemeName())
                .schemeCode(dto.getSchemeCode())
                .planType(dto.getPlanType())
                .dividendOption(dto.getDividendOption())
                .investmentAmount(dto.getInvestmentAmount())
                .switchToSchemeName(dto.getSwitchToSchemeName())
                .switchPlanType(dto.getSwitchPlanType())
                .build();
    }

    default List<JointApplicant> mapJointApplicants(List<InvestmentRequest.JointApplicantDto> dtos) {
        if (dtos == null) return null;
        return dtos.stream().map(dto -> JointApplicant.builder()
                .title(dto.getTitle() != null ? Title.fromCode(dto.getTitle()) : null)
                .firstName(dto.getFirstName()).middleName(dto.getMiddleName()).lastName(dto.getLastName())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender() != null ? Gender.fromCode(dto.getGender()) : null)
                .pan(dto.getPan()).kycNumber(dto.getKycNumber())
                .build()).collect(Collectors.toList());
    }

    default List<NomineeDetails> mapNominees(List<InvestmentRequest.NomineeDto> dtos) {
        if (dtos == null) return null;
        return dtos.stream().map(dto -> NomineeDetails.builder()
                .nomineeName(dto.getNomineeName())
                .allocationPercentage(dto.getAllocationPercentage())
                .dateOfBirth(dto.getDateOfBirth())
                .relationship(dto.getRelationship())
                .guardianName(dto.getGuardianName())
                .address(mapAddress(dto.getAddress()))
                .build()).collect(Collectors.toList());
    }

    default TransactionChargeType mapTransactionChargeType(String code) {
        return code != null ? TransactionChargeType.fromCode(code) : TransactionChargeType.EXISTING_INVESTOR;
    }

    default Title mapTitle(String code) {
        return code != null ? Title.fromCode(code) : null;
    }

    default Gender mapGender(String code) {
        return code != null ? Gender.fromCode(code) : null;
    }

    default ApplicantStatus mapApplicantStatus(String code) {
        return code != null ? ApplicantStatus.fromCode(code) : ApplicantStatus.INDIVIDUAL;
    }

    default ModeOfHolding mapModeOfHolding(String code) {
        return code != null ? ModeOfHolding.fromCode(code) : ModeOfHolding.SINGLE;
    }

    default UnitHoldingOption mapUnitHoldingOption(String code) {
        return code != null ? UnitHoldingOption.fromCode(code) : UnitHoldingOption.PHYSICAL;
    }
}
