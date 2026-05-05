package com.employeedb.web;

import com.employeedb.model.Employee;
import com.employeedb.service.EmployeeService;
import com.employeedb.service.export.EmployeeExcelExportService;
import com.employeedb.service.export.EmployeePdfExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employees")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

  private final EmployeeService service;
  private final EmployeePdfExportService pdfExport;
  private final EmployeeExcelExportService excelExport;

  public EmployeeController(
      EmployeeService service,
      EmployeePdfExportService pdfExport,
      EmployeeExcelExportService excelExport) {
    this.service = service;
    this.pdfExport = pdfExport;
    this.excelExport = excelExport;
  }

  @GetMapping
  @Operation(summary = "List employees (search, filter, pagination)")
  public Page<Employee> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String department,
      @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return service.findPage(q, department, pageable);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get one employee")
  public Employee one(@PathVariable Long id) {
    return service.getById(id);
  }

  @PostMapping
  @Operation(summary = "Create employee")
  public ResponseEntity<Employee> create(@Valid @RequestBody Employee body) {
    Employee saved = service.create(body);
    return ResponseEntity.status(201).body(saved);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update employee")
  public Employee update(@PathVariable Long id, @Valid @RequestBody Employee body) {
    return service.update(id, body);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete employee (ADMIN only)")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/export/pdf")
  @Operation(summary = "Export filtered employees to PDF (ADMIN only)")
  public ResponseEntity<byte[]> exportPdf(
      @RequestParam(required = false) String q, @RequestParam(required = false) String department) {
    List<Employee> rows = service.findAllForExport(q, department);
    byte[] bytes = pdfExport.build(rows);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.pdf")
        .contentType(MediaType.APPLICATION_PDF)
        .body(bytes);
  }

  @GetMapping("/export/excel")
  @Operation(summary = "Export filtered employees to Excel (ADMIN only)")
  public ResponseEntity<byte[]> exportExcel(
      @RequestParam(required = false) String q, @RequestParam(required = false) String department)
      throws IOException {
    List<Employee> rows = service.findAllForExport(q, department);
    byte[] bytes = excelExport.build(rows);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.xlsx")
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(bytes);
  }
}
