package com.employeedb.config;

import com.employeedb.model.AppUser;
import com.employeedb.model.Employee;
import com.employeedb.model.Role;
import com.employeedb.repo.AppUserRepository;
import com.employeedb.repo.EmployeeRepository;
import java.math.BigDecimal;
import java.util.List;
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
      List.of(
        emp("jdoe",     "jdoe@example.com",     "Jane",    "Doe",       28, "9876543210", "Engineering",  "95000"),
        emp("asmith",   "asmith@example.com",   "Alice",   "Smith",     34, "9823456710", "Engineering",  "112000"),
        emp("bjones",   "bjones@example.com",   "Bob",     "Jones",     41, "9812345670", "Management",   "145000"),
        emp("clee",     "clee@example.com",      "Carol",   "Lee",       29, "9898765432", "Design",       "88000"),
        emp("dkumar",   "dkumar@example.com",   "David",   "Kumar",     36, "9867543210", "Engineering",  "105000"),
        emp("emartin",  "emartin@example.com",  "Eva",     "Martin",    25, "9856432109", "HR",           "72000"),
        emp("fwilson",  "fwilson@example.com",  "Frank",   "Wilson",    45, "9845321098", "Finance",      "130000"),
        emp("gharris",  "gharris@example.com",  "Grace",   "Harris",    31, "9834210987", "Design",       "91000"),
        emp("htaylor",  "htaylor@example.com",  "Henry",   "Taylor",    38, "9823109876", "Finance",      "118000"),
        emp("iclark",   "iclark@example.com",   "Iris",    "Clark",     27, "9812098765", "HR",           "69000"),
        emp("jwhite",   "jwhite@example.com",   "James",   "White",     33, "9801987654", "Engineering",  "99000"),
        emp("kbrown",   "kbrown@example.com",   "Karen",   "Brown",     40, "9790876543", "Management",   "138000"),
        emp("lmiller",  "lmiller@example.com",  "Leo",     "Miller",    24, "9780765432", "Design",       "76000"),
        emp("mwang",    "mwang@example.com",    "Mia",     "Wang",      30, "9770654321", "Engineering",  "102000"),
        emp("nthomas",  "nthomas@example.com",  "Noah",    "Thomas",    47, "9760543210", "Finance",      "155000"),
        emp("ojackson", "ojackson@example.com", "Olivia",  "Jackson",   26, "9750432109", "HR",           "65000"),
        emp("pgarcia",  "pgarcia@example.com",  "Paul",    "Garcia",    35, "9740321098", "Engineering",  "108000"),
        emp("qmartinez","qmartinez@example.com","Quinn",   "Martinez",  29, "9730210987", "Design",       "84000"),
        emp("rrobinson","rrobinson@example.com","Rachel",  "Robinson",  43, "9720109876", "Management",   "142000"),
        emp("slopez",   "slopez@example.com",   "Sam",     "Lopez",     32, "9710098765", "Finance",      "121000")
      ).forEach(employees::save);
    }
  }

  private static Employee emp(String username, String email, String first, String last,
      int age, String mobile, String dept, String salary) {
    Employee e = new Employee();
    e.setUsername(username);
    e.setEmail(email);
    e.setFirstName(first);
    e.setLastName(last);
    e.setAge(age);
    e.setMobile(mobile);
    e.setDepartment(dept);
    e.setSalary(new BigDecimal(salary));
    e.setCreatedBy("admin");
    return e;
  }
}
