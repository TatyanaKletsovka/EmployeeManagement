package com.syberry.bakery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompensationFilterDto {

    private static final LocalDate DEFAULT_START_DATE = LocalDate.of(2000, 1, 1);
    private static final LocalDate DEFAULT_END_DATE = LocalDate.of(3000, 1, 1);

    private String name = "";
    private float amountStart = 0;
    private float amountEnd = 1000000000;
    private LocalDate effectiveFromStart = DEFAULT_START_DATE;
    private LocalDate effectiveFromEnd = DEFAULT_END_DATE;
    private LocalDate validUntilStart = DEFAULT_START_DATE;
    private LocalDate validUntilEnd = DEFAULT_END_DATE;
    private Boolean isUpdated = null;
    private LocalDate updatedAtStart = DEFAULT_START_DATE;
    private LocalDate updatedAtEnd = DEFAULT_END_DATE;
}
