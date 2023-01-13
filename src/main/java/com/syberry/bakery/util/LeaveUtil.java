package com.syberry.bakery.util;

import com.syberry.bakery.dto.LeaveType;
import com.syberry.bakery.entity.Contract;
import com.syberry.bakery.entity.Leave;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.repository.ContractRepository;
import com.syberry.bakery.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static java.time.temporal.ChronoUnit.DAYS;

@Component
@RequiredArgsConstructor
public class LeaveUtil {

    private final LeaveRepository leaveRepository;
    private final ContractRepository contractRepository;

    @Value("${bakery.leave.sickDays}")
    private int sickDays;
    @Value("${bakery.leave.paidDays}")
    private int paidDays;

    public boolean checkRemainingDays(Leave leave, LeaveType leaveType) {
        int getSickDay = (int) DAYS.between(leave.getLeaveStartDate(), leave.getLeaveEndDate()) + 1;
        List<Leave> leaves = getLeavesForCount(leave, leaveType);
        int remainingDays = chooseTypeOfDays(leaveType);
        for (Leave oneLeave : leaves) {
            if (!Objects.equals(oneLeave.getId(), leave.getId())) {
                remainingDays -= DAYS.between(oneLeave.getLeaveStartDate(), oneLeave.getLeaveEndDate()) + 1;
            }
        }
        remainingDays -= getSickDay;
        return remainingDays >= 0;
    }

    public int getRemainingDays(Leave leave, LeaveType leaveType) {
        List<Leave> leaves = getLeavesForCount(leave, leaveType);
        int remainingDays = chooseTypeOfDays(leaveType);
        return remainingDays - leaves.stream()
                .mapToInt((l) -> (int) DAYS.between(l.getLeaveStartDate(), l.getLeaveEndDate()) + 1)
                .sum();
    }

    private List<Leave> getLeavesForCount(Leave leave, LeaveType leaveType) {
        LocalDate calendarYear = getCalendarYear(leave.getEmployee().getId(), LocalDate.now());
        return leaveRepository
                .findByEmployeeIdAndLeaveTypeAndLeaveEndDateGreaterThanEqual(leave.getEmployee().getId(),
                        leaveType, calendarYear);
    }

    private LocalDate getCalendarYear(Long employeeId, LocalDate endDate) {
        Contract contract = contractRepository
                .findByEmployeeIdAndContractEndDateGreaterThan(employeeId, endDate)
                .orElseThrow(() -> new EntityNotFoundException("There is no such contract"));
        LocalDate contractStartDate = contract.getContractStartDate();
        int day = contractStartDate.getDayOfMonth();
        int month = contractStartDate.getMonthValue();
        int year = LocalDate.now().getYear();
        LocalDate calendarYear = LocalDate.of(year, month, day);
        return calendarYear.isAfter(LocalDate.now())
                ? LocalDate.of(year - 1, month, day)
                : calendarYear;
    }

    private int chooseTypeOfDays(LeaveType leaveType) {
        return leaveType == LeaveType.SICK_DAY ? sickDays : paidDays;
    }
}
