package com.syberry.bakery.repository;

import com.syberry.bakery.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUserEmailIgnoreCaseAndUserIsBlockedFalse(String email);

    Optional<Employee> findByIdAndUserIsBlockedFalse(Long id);

    @Query("""
            select e from Employee e
            where e.user.isBlocked = false and lower(concat(e.user.firstName, ' ', e.user.lastName)) like lower(concat('%',:name,'%'))""")
    Page<Employee> findByUserIsBlockedFalseAndFilterIn(String name, Pageable pageable);

}
