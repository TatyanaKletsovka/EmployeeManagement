package com.syberry.bakery.converter;

import com.syberry.bakery.dto.LeaveFullDto;
import com.syberry.bakery.dto.LeaveSaveDto;
import com.syberry.bakery.dto.LeaveShortDto;
import com.syberry.bakery.dto.LeaveType;
import com.syberry.bakery.entity.Leave;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.repository.EmployeeRepository;
import com.syberry.bakery.util.LeaveUtil;
import lombok.experimental.UtilityClass;

import static java.time.temporal.ChronoUnit.DAYS;

@UtilityClass
public class LeaveConverter {

    public Leave toEntity(LeaveSaveDto dto, EmployeeRepository employeeRepository) {
        return Leave.builder()
                .id(dto.getId())
                .employee(employeeRepository.findByIdAndUserIsBlockedFalse(dto.getEmployeeId())
                        .orElseThrow(() -> new EntityNotFoundException("There is no such employee")))
                .leaveType(dto.getLeaveType())
                .leaveStartDate(dto.getLeaveStartDate())
                .leaveEndDate(dto.getLeaveEndDate())
                .leaveStatus(dto.getLeaveStatus())
                .leaveReason(dto.getLeaveReason())
                .build();
    }

    public LeaveShortDto toShortDto(Leave entity) {
        return new LeaveShortDto(entity.getId(),
                entity.getEmployee().getUser().getFirstName(),
                entity.getEmployee().getUser().getLastName(),
                entity.getLeaveType(),
                entity.getLeaveStartDate(),
                entity.getLeaveStatus());
    }

    public LeaveFullDto toFullDto(Leave entity, LeaveUtil leaveUtil) {
        int leaveDuration = (int) DAYS.between(entity.getLeaveStartDate(), entity.getLeaveEndDate()) + 1;
        int remainingSickDays = leaveUtil.getRemainingDays(entity, LeaveType.SICK_DAY);
        int remainingPaidLeaveDays = leaveUtil.getRemainingDays(entity, LeaveType.PAID_LEAVE);
        return new LeaveFullDto(entity.getId(),
                entity.getLeaveType(),
                entity.getLeaveStartDate(),
                entity.getLeaveEndDate(),
                leaveDuration,
                entity.getLeaveStatus(),
                remainingSickDays,
                remainingPaidLeaveDays,
                entity.getLeaveReason());
    }
}
