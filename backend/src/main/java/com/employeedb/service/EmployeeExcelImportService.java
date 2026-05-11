package com.employeedb.service;

import static com.employeedb.dto.Dtos.*;
import com.employeedb.model.Employee;
import com.employeedb.repo.EmployeeRepository;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeExcelImportService {

  private final EmployeeRepository repo;

  public EmployeeExcelImportService(EmployeeRepository repo) {
    this.repo = repo;
  }

  @Transactional
  public ImportResult importFile(InputStream in) throws Exception {
    int imported = 0;
    int skipped = 0;
    List<String> errors = new ArrayList<>();

    String actor = SecurityContextHolder.getContext().getAuthentication().getName();

    try (Workbook wb = new XSSFWorkbook(in)) {
      Sheet sheet = wb.getSheetAt(0);
      int lastRow = sheet.getLastRowNum();

      for (int i = 1; i <= lastRow; i++) {
        Row row = sheet.getRow(i);
        if (row == null || isRowEmpty(row)) continue;

        try {
          String username  = str(row, 0);
          String email     = str(row, 1);
          String firstName = str(row, 2);
          String lastName  = str(row, 3);
          String ageStr    = str(row, 4);
          String mobile    = str(row, 5);
          String dept      = str(row, 6);
          String salaryStr = str(row, 7);

          if (username.isBlank() || email.isBlank() || firstName.isBlank()
              || lastName.isBlank() || ageStr.isBlank() || mobile.isBlank()) {
            errors.add("Row " + (i + 1) + ": Username, Email, First name, Last name, Age and Mobile are required");
            skipped++;
            continue;
          }

          if (!username.matches("[A-Za-z0-9_]{3,50}")) {
            errors.add("Row " + (i + 1) + ": Username '" + username + "' must be 3–50 chars (letters, digits, underscore)");
            skipped++;
            continue;
          }

          if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            errors.add("Row " + (i + 1) + ": Email '" + email + "' is not valid");
            skipped++;
            continue;
          }

          int age;
          try {
            age = Integer.parseInt(ageStr.replace(".0", "").trim());
          } catch (NumberFormatException e) {
            errors.add("Row " + (i + 1) + ": Age '" + ageStr + "' is not a number");
            skipped++;
            continue;
          }
          if (age < 18 || age > 100) {
            errors.add("Row " + (i + 1) + ": Age must be 18–100 (got " + age + ")");
            skipped++;
            continue;
          }

          if (!mobile.matches("\\d{10}")) {
            errors.add("Row " + (i + 1) + ": Mobile '" + mobile + "' must be exactly 10 digits");
            skipped++;
            continue;
          }

          if (repo.existsByUsername(username)) {
            errors.add("Row " + (i + 1) + ": Username '" + username + "' already exists — skipped");
            skipped++;
            continue;
          }

          BigDecimal salary = null;
          if (!salaryStr.isBlank()) {
            try {
                salary = new BigDecimal(salaryStr.replace(",", "").trim());
            } catch (NumberFormatException ignored) {}
          }

          Employee e = new Employee();
          e.setUsername(username);
          e.setEmail(email);
          e.setFirstName(firstName);
          e.setLastName(lastName);
          e.setAge(age);
          e.setMobile(mobile);
          e.setDepartment(dept.isBlank() ? null : dept);
          e.setSalary(salary);
          e.setCreatedBy(actor);
          repo.save(e);
          imported++;

        } catch (Exception ex) {
          errors.add("Row " + (i + 1) + ": Unexpected error — " + ex.getMessage());
          skipped++;
        }
      }
    }

    return new ImportResult(imported, skipped, errors);
  }

  private String str(Row row, int col) {
    Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    if (cell == null) return "";
    if (cell.getCellType() == CellType.NUMERIC) {
      double v = cell.getNumericCellValue();
      // Return as integer string if it is a whole number (avoids "25.0" for age)
      if (v == Math.floor(v) && !Double.isInfinite(v)) {
        return String.valueOf((long) v);
      }
      return String.valueOf(v);
    }
    return cell.toString().trim();
  }

  private boolean isRowEmpty(Row row) {
    for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
      Cell cell = row.getCell(c);
      if (cell != null && cell.getCellType() != CellType.BLANK && !cell.toString().isBlank()) {
        return false;
      }
    }
    return true;
  }
}
