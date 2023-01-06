package com.syberry.bakery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContractFullDto {

    private Long contractId;
    private Position position;
    private String contractDuration;
    private LocalDate dateOfSignature;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private AssignmentType type;
    private Boolean probationPeriod;
    private LocalDate probationStartDate;
    private LocalDate probationEndDate;
}
