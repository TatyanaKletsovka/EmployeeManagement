package com.syberry.bakery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContractSaveDto {

    private Long id;
    @NotNull(message = "Employee is required")
    private Long employeeId;
    @NotNull(message = "Position is required")
    private Position position;
    @PastOrPresent(message = "Date can't be in feature")
    private LocalDate dateOfSignature = LocalDate.now();
    @PastOrPresent(message = "Date can't be in feature")
    private LocalDate contractStartDate = LocalDate.now();
    @NotNull(message = "Contract end date is required")
    @Future(message = "Data should be in feature")
    private LocalDate contractEndDate;
    @NotNull(message = "Assignment type is required")
    private AssignmentType type;
    private Boolean probationPeriod = false;
    private LocalDate probationStartDate;
    private LocalDate probationEndDate;
}
