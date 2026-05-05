package com.employeedb.service;

import com.employeedb.dto.LoginRequest;
import com.employeedb.dto.RegisterRequest;
import com.employeedb.model.AppUser;
import com.employeedb.model.Role;
import com.employeedb.repo.AppUserRepository;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
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
    if (repo.existsByUsername(req.getUsername())) {
      throw new IllegalArgumentException("Username already exists");
    }
    if (repo.existsByEmail(req.getEmail())) {
      throw new IllegalArgumentException("Email already registered");
    }
    AppUser u = new AppUser();
    u.setUsername(req.getUsername());
    u.setEmail(req.getEmail());
    u.setPasswordHash(encoder.encode(req.getPassword()));
    u.setRole(Role.USER);
    return repo.save(u);
  }

  public AppUser authenticate(@Valid LoginRequest req) {
    authManager.authenticate(
        new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
    return repo.findByUsername(req.getUsername()).orElseThrow();
  }
}
