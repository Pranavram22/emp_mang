package com.employeedb.dto;

import com.employeedb.model.Role;

public record AuthResponse(String token, String username, Role role, String bearerType) {}
