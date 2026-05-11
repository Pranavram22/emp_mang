package com.employeedb.service;

import com.employeedb.model.Employee;
import com.employeedb.repo.EmployeeRepository;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

  private final EmployeeRepository repo;

  public EmployeeService(EmployeeRepository repo) { this.repo = repo; }

  // ── Queries ────────────────────────────────────────────────────────────────

  public Page<Employee> findPage(String q, String dept, BigDecimal minSal, BigDecimal maxSal, Pageable pageable) {
    return repo.findAll(filter(q, dept, minSal, maxSal), pageable);
  }

  public List<Employee> findAllForExport(String q, String dept, BigDecimal minSal, BigDecimal maxSal) {
    return repo.findAll(filter(q, dept, minSal, maxSal));
  }

  public Employee getById(Long id) {
    return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Employee not found"));
  }

  // ── Mutations ──────────────────────────────────────────────────────────────

  @Transactional
  public Employee create(Employee e) {
    if (repo.existsByUsername(e.getUsername())) throw new IllegalArgumentException("Employee username already exists");
    e.setId(null);
    e.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
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
    if (!repo.existsById(id)) throw new IllegalArgumentException("Employee not found");
    repo.deleteById(id);
  }

  // ── Filter spec (inlined from EmployeeSpecs) ───────────────────────────────

  private static Specification<Employee> filter(String query, String department, BigDecimal minSalary, BigDecimal maxSalary) {
    return (root, cq, cb) -> {
      List<Predicate> p = new ArrayList<>();
      if (query != null && !query.isBlank()) {
        String like = "%" + query.toLowerCase().trim() + "%";
        p.add(cb.or(
            cb.like(cb.lower(root.get("username")), like),
            cb.like(cb.lower(root.get("email")), like),
            cb.like(cb.lower(root.get("firstName")), like),
            cb.like(cb.lower(root.get("lastName")), like)));
      }
      if (department != null && !department.isBlank())
        p.add(cb.equal(cb.lower(root.get("department")), department.toLowerCase().trim()));
      if (minSalary != null)
        p.add(cb.greaterThanOrEqualTo(root.get("salary"), minSalary));
      if (maxSalary != null)
        p.add(cb.lessThanOrEqualTo(root.get("salary"), maxSalary));
      return p.isEmpty() ? cb.conjunction() : cb.and(p.toArray(Predicate[]::new));
    };
  }
}
