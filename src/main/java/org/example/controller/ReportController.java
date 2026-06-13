package org.example.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.example.Repository.Impl.ReportImpl;
import org.example.Repository.ReportRepository;
import org.example.model.CustomerOrder;
import org.example.model.Product;

import java.io.FileOutputStream;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class ReportController {

    private final ReportRepository reportRepo;

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE      = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD,   new BaseColor(34, 139, 34));
    private static final Font FONT_SUBTITLE   = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD,   new BaseColor(60, 60, 60));
    private static final Font FONT_SECTION    = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,   new BaseColor(255, 255, 255));
    private static final Font FONT_BODY       = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(40, 40, 40));
    private static final Font FONT_BODY_BOLD  = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   new BaseColor(40, 40, 40));
    private static final Font FONT_ALERT      = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   new BaseColor(180, 0, 0));

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final BaseColor COLOR_HEADER     = new BaseColor(34, 139, 34);   // green
    private static final BaseColor COLOR_ROW_ALT    = new BaseColor(240, 248, 240); // light green
    private static final BaseColor COLOR_ROW_WHITE  = BaseColor.WHITE;
    private static final BaseColor COLOR_ALERT_ROW  = new BaseColor(255, 230, 230); // light red

    public ReportController() {
        this.reportRepo = new ReportImpl();
    }

    // ── Monthly Delivered Orders ──────────────────────────────────────────────

    public List<CustomerOrder> getMonthlyOrders(int month, int year) {
        if (!isValidMonthYear(month, year)) return List.of();
        return reportRepo.getMonthlyDeliveredOrders(month, year);
    }

    // ── Monthly Revenue ───────────────────────────────────────────────────────

    public double getMonthlyRevenue(int month, int year) {
        if (!isValidMonthYear(month, year)) return 0.0;
        return reportRepo.getMonthlyRevenue(month, year);
    }

    // ── Low Stock ─────────────────────────────────────────────────────────────

    public List<Product> getLowStockProducts() {
        return reportRepo.getLowStockProducts();
    }

    // ── Top Selling ───────────────────────────────────────────────────────────

    public List<CustomerOrder> getTopSellingProducts(int month, int year, int topN) {
        if (!isValidMonthYear(month, year)) return List.of();
        if (topN <= 0) {
            System.err.println("[ReportController] topN must be greater than 0.");
            return List.of();
        }
        return reportRepo.getTopSellingProducts(month, year, topN);
    }

    // ── PDF Generation ────────────────────────────────────────────────────────

    /**
     * Generates a full monthly PDF report including:
     * - Revenue summary
     * - Delivered orders table
     * - Top selling products
     * - Low stock alerts
     *
     * @param month    1-12
     * @param year     e.g. 2026
     * @param filePath full path to save PDF e.g. "C:/reports/june_2026.pdf"
     * @return true if PDF generated successfully
     */
    public boolean generatePDFReport(int month, int year, String filePath) {
        if (!isValidMonthYear(month, year)) return false;

        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        // fetch all data
        List<CustomerOrder> orders    = getMonthlyOrders(month, year);
        double revenue                = getMonthlyRevenue(month, year);
        List<CustomerOrder> topSellers = getTopSellingProducts(month, year, 5);
        List<Product> lowStock        = getLowStockProducts();

        try {
            Document document = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // ── Title ─────────────────────────────────────────────────────────
            Paragraph title = new Paragraph("GreenLoop - Monthly Report", FONT_TITLE);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph(monthName + " " + year, FONT_SUBTITLE);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // ── Revenue Summary ───────────────────────────────────────────────
            addSectionHeader(document, "Revenue Summary");

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(50);
            summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            summaryTable.setSpacingAfter(15);

            addSummaryRow(summaryTable, "Total Delivered Orders", String.valueOf(orders.size()));
            addSummaryRow(summaryTable, "Total Revenue", String.format("Rs. %.2f", revenue));
            document.add(summaryTable);

            // ── Delivered Orders Table ────────────────────────────────────────
            addSectionHeader(document, "Delivered Orders");

            if (orders.isEmpty()) {
                document.add(new Paragraph("No delivered orders for this period.", FONT_BODY));
            } else {
                PdfPTable ordersTable = new PdfPTable(5);
                ordersTable.setWidthPercentage(100);
                ordersTable.setWidths(new float[]{3f, 2f, 2f, 2f, 2f});
                ordersTable.setSpacingAfter(15);

                // headers
                addTableHeader(ordersTable, "Product");
                addTableHeader(ordersTable, "Customer");
                addTableHeader(ordersTable, "Qty");
                addTableHeader(ordersTable, "Unit Price");
                addTableHeader(ordersTable, "Total");

                // rows
                boolean alt = false;
                for (CustomerOrder o : orders) {
                    BaseColor rowColor = alt ? COLOR_ROW_ALT : COLOR_ROW_WHITE;
                    addTableRow(ordersTable, rowColor,
                            o.getProductName(),
                            o.getCustomerName(),
                            String.valueOf(o.getQuantityOrdered()),
                            String.format("Rs. %.2f", o.getUnitPrice()),
                            String.format("Rs. %.2f", o.getTotalAmount())
                    );
                    alt = !alt;
                }
                document.add(ordersTable);
            }

            // ── Top Selling Products ──────────────────────────────────────────
            addSectionHeader(document, "Top 5 Selling Products");

            if (topSellers.isEmpty()) {
                document.add(new Paragraph("No sales data available.", FONT_BODY));
            } else {
                PdfPTable topTable = new PdfPTable(4);
                topTable.setWidthPercentage(100);
                topTable.setWidths(new float[]{1f, 3f, 2f, 2f});
                topTable.setSpacingAfter(15);

                addTableHeader(topTable, "Rank");
                addTableHeader(topTable, "Product");
                addTableHeader(topTable, "Total Qty");
                addTableHeader(topTable, "Revenue");

                boolean alt = false;
                int rank = 1;
                for (CustomerOrder o : topSellers) {
                    BaseColor rowColor = alt ? COLOR_ROW_ALT : COLOR_ROW_WHITE;
                    addTableRow(topTable, rowColor,
                            String.valueOf(rank++),
                            o.getProductName(),
                            String.valueOf(o.getQuantityOrdered()),
                            String.format("Rs. %.2f", o.getTotalAmount())
                    );
                    alt = !alt;
                }
                document.add(topTable);
            }

            // ── Low Stock Alerts ──────────────────────────────────────────────
            addSectionHeader(document, "Low Stock Alerts");

            if (lowStock.isEmpty()) {
                Paragraph allGood = new Paragraph("All products are sufficiently stocked.", FONT_BODY);
                allGood.setSpacingAfter(15);
                document.add(allGood);
            } else {
                PdfPTable stockTable = new PdfPTable(4);
                stockTable.setWidthPercentage(100);
                stockTable.setWidths(new float[]{3f, 2f, 2f, 2f});
                stockTable.setSpacingAfter(15);

                addTableHeader(stockTable, "Product");
                addTableHeader(stockTable, "Category");
                addTableHeader(stockTable, "Stock");
                addTableHeader(stockTable, "Reorder Level");

                for (Product p : lowStock) {
                    // highlight out of stock in red
                    BaseColor rowColor = p.getStockQuantity() == 0
                            ? COLOR_ALERT_ROW : COLOR_ROW_WHITE;
                    addTableRow(stockTable, rowColor,
                            p.getName(),
                            p.getCategory(),
                            String.valueOf(p.getStockQuantity()),
                            String.valueOf(p.getReorderLevel())
                    );
                }
                document.add(stockTable);
            }

            // ── Footer ────────────────────────────────────────────────────────
            Paragraph footer = new Paragraph(
                    "Generated by GreenLoop System  •  " + java.time.LocalDate.now(),
                    new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY)
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(20);
            document.add(footer);

            document.close();
            System.out.println("[ReportController] PDF saved to: " + filePath);
            return true;

        } catch (Exception e) {
            System.err.println("[ReportController] PDF generation error: " + e.getMessage());
            return false;
        }
    }

    // ── PDF Helper Methods ────────────────────────────────────────────────────

    private void addSectionHeader(Document doc, String title) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingBefore(10);
        headerTable.setSpacingAfter(8);

        PdfPCell cell = new PdfPCell(new Phrase(title, FONT_SECTION));
        cell.setBackgroundColor(COLOR_HEADER);
        cell.setPadding(6);
        cell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(cell);
        doc.add(headerTable);
    }

    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BODY_BOLD));
        cell.setBackgroundColor(new BaseColor(200, 230, 200));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTableRow(PdfPTable table, BaseColor color, String... values) {
        for (String val : values) {
            PdfPCell cell = new PdfPCell(new Phrase(val, FONT_BODY));
            cell.setBackgroundColor(color);
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addSummaryRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FONT_BODY_BOLD));
        labelCell.setPadding(5);
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, FONT_BODY));
        valueCell.setPadding(5);
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(valueCell);
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private boolean isValidMonthYear(int month, int year) {
        if (month < 1 || month > 12) {
            System.err.println("[ReportController] Invalid month: " + month);
            return false;
        }
        if (year < 2000 || year > 2100) {
            System.err.println("[ReportController] Invalid year: " + year);
            return false;
        }
        return true;
    }
}