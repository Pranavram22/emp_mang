package com.employeedb.service.spec;

import com.employeedb.model.Employee;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class EmployeeSpecs {

  private EmployeeSpecs() {}

  public static Specification<Employee> filter(String query, String department) {
    return (root, cq, cb) -> {
      List<Predicate> p = new ArrayList<>();
      if (query != null && !query.isBlank()) {
        String like = "%" + query.toLowerCase().trim() + "%";
        p.add(
            cb.or(
                cb.like(cb.lower(root.get("username")), like),
                cb.like(cb.lower(root.get("email")), like),
                cb.like(cb.lower(root.get("firstName")), like),
                cb.like(cb.lower(root.get("lastName")), like)));
      }
      if (department != null && !department.isBlank()) {
        p.add(cb.equal(cb.lower(root.get("department")), department.toLowerCase().trim()));
      }
      if (p.isEmpty()) {
        return cb.conjunction();
      }
      return cb.and(p.toArray(Predicate[]::new));
    };
  }
}
