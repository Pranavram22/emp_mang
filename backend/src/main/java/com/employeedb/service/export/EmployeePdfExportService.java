package com.employeedb.service.export;

import com.employeedb.model.Employee;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EmployeePdfExportService {

  public byte[] build(List<Employee> rows) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PdfWriter writer = new PdfWriter(baos);
    PdfDocument pdf = new PdfDocument(writer);
    Document doc = new Document(pdf);

    doc.add(new Paragraph("Employee export").setBold().setFontSize(16));

    Table table = new Table(UnitValue.createPercentArray(new float[] {5, 8, 10, 10, 5, 8, 8, 8, 8}));
    table.useAllAvailableWidth();

    table.addHeaderCell("ID");
    table.addHeaderCell("Username");
    table.addHeaderCell("Email");
    table.addHeaderCell("Name");
    table.addHeaderCell("Age");
    table.addHeaderCell("Mobile");
    table.addHeaderCell("Department");
    table.addHeaderCell("Salary");
    table.addHeaderCell("Created");

    for (Employee e : rows) {
      table.addCell(String.valueOf(e.getId()));
      table.addCell(safe(e.getUsername()));
      table.addCell(safe(e.getEmail()));
      table.addCell(safe(e.getFirstName()) + " " + safe(e.getLastName()));
      table.addCell(String.valueOf(e.getAge()));
      table.addCell(safe(e.getMobile()));
      table.addCell(safe(e.getDepartment()));
      table.addCell(e.getSalary() != null ? e.getSalary().toPlainString() : "");
      table.addCell(e.getCreatedAt() != null ? e.getCreatedAt().toString() : "");
    }

    doc.add(table);
    doc.close();
    return baos.toByteArray();
  }

  private static String safe(String s) {
    return s != null ? s : "";
  }
}
