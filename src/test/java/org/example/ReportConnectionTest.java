package org.example;

import org.example.Repository.Impl.ReportImpl;
import org.example.model.CustomerOrder;
import org.example.model.Product;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReportConnectionTest {

    private static ReportImpl repo;

    @BeforeAll
    static void setup() {
        repo = new ReportImpl();
    }

    @Test
    @Order(1)
    void testGetMonthlyDeliveredOrders() {
        List<CustomerOrder> customerOrders = repo.getMonthlyDeliveredOrders(6, 2026);

        assertNotNull(customerOrders, "Orders list should not be null");
        assertEquals(2, customerOrders.size(), "Should only return 2 DELIVERED orders, not the PENDING one");

        // verify all returned orders are actually DELIVERED
        for (CustomerOrder o : customerOrders) {
            assertEquals("DELIVERED", o.getStatus(),
                    "All returned orders should have DELIVERED status");
        }

        System.out.println("Delivered orders in June 2026: " + customerOrders.size());
    }

    @Test
    @Order(2)
    void testGetMonthlyDeliveredOrders_WrongMonth() {
        List<CustomerOrder> customerOrders = repo.getMonthlyDeliveredOrders(1, 2026);

        assertNotNull(customerOrders);
        assertTrue(customerOrders.isEmpty(), "Should return empty list for a month with no orders");
    }

    @Test
    @Order(3)
    void testGetMonthlyRevenue() {
        double revenue = repo.getMonthlyRevenue(6, 2026);

        // Fresh Mart: 2500.00 + SuperStore: 3600.00 = 6100.00
        // PENDING order (1600.00) should NOT be included
        assertEquals(6100.00, revenue, 0.01,
                "Revenue should only sum DELIVERED orders");

        System.out.println("June 2026 revenue: " + revenue);
    }

    @Test
    @Order(4)
    void testGetMonthlyRevenue_NoOrders() {
        double revenue = repo.getMonthlyRevenue(1, 2026);

        assertEquals(0.0, revenue, 0.01,
                "Revenue should be 0.0 for a month with no orders");
    }

    @Test
    @Order(5)
    void testGetLowStockProducts() {
        List<Product> lowStock = repo.getLowStockProducts();

        assertNotNull(lowStock, "Low stock list should not be null");
        // Compostable Bag 5kg  → stock=5,  reorderLevel=10  ✅ low
        // Recycled Box Medium  → stock=50, reorderLevel=20  ❌ fine
        // Biodegradable Wrap   → stock=8,  reorderLevel=15  ✅ low
        assertEquals(2, lowStock.size(), "Should return 2 low stock products");

        for (Product p : lowStock) {
            assertTrue(p.getStockQuantity() <= p.getReorderLevel(),
                    p.getName() + " should have stock <= reorderLevel");
            System.out.println("Low stock: " + p.getName() +
                    " (stock=" + p.getStockQuantity() +
                    ", reorderLevel=" + p.getReorderLevel() + ")");
        }
    }

    @Test
    @Order(6)
    void testGetTopSellingProducts() {
        List<CustomerOrder> top = repo.getTopSellingProducts(6, 2026, 3);

        assertNotNull(top, "Top products list should not be null");
        assertFalse(top.isEmpty(), "Should return at least one product");

        // Recycled Box Medium: qty=20 should be first
        assertEquals("Recycled Box Medium", top.get(0).getProductName(),
                "Recycled Box Medium should be top seller with qty 20");

        System.out.println("Top sellers:");
        for (CustomerOrder o : top) {
            System.out.println("  " + o.getProductName() +
                    " → qty: " + o.getQuantityOrdered() +
                    ", revenue: " + o.getTotalAmount());
        }
    }

    @Test
    @Order(7)
    void testGetTopSellingProducts_PendingNotIncluded() {
        List<CustomerOrder> top = repo.getTopSellingProducts(6, 2026, 5);

        // Biodegradable Wrap Roll is PENDING so should not appear
        boolean pendingFound = top.stream()
                .anyMatch(o -> "Biodegradable Wrap Roll".equals(o.getProductName()));

        assertFalse(pendingFound,
                "PENDING orders should not appear in top selling products");
    }
}