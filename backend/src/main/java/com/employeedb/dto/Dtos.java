package com.employeedb.dto;

import com.employeedb.model.Role;
import jakarta.validation.constraints.*;
import java.util.List;

/** All data-transfer objects in one file. Import with: import static com.employeedb.dto.Dtos.*; */
public final class Dtos {
  private Dtos() {}

  // ── Response records ────────────────────────────────────────────────────────
  public record AuthResponse(String token, String username, Role role, String bearerType) {}

  public record StatsResponse(
      long totalEmployees, long totalDepartments, double avgSalary,
      long addedThisMonth, String highestPaidName, double highestSalary) {}

  public record DeptCount(String department, long count) {}

  public record ImportResult(int imported, int skipped, List<String> errors) {}

  public record UserSummary(Long id, String username, String email, Role role) {}

  // ── Request classes (mutable for Jackson) ──────────────────────────────────
  public static class LoginRequest {
    @NotBlank private String username;
    @NotBlank @Size(max = 72) private String password;
    public String getUsername() { return username; }
    public void setUsername(String v) { username = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { password = v; }
  }

  public static class RegisterRequest {
    @NotBlank @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,50}$",
             message = "Username must be 3–50 characters: letters, digits, underscore only")
    private String username;
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 6, max = 72) private String password;
    public String getUsername() { return username; }
    public void setUsername(String v) { username = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { password = v; }
  }

  public static class ChangePasswordRequest {
    @NotBlank private String currentPassword;
    @NotBlank @Size(min = 6, max = 100, message = "New password must be 6–100 characters")
    private String newPassword;
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String v) { currentPassword = v; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String v) { newPassword = v; }
  }
}
