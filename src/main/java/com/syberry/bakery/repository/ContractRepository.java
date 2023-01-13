package com.syberry.bakery.repository;

import com.syberry.bakery.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    @Query("""
            select c from Contract c
            where lower (concat(c.employee.user.firstName, ' ', c.employee.user.lastName)) like lower(concat('%',:name,'%'))
            and c.employee.user.isBlocked = false""")
    Page<Contract> findByEmployeeIsBlockedFalseAndFilterIn(Pageable pageable, String name);

    Optional<Contract> findByIdAndEmployeeUserIsBlockedFalse(Long id);

    List<Contract> findByEmployeeIdAndEmployeeUserIsBlockedFalse(Long id);

    List<Contract> findByEmployeeUserEmailAndEmployeeUserIsBlockedFalse(String email);

    Optional<Contract> findByEmployeeIdAndContractEndDateGreaterThan(Long id, LocalDate contractEndDate);

    @Query("""
            select c from Contract c
            where c.employee.id = ?1
            and c.employee.user.isBlocked = false
            and (c.contractStartDate between ?2 and ?3 or c.contractEndDate between ?2 and ?3)
            or (c.contractStartDate <= ?2 and c.contractEndDate >= ?3)""")
    List<Contract> findByEmployeeIdAndContractStartDateOrContractEndDateBetween(Long id, LocalDate contractStartDate, LocalDate contractEndDate);
}
