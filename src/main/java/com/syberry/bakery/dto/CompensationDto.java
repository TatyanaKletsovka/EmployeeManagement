package com.syberry.bakery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompensationDto {

    private Long id;
    @NotNull
    @Positive
    private float amount;
    @NotNull
    private LocalDate effectiveFrom;
    @NotNull
    private LocalDate validUntil;
    @NotNull
    @Size(max = 500)
    private String specialConditions;
    @NotNull
    private Long employeeId;
    private Long previousCompensationId;
    private Long nextCompensationId;
}
