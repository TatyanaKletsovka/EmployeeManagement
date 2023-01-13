package com.syberry.bakery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
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
    private LocalDate dateOfSignature = LocalDate.now();
    @FutureOrPresent(message = "Date can't be in past")
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
