package com.syberry.bakery.repository;

import com.syberry.bakery.entity.Compensation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompensationRepository extends JpaRepository<Compensation, Long>, JpaSpecificationExecutor<Compensation> {

    Optional<Compensation> findByIdAndEmployeeUserIsBlockedFalse(Long id);

    @Query("""
            select c from Compensation c
            where c.employee.id = :employeeId
            and (:effectiveFrom between c.effectiveFrom and c.validUntil
            or :validUntil between c.effectiveFrom and c.validUntil)""")
    List<Compensation> findByDatesCrossingInEmployee(Long employeeId, LocalDate effectiveFrom, LocalDate validUntil);

    @Query("""
            select c from Compensation c
            where c.employee.user.isBlocked = false
            and c.id < :id and c.employee.id = :employeeId""")
    List<Compensation> findByNotBlockedAndIdLessThanAndEmployeeId(Long id, Long employeeId);

    @Query("""
            select c from Compensation c
            where c.employee.user.isBlocked = false
            and c.id > :id and c.employee.id = :employeeId""")
    List<Compensation> findByNotBlockedAndIdGreaterThanAndEmployeeId(Long id, Long employeeId);
}
