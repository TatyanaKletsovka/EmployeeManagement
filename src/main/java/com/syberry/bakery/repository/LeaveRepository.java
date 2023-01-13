package com.syberry.bakery.repository;

import com.syberry.bakery.dto.LeaveType;
import com.syberry.bakery.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LeaveRepository extends JpaRepository<Leave, Long>, JpaSpecificationExecutor<Leave> {

    List<Leave> findByEmployeeIdAndEmployeeUserIsBlockedFalse(Long id);

    List<Leave> findByEmployeeUserEmailAndEmployeeUserIsBlockedFalse(String email);

    Optional<Leave> findByIdAndEmployeeUserIsBlockedFalse(Long id);

    List<Leave> findByEmployeeIdAndLeaveTypeAndLeaveEndDateGreaterThanEqual(Long id, LeaveType leaveType, LocalDate leaveStartDate);

    @Query("""
            select l from Leave l
            where l.employee.id = ?1
            and l.employee.user.isBlocked = false
            and (l.leaveStartDate between ?2 and ?3 or l.leaveEndDate between ?2 and ?3)
            or (l.leaveStartDate <= ?2 and l.leaveEndDate >= ?3)""")
    List<Leave> findByEmployeeIdAndLeaveStartDateOrLeaveEndDateBetween(Long id, LocalDate leaveStartDate, LocalDate leaveEndDate);

}
