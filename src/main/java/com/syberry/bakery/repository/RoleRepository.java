package com.syberry.bakery.repository;

import com.syberry.bakery.dto.RoleName;
import com.syberry.bakery.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(RoleName roleName);
}
