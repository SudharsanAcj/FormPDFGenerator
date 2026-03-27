package com.formgenerator.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NomineeDetails {
    private String nomineeName;
    private Integer allocationPercentage;
    private LocalDate dateOfBirth;
    private String relationship;
    // For minor nominee
    private String guardianName;
    private Address address;
}
