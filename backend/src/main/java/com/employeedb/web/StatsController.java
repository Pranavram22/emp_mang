package com.employeedb.web;

import com.employeedb.dto.DeptCount;
import com.employeedb.dto.StatsResponse;
import com.employeedb.model.Employee;
import com.employeedb.repo.EmployeeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@Tag(name = "Stats")
@SecurityRequirement(name = "bearerAuth")
public class StatsController {

  private final EmployeeRepository repo;

  public StatsController(EmployeeRepository repo) {
    this.repo = repo;
  }

  @GetMapping
  @Operation(summary = "Dashboard stats")
  public StatsResponse stats() {
    List<Employee> all = repo.findAll();
    long total = all.size();

    long departments = all.stream()
        .map(Employee::getDepartment)
        .filter(d -> d != null && !d.isBlank())
        .distinct()
        .count();

    double avg = all.stream()
        .filter(e -> e.getSalary() != null)
        .mapToDouble(e -> e.getSalary().doubleValue())
        .average()
        .orElse(0);

    LocalDateTime firstOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
    long addedThisMonth = all.stream()
        .filter(e -> e.getCreatedAt() != null && e.getCreatedAt().isAfter(firstOfMonth))
        .count();

    Employee highest = all.stream()
        .filter(e -> e.getSalary() != null)
        .max(Comparator.comparing(Employee::getSalary))
        .orElse(null);

    String highestName = highest != null ? highest.getFirstName() + " " + highest.getLastName() : "—";
    double highestSalary = highest != null ? highest.getSalary().doubleValue() : 0;

    return new StatsResponse(total, departments, avg, addedThisMonth, highestName, highestSalary);
  }

  @GetMapping("/departments")
  @Operation(summary = "List of distinct departments")
  public List<String> departments() {
    return repo.findAll(Sort.by("department")).stream()
        .map(Employee::getDepartment)
        .filter(d -> d != null && !d.isBlank())
        .distinct()
        .sorted()
        .toList();
  }

  @GetMapping("/dept-breakdown")
  @Operation(summary = "Employee count per department")
  public List<DeptCount> deptBreakdown() {
    return repo.findAll().stream()
        .filter(e -> e.getDepartment() != null && !e.getDepartment().isBlank())
        .collect(java.util.stream.Collectors.groupingBy(
            Employee::getDepartment, java.util.stream.Collectors.counting()))
        .entrySet().stream()
        .map(en -> new DeptCount(en.getKey(), en.getValue()))
        .sorted(java.util.Comparator.comparingLong(DeptCount::count).reversed())
        .toList();
  }
}
