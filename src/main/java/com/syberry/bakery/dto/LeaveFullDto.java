package com.syberry.bakery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveFullDto {

    private Long id;
    private LeaveType leaveType;
    private LocalDate leaveStartDate;
    private LocalDate leaveEndDate;
    private int leaveDuration;
    private LeaveStatus leaveStatus;
    private int remainingSickDays;
    private int remainingPaidLeaveDays;
    private String leaveReason;
}
