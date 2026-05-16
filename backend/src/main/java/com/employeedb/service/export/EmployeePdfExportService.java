package com.employeedb.service.export;

import com.employeedb.model.Employee;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class EmployeePdfExportService {

  private static final DeviceRgb HEADER_BG  = new DeviceRgb(26,  26,  46);
  private static final DeviceRgb ALT_ROW_BG = new DeviceRgb(245, 245, 250);
  private static final DeviceRgb BORDER_CLR = new DeviceRgb(200, 200, 210);
  private static final DeviceRgb MUTED      = new DeviceRgb(120, 120, 130);

  public byte[] build(List<Employee> rows) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PdfWriter   writer = new PdfWriter(baos);
    PdfDocument pdf    = new PdfDocument(writer);
    Document    doc    = new Document(pdf);
    doc.setMargins(50, 40, 60, 40);

    PdfFont bold    = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
    PdfFont regular = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

    // ── Page-number footer ────────────────────────────────────────────────────
    pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new PageFooter(regular));

    // ── Title block ───────────────────────────────────────────────────────────
    doc.add(new Paragraph("Employee Database")
        .setFont(bold).setFontSize(20)
        .setFontColor(new DeviceRgb(26, 26, 46))
        .setMarginBottom(2));

    String exported = "Exported on " +
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")) +
        "   |   Total records: " + rows.size();
    doc.add(new Paragraph(exported)
        .setFont(regular).setFontSize(9)
        .setFontColor(MUTED)
        .setMarginBottom(14));

    // ── Table ─────────────────────────────────────────────────────────────────
    float[] colWidths = {4f, 9f, 10f, 16f, 4f, 9f, 10f, 8f};
    Table table = new Table(UnitValue.createPercentArray(colWidths)).useAllAvailableWidth();
    table.setMarginBottom(12);

    String[] headers = {"#", "Username", "Email", "Full Name", "Age", "Mobile", "Department", "Salary"};
    for (String h : headers) {
      table.addHeaderCell(
          new Cell().add(new Paragraph(h).setFont(bold).setFontSize(9).setFontColor(new DeviceRgb(255, 255, 255)))
              .setBackgroundColor(HEADER_BG)
              .setBorder(new SolidBorder(new DeviceRgb(26, 26, 46), 0))
              .setPadding(6));
    }

    NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    int rowIdx = 0;
    for (Employee e : rows) {
      boolean alt = (rowIdx++ % 2 == 1);
      DeviceRgb rowBg = alt ? ALT_ROW_BG : new DeviceRgb(255, 255, 255);

      String salary = e.getSalary() != null ? "$" + nf.format(e.getSalary().longValue()) : "—";
      String[] values = {
          String.valueOf(e.getId()),
          safe(e.getUsername()),
          safe(e.getEmail()),
          safe(e.getFirstName()) + " " + safe(e.getLastName()),
          String.valueOf(e.getAge()),
          safe(e.getMobile()),
          e.getDepartment() != null ? e.getDepartment() : "—",
          salary
      };
      for (String v : values) {
        table.addCell(
            new Cell().add(new Paragraph(v).setFont(regular).setFontSize(8.5f))
                .setBackgroundColor(rowBg)
                .setBorder(new SolidBorder(BORDER_CLR, 0.5f))
                .setPaddingTop(5).setPaddingBottom(5).setPaddingLeft(6).setPaddingRight(6));
      }
    }
    doc.add(table);

    // ── Summary line ──────────────────────────────────────────────────────────
    long depts = rows.stream()
        .map(Employee::getDepartment).filter(d -> d != null && !d.isBlank()).distinct().count();
    double avg = rows.stream()
        .filter(e -> e.getSalary() != null)
        .mapToDouble(e -> e.getSalary().doubleValue()).average().orElse(0);

    String summary = String.format(
        "Departments: %d   |   Avg Salary: $%s   |   Total Employees: %d",
        depts, nf.format((long) avg), rows.size());
    doc.add(new Paragraph(summary)
        .setFont(regular).setFontSize(8.5f)
        .setFontColor(MUTED)
        .setBorderTop(new SolidBorder(BORDER_CLR, 0.5f))
        .setPaddingTop(8));

    doc.close();
    return baos.toByteArray();
  }

  private static String safe(String s) { return s != null ? s : ""; }

  // ── Footer handler ────────────────────────────────────────────────────────
  private static class PageFooter implements IEventHandler {
    private final PdfFont font;
    PageFooter(PdfFont font) { this.font = font; }

    @Override
    public void handleEvent(Event event) {
      PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
      PdfDocument pdfDoc = docEvent.getDocument();
      PdfPage page = docEvent.getPage();
      int pageNum = pdfDoc.getPageNumber(page);
      int total   = pdfDoc.getNumberOfPages();
      Rectangle pageSize = page.getPageSize();

      PdfCanvas canvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), pdfDoc);
      try (Canvas c = new Canvas(canvas, pageSize)) {
        c.add(new Paragraph("Employee Database  |  Page " + pageNum + " of " + total)
            .setFont(font).setFontSize(7.5f)
            .setFontColor(new DeviceRgb(160, 160, 170))
            .setTextAlignment(TextAlignment.CENTER)
            .setFixedPosition(pageSize.getLeft() + 40, pageSize.getBottom() + 20,
                pageSize.getWidth() - 80));
      }
    }
  }
}
