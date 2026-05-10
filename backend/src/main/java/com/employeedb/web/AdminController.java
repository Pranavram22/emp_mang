package com.employeedb.web;

import com.employeedb.dto.UserSummary;
import com.employeedb.model.AppUser;
import com.employeedb.model.Role;
import com.employeedb.repo.AppUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

  private final AppUserRepository repo;

  public AdminController(AppUserRepository repo) {
    this.repo = repo;
  }

  @GetMapping("/users")
  @Operation(summary = "List all app users")
  public List<UserSummary> listUsers() {
    return repo.findAll().stream()
        .map(u -> new UserSummary(u.getId(), u.getUsername(), u.getEmail(), u.getRole()))
        .toList();
  }

  @PutMapping("/users/{id}/role")
  @Operation(summary = "Change a user's role")
  public UserSummary changeRole(
      @PathVariable Long id,
      @RequestBody Map<String, String> body,
      Principal principal) {
    AppUser u = repo.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (u.getUsername().equals(principal.getName())) {
      throw new IllegalArgumentException("You cannot change your own role");
    }
    String roleStr = body.getOrDefault("role", "").toUpperCase();
    try {
      u.setRole(Role.valueOf(roleStr));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid role: " + roleStr + ". Use ADMIN or USER");
    }
    repo.save(u);
    return new UserSummary(u.getId(), u.getUsername(), u.getEmail(), u.getRole());
  }

  @DeleteMapping("/users/{id}")
  @Operation(summary = "Delete a user account")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id, Principal principal) {
    AppUser u = repo.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (u.getUsername().equals(principal.getName())) {
      throw new IllegalArgumentException("You cannot delete your own account");
    }
    repo.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
