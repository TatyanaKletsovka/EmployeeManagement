package com.syberry.bakery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveSaveDto {

    private Long id;
    @NotNull(message = "Employee id is required")
    private Long employeeId;
    private LeaveType leaveType = LeaveType.SICK_DAY;
    @FutureOrPresent(message = "Leave start date can't be in past")
    private LocalDate leaveStartDate = LocalDate.now();
    @FutureOrPresent(message = "Leave end data can't be in past")
    private LocalDate leaveEndDate = LocalDate.now();
    @NotNull(message = "Status is required")
    private LeaveStatus leaveStatus;
    private String leaveReason;
}
