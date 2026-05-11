package com.employeedb.web;

import static com.employeedb.dto.Dtos.*;
import com.employeedb.model.Employee;
import com.employeedb.service.EmployeeExcelImportService;
import com.employeedb.service.EmployeeService;
import com.employeedb.service.export.EmployeeExcelExportService;
import com.employeedb.service.export.EmployeePdfExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
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
  private final EmployeeExcelImportService excelImport;

  public EmployeeController(
      EmployeeService service,
      EmployeePdfExportService pdfExport,
      EmployeeExcelExportService excelExport,
      EmployeeExcelImportService excelImport) {
    this.service = service;
    this.pdfExport = pdfExport;
    this.excelExport = excelExport;
    this.excelImport = excelImport;
  }

  @GetMapping
  @Operation(summary = "List employees (search, filter, pagination)")
  public Page<Employee> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String department,
      @RequestParam(required = false) BigDecimal minSalary,
      @RequestParam(required = false) BigDecimal maxSalary,
      @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return service.findPage(q, department, minSalary, maxSalary, pageable);
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
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String department,
      @RequestParam(required = false) BigDecimal minSalary,
      @RequestParam(required = false) BigDecimal maxSalary) {
    List<Employee> rows = service.findAllForExport(q, department, minSalary, maxSalary);
    byte[] bytes = pdfExport.build(rows);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.pdf")
        .contentType(MediaType.APPLICATION_PDF)
        .body(bytes);
  }

  @GetMapping("/import/template")
  @Operation(summary = "Download blank import template (ADMIN only)")
  public ResponseEntity<byte[]> importTemplate() throws IOException {
    try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sh = wb.createSheet("Employees");
      Row header = sh.createRow(0);
      String[] cols = {"Username", "Email", "First name", "Last name", "Age", "Mobile", "Department", "Salary"};
      for (int i = 0; i < cols.length; i++) {
        header.createCell(i).setCellValue(cols[i]);
      }
      for (int i = 0; i < cols.length; i++) sh.setColumnWidth(i, 5000);
      wb.write(out);
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee_import_template.xlsx")
          .contentType(MediaType.parseMediaType(
              "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
          .body(out.toByteArray());
    }
  }

  @PostMapping("/import/excel")
  @Operation(summary = "Bulk import employees from Excel (ADMIN only)")
  public ResponseEntity<ImportResult> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("Uploaded file is empty");
    }
    ImportResult result = excelImport.importFile(file.getInputStream());
    return ResponseEntity.ok(result);
  }

  @GetMapping("/export/excel")
  @Operation(summary = "Export filtered employees to Excel (ADMIN only)")
  public ResponseEntity<byte[]> exportExcel(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String department,
      @RequestParam(required = false) BigDecimal minSalary,
      @RequestParam(required = false) BigDecimal maxSalary)
      throws IOException {
    List<Employee> rows = service.findAllForExport(q, department, minSalary, maxSalary);
    byte[] bytes = excelExport.build(rows);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.xlsx")
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(bytes);
  }
}
