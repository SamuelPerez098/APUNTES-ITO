package com.englishlab.repository;

import com.englishlab.model.entity.Role;
import com.englishlab.model.entity.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
