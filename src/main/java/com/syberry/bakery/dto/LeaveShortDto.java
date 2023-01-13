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
public class LeaveShortDto {

    private Long id;
    private String firstName;
    private String lastName;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LeaveStatus leaveStatus;
}
