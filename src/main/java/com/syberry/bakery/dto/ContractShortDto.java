package com.syberry.bakery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractShortDto {

    private Long employeeId;
    private String firstName;
    private String lastName;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
}
