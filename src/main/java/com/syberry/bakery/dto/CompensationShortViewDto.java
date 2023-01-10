package com.syberry.bakery.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompensationShortViewDto {

    private Long id;
    private String employeeFirstName;
    private String employeeLastName;
    private Long employeeId;
    private LocalDate effectiveFrom;
    private LocalDate validUntil;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime updatedAt;
    private float amount;
}
