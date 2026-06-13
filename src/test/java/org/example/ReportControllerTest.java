package org.example;

import org.example.controller.ReportController;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReportControllerTest {

    private static ReportController controller;
    private static final String PDF_PATH = "test_report_june_2026.pdf";

    @BeforeAll
    static void setup() {
        controller = new ReportController();
    }

    @AfterAll
    static void cleanup() {
        // comment this out if you want to keep the PDF after tests
        // new File(PDF_PATH).delete();
    }

    // ── Validation Tests ──────────────────────────────────────────────────────

    @Test
    @Order(1)
    void testInvalidMonth() {
        List<?> result = controller.getMonthlyOrders(13, 2026);
        assertTrue(result.isEmpty(), "Month 13 should return empty list");
    }

    @Test
    @Order(2)
    void testInvalidYear() {
        List<?> result = controller.getMonthlyOrders(6, 1999);
        assertTrue(result.isEmpty(), "Year 1999 should return empty list");
    }

    @Test
    @Order(3)
    void testInvalidTopN() {
        List<?> result = controller.getTopSellingProducts(6, 2026, 0);
        assertTrue(result.isEmpty(), "topN=0 should return empty list");
    }

    @Test
    @Order(4)
    void testRevenueInvalidMonth() {
        double revenue = controller.getMonthlyRevenue(0, 2026);
        assertEquals(0.0, revenue, 0.01, "Invalid month should return 0.0 revenue");
    }

    // ── Data Tests ────────────────────────────────────────────────────────────

    @Test
    @Order(5)
    void testGetMonthlyOrders() {
        var orders = controller.getMonthlyOrders(6, 2026);
        assertNotNull(orders, "Orders should not be null");
        for (var o : orders) {
            assertEquals("DELIVERED", o.getStatus(),
                    "All orders should be DELIVERED");
        }
        System.out.println("Monthly orders count: " + orders.size());
    }

    @Test
    @Order(6)
    void testGetMonthlyRevenue() {
        double revenue = controller.getMonthlyRevenue(6, 2026);
        assertTrue(revenue >= 0, "Revenue should not be negative");

        // cross check with orders
        var orders = controller.getMonthlyOrders(6, 2026);
        double expected = orders.stream().mapToDouble(o -> o.getTotalAmount()).sum();
        assertEquals(expected, revenue, 0.01, "Revenue should match sum of delivered orders");

        System.out.println("Revenue: " + revenue);
    }

    @Test
    @Order(7)
    void testGetLowStockProducts() {
        var products = controller.getLowStockProducts();
        assertNotNull(products);
        for (var p : products) {
            assertTrue(p.getStockQuantity() <= p.getReorderLevel(),
                    p.getName() + " should be at or below reorder level");
        }
        System.out.println("Low stock products: " + products.size());
    }

    @Test
    @Order(8)
    void testGetTopSellingProducts() {
        var top = controller.getTopSellingProducts(6, 2026, 5);
        assertNotNull(top);

        // verify sorted descending by quantity
        for (int i = 0; i < top.size() - 1; i++) {
            assertTrue(
                    top.get(i).getQuantityOrdered() >= top.get(i + 1).getQuantityOrdered(),
                    "Should be sorted descending by quantity"
            );
        }
        System.out.println("Top sellers: " + top.size());
    }

    // ── PDF Generation Test ───────────────────────────────────────────────────

    @Test
    @Order(9)
    void testGeneratePDF() {
        boolean result = controller.generatePDFReport(6, 2026, PDF_PATH);

        assertTrue(result, "PDF generation should return true");

        // check file actually exists on disk
        File pdf = new File(PDF_PATH);
        assertTrue(pdf.exists(), "PDF file should exist on disk");
        assertTrue(pdf.length() > 0, "PDF file should not be empty");

        System.out.println("PDF generated at: " + pdf.getAbsolutePath());
        System.out.println("PDF size: " + pdf.length() + " bytes");
    }

    @Test
    @Order(10)
    void testGeneratePDF_InvalidMonth() {
        boolean result = controller.generatePDFReport(13, 2026, "invalid_test.pdf");
        assertFalse(result, "Should return false for invalid month");

        // make sure no file was created
        File pdf = new File("invalid_test.pdf");
        assertFalse(pdf.exists(), "No PDF should be created for invalid input");
    }
}