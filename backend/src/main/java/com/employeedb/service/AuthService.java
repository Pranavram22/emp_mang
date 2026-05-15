package com.employeedb.service;

import static com.employeedb.dto.Dtos.*;
import com.employeedb.model.AppUser;
import com.employeedb.model.Role;
import com.employeedb.repo.AppUserRepository;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class AuthService {

  private final AppUserRepository repo;
  private final PasswordEncoder encoder;
  private final AuthenticationManager authManager;

  public AuthService(
      AppUserRepository repo, PasswordEncoder encoder, AuthenticationManager authManager) {
    this.repo = repo;
    this.encoder = encoder;
    this.authManager = authManager;
  }

  @Transactional
  public AppUser register(@Valid RegisterRequest req) {
    String username = normalizeUsername(req.getUsername());
    String email = normalizeEmail(req.getEmail());
    if (repo.existsByUsername(username)) {
      throw new IllegalArgumentException("Username already exists");
    }
    if (repo.existsByEmailIgnoreCase(email)) {
      throw new IllegalArgumentException("Email already registered");
    }
    AppUser u = new AppUser();
    u.setUsername(username);
    u.setEmail(email);
    u.setPasswordHash(encoder.encode(req.getPassword()));
    u.setRole(Role.USER);
    return repo.save(u);
  }

  public AppUser authenticate(@Valid LoginRequest req) {
    String identifier = normalizeIdentifier(req.getUsername());
    authManager.authenticate(
        new UsernamePasswordAuthenticationToken(identifier, req.getPassword()));
    return findByUsernameOrEmail(identifier);
  }

  @Transactional
  public void changePassword(String username, @Valid ChangePasswordRequest req) {
    AppUser u = repo.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (!encoder.matches(req.getCurrentPassword(), u.getPasswordHash())) {
      throw new BadCredentialsException("Current password is incorrect");
    }
    u.setPasswordHash(encoder.encode(req.getNewPassword()));
    repo.save(u);
  }

  private AppUser findByUsernameOrEmail(String identifier) {
    return repo.findByUsername(identifier)
        .or(() -> repo.findByEmailIgnoreCase(identifier))
        .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
  }

  private static String normalizeUsername(String username) {
    return username != null ? username.trim() : null;
  }

  private static String normalizeEmail(String email) {
    return email != null ? email.trim().toLowerCase() : null;
  }

  private static String normalizeIdentifier(String identifier) {
    return identifier != null ? identifier.trim() : null;
  }
}
