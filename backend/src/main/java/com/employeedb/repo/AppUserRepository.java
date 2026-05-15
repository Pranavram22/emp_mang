package com.employeedb.repo;

import com.employeedb.model.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
  Optional<AppUser> findByUsername(String username);

  Optional<AppUser> findByEmailIgnoreCase(String email);

  boolean existsByUsername(String username);

  boolean existsByEmailIgnoreCase(String email);
}
