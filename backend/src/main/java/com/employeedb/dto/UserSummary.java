package com.employeedb.dto;

import com.employeedb.model.Role;

public record UserSummary(Long id, String username, String email, Role role) {}
