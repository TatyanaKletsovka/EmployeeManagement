package com.syberry.bakery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {

    private Long id;
    private String firstName;
    private String lastName;
    @NotNull
    @Past(message = "Date of birth can't be in future")
    private LocalDate dateOfBirth;
    @NotBlank
    @Size(min = 5, max = 10)
    @Pattern(regexp = "^\\d{10}$", message = "The phone number must consist of 10 digits")
    private String phone;
    @NotBlank
    @Email
    @Size(max = 255)
    private String personalEmail;
    @NotNull
    private Gender gender;
    @NotBlank
    @Size(max = 100)
    private String addressLine1;
    @NotNull
    @Size(max = 100)
    private String addressLine2;
    @NotBlank
    @Size(max = 255)
    private String addressCountry;
    @NotBlank
    @Size(max = 2)
    private String addressState;
    @NotBlank
    @Size(max = 10)
    private String addressZip;
    @NotNull
    private IdType idType;
    @NotBlank
    @Size(max = 10)
    private String idNumber;
    @NotBlank
    @Size(max = 255)
    private String citizenship;
    @NotNull
    private MaritalStatus maritalStatus;
    private int numberOfChildren;
    private String email;

}
