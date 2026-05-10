package com.employeedb.web;

import com.employeedb.dto.AuthResponse;
import com.employeedb.dto.ChangePasswordRequest;
import com.employeedb.dto.LoginRequest;
import com.employeedb.dto.RegisterRequest;
import com.employeedb.model.AppUser;
import com.employeedb.security.JwtService;
import com.employeedb.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {

  private final AuthService authService;
  private final JwtService jwtService;

  public AuthController(AuthService authService, JwtService jwtService) {
    this.authService = authService;
    this.jwtService = jwtService;
  }

  @PostMapping("/register")
  @Operation(summary = "Sign up (role USER)")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest body) {
    AppUser u = authService.register(body);
    String token = jwtService.generateToken(u);
    return ResponseEntity.ok(new AuthResponse(token, u.getUsername(), u.getRole(), "Bearer"));
  }

  @PostMapping("/login")
  @Operation(summary = "Log in")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest body) {
    AppUser u = authService.authenticate(body);
    String token = jwtService.generateToken(u);
    return ResponseEntity.ok(new AuthResponse(token, u.getUsername(), u.getRole(), "Bearer"));
  }

  @PostMapping("/change-password")
  @Operation(summary = "Change own password")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<Void> changePassword(
      @Valid @RequestBody ChangePasswordRequest body, Principal principal) {
    authService.changePassword(principal.getName(), body);
    return ResponseEntity.noContent().build();
  }
}
