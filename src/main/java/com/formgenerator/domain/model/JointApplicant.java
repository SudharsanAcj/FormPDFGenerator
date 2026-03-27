package com.formgenerator.domain.model;

import com.formgenerator.domain.model.enums.Gender;
import com.formgenerator.domain.model.enums.Title;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JointApplicant {
    private Title title;
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String pan;
    private String kycNumber;
}
