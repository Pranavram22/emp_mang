package com.employeedb.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
public class Employee {

  /** Login-style username for the employee record: 3–50 chars, alphanumeric + underscore. */
  public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]{3,50}$";

  /** Exactly 10 digits (common mobile format in notes). */
  public static final String MOBILE_PATTERN = "^[0-9]{10}$";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(min = 3, max = 50)
  @Pattern(regexp = USERNAME_PATTERN, message = "Username must be 3–50 characters: letters, digits, underscore only")
  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @NotBlank
  @Email(message = "Invalid email format")
  @Column(nullable = false, length = 120)
  private String email;

  @NotNull(message = "Age is required")
  @Min(value = 18, message = "Age must be at least 18")
  @Max(value = 100, message = "Age must be at most 100")
  @Column(nullable = false)
  private Integer age;

  @NotBlank
  @Pattern(regexp = MOBILE_PATTERN, message = "Mobile must be exactly 10 digits")
  @Column(nullable = false, length = 10)
  private String mobile;

  @NotBlank
  @Size(max = 80)
  @Column(nullable = false, length = 80)
  private String firstName;

  @NotBlank
  @Size(max = 80)
  @Column(nullable = false, length = 80)
  private String lastName;

  @Size(max = 80)
  @Column(length = 80)
  private String department;

  @Column(precision = 12, scale = 2)
  private BigDecimal salary;

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public BigDecimal getSalary() {
    return salary;
  }

  public void setSalary(BigDecimal salary) {
    this.salary = salary;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
