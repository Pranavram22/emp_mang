package com.employeedb.service.export;

import com.employeedb.model.Employee;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class EmployeeExcelExportService {

  public byte[] build(List<Employee> rows) throws IOException {
    try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sh = wb.createSheet("Employees");
      Row header = sh.createRow(0);
      String[] cols = {
        "ID", "Username", "Email", "First name", "Last name", "Age", "Mobile", "Department", "Salary",
        "Created"
      };
      for (int i = 0; i < cols.length; i++) {
        header.createCell(i).setCellValue(cols[i]);
      }
      int r = 1;
      for (Employee e : rows) {
        Row row = sh.createRow(r++);
        int c = 0;
        row.createCell(c++).setCellValue(e.getId() != null ? e.getId() : 0);
        row.createCell(c++).setCellValue(e.getUsername());
        row.createCell(c++).setCellValue(e.getEmail());
        row.createCell(c++).setCellValue(e.getFirstName());
        row.createCell(c++).setCellValue(e.getLastName());
        row.createCell(c++).setCellValue(e.getAge() != null ? e.getAge() : 0);
        row.createCell(c++).setCellValue(e.getMobile());
        row.createCell(c++).setCellValue(e.getDepartment() != null ? e.getDepartment() : "");
        row.createCell(c++)
            .setCellValue(e.getSalary() != null ? e.getSalary().doubleValue() : 0d);
        row.createCell(c).setCellValue(e.getCreatedAt() != null ? e.getCreatedAt().toString() : "");
      }
      for (int i = 0; i < cols.length; i++) {
        sh.autoSizeColumn(i);
      }
      wb.write(out);
      return out.toByteArray();
    }
  }
}
