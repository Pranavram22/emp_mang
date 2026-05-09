package com.employeedb.dto;

public record StatsResponse(
    long totalEmployees,
    long totalDepartments,
    double avgSalary,
    long addedThisMonth,
    String highestPaidName,
    double highestSalary
) {}
