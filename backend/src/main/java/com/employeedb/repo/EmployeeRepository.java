package com.employeedb.repo;

import com.employeedb.model.Employee;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
  Optional<Employee> findByUsername(String username);

  boolean existsByUsername(String username);
  boolean existsByEmail(String email);
}
