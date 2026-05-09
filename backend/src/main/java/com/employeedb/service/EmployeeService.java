package com.employeedb.service;

import com.employeedb.model.Employee;
import com.employeedb.repo.EmployeeRepository;
import com.employeedb.service.spec.EmployeeSpecs;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

  private final EmployeeRepository repo;

  public EmployeeService(EmployeeRepository repo) {
    this.repo = repo;
  }

  public Page<Employee> findPage(
      String q, String department, BigDecimal minSalary, BigDecimal maxSalary, Pageable pageable) {
    return repo.findAll(EmployeeSpecs.filter(q, department, minSalary, maxSalary), pageable);
  }

  public List<Employee> findAllForExport(
      String q, String department, BigDecimal minSalary, BigDecimal maxSalary) {
    return repo.findAll(EmployeeSpecs.filter(q, department, minSalary, maxSalary));
  }

  public Employee getById(Long id) {
    return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Employee not found"));
  }

  @Transactional
  public Employee create(Employee e) {
    if (repo.existsByUsername(e.getUsername())) {
      throw new IllegalArgumentException("Employee username already exists");
    }
    e.setId(null);
    return repo.save(e);
  }

  @Transactional
  public Employee update(Long id, Employee patch) {
    Employee existing = getById(id);
    if (!existing.getUsername().equals(patch.getUsername()) && repo.existsByUsername(patch.getUsername())) {
      throw new IllegalArgumentException("Employee username already exists");
    }
    existing.setUsername(patch.getUsername());
    existing.setEmail(patch.getEmail());
    existing.setAge(patch.getAge());
    existing.setMobile(patch.getMobile());
    existing.setFirstName(patch.getFirstName());
    existing.setLastName(patch.getLastName());
    existing.setDepartment(patch.getDepartment());
    existing.setSalary(patch.getSalary());
    return repo.save(existing);
  }

  @Transactional
  public void delete(Long id) {
    if (!repo.existsById(id)) {
      throw new IllegalArgumentException("Employee not found");
    }
    repo.deleteById(id);
  }
}
