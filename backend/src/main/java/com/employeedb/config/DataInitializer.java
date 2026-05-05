package com.employeedb.config;

import com.employeedb.model.AppUser;
import com.employeedb.model.Employee;
import com.employeedb.model.Role;
import com.employeedb.repo.AppUserRepository;
import com.employeedb.repo.EmployeeRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

  private final AppUserRepository users;
  private final EmployeeRepository employees;
  private final PasswordEncoder encoder;

  public DataInitializer(
      AppUserRepository users, EmployeeRepository employees, PasswordEncoder encoder) {
    this.users = users;
    this.employees = employees;
    this.encoder = encoder;
  }

  @Override
  public void run(String... args) {
    if (users.count() == 0) {
      AppUser admin = new AppUser();
      admin.setUsername("admin");
      admin.setEmail("admin@example.com");
      admin.setPasswordHash(encoder.encode("admin123"));
      admin.setRole(Role.ADMIN);
      users.save(admin);

      AppUser user = new AppUser();
      user.setUsername("user1");
      user.setEmail("user1@example.com");
      user.setPasswordHash(encoder.encode("user123"));
      user.setRole(Role.USER);
      users.save(user);
    }

    if (employees.count() == 0) {
      Employee e = new Employee();
      e.setUsername("jdoe");
      e.setEmail("jdoe@example.com");
      e.setAge(28);
      e.setMobile("9876543210");
      e.setFirstName("Jane");
      e.setLastName("Doe");
      e.setDepartment("Engineering");
      e.setSalary(new BigDecimal("95000.00"));
      employees.save(e);
    }
  }
}
