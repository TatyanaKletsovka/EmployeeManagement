package com.syberry.bakery.repository;

import com.syberry.bakery.entity.User;
import com.syberry.bakery.dto.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIdAndIsBlockedFalse(Long id);
    Optional<User> findByEmailAndIsBlockedFalse(String email);
    Optional<User> findByEmail(String email);
    Page<User> findAllByIsBlockedFalse(Pageable pageable);
    @Query(value = "select distinct u from User u inner join u.roles r where u.firstName like CONCAT('%',:firstName,'%') and u.lastName like CONCAT('%',:lastName,'%') and u.email like CONCAT('%',:email,'%') and r.roleName in :roles and u.isBlocked = false")
    Page<User> findAllByFiltering(String firstName, String lastName, String email, List<RoleName> roles, Pageable pageable);
    @Query(value = "SELECT COUNT(u.id) FROM User u inner join u.roles r where r.roleName in :roles and u.isBlocked = false")
    long countAllByBlockedIsFalseAndRoleIn(List<RoleName> roles);
}

