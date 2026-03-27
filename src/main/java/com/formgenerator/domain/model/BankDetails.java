package com.formgenerator.domain.model;

import com.formgenerator.domain.model.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDetails {
    private String bankName;
    private String accountNumber;
    private AccountType accountType;
    private String ifscCode;
    private String micrCode;
    private String branchName;
    private Address branchAddress;
}
