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
        List<CustomerOrder> orders = repo.getMonthlyDeliveredOrders(6, 2026);

        assertNotNull(orders, "Orders list should not be null");

        // verify ALL returned orders are DELIVERED — no matter how many there are
        for (CustomerOrder o : orders) {
            assertEquals("DELIVERED", o.getStatus(),
                    "Every returned order must have DELIVERED status");
        }

        System.out.println("Delivered orders in June 2026: " + orders.size());
    }

    @Test
    @Order(2)
    void testGetMonthlyDeliveredOrders_WrongMonth() {
        List<CustomerOrder> orders = repo.getMonthlyDeliveredOrders(1, 2020);

        assertNotNull(orders);
        assertTrue(orders.isEmpty(), "Should return empty list for a month with no orders");
    }

    @Test
    @Order(3)
    void testGetMonthlyRevenue() {
        double revenue = repo.getMonthlyRevenue(6, 2026);

        // revenue must be >= 0
        assertTrue(revenue >= 0, "Revenue should not be negative");

        // must match sum of delivered orders manually
        List<CustomerOrder> orders = repo.getMonthlyDeliveredOrders(6, 2026);
        double expected = orders.stream().mapToDouble(CustomerOrder::getTotalAmount).sum();
        assertEquals(expected, revenue, 0.01,
                "Revenue should equal sum of all delivered order totals");

        System.out.println("June 2026 revenue: " + revenue);
    }

    @Test
    @Order(4)
    void testGetMonthlyRevenue_NoOrders() {
        double revenue = repo.getMonthlyRevenue(1, 2020);

        assertEquals(0.0, revenue, 0.01,
                "Revenue should be 0.0 for a month with no orders");
    }

    @Test
    @Order(5)
    void testGetLowStockProducts() {
        List<Product> lowStock = repo.getLowStockProducts();

        assertNotNull(lowStock, "Low stock list should not be null");

        // every product returned must actually be at or below its reorder level
        for (Product p : lowStock) {
            assertTrue(p.getStockQuantity() <= p.getReorderLevel(),
                    p.getName() + " should have stock <= reorderLevel but was " +
                            p.getStockQuantity() + " <= " + p.getReorderLevel());
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

        // verify list is sorted descending by quantity
        for (int i = 0; i < top.size() - 1; i++) {
            assertTrue(
                    top.get(i).getQuantityOrdered() >= top.get(i + 1).getQuantityOrdered(),
                    "Products should be sorted by quantity descending"
            );
        }

        // verify no negative amounts
        for (CustomerOrder o : top) {
            assertTrue(o.getTotalAmount() >= 0, "Total amount should not be negative");
            assertTrue(o.getQuantityOrdered() > 0, "Quantity should be positive");
        }

        System.out.println("Top sellers:");
        for (CustomerOrder o : top) {
            System.out.println("  " + o.getProductName() +
                    " → qty: " + o.getQuantityOrdered() +
                    ", revenue: " + o.getTotalAmount());
        }
    }

    @Test
    @Order(7)
    void testGetTopSellingProducts_OnlyDelivered() {
        List<CustomerOrder> top = repo.getTopSellingProducts(6, 2026, 10);

        // cross check — every product in top sellers must also appear
        // in delivered orders, not from pending ones
        List<CustomerOrder> delivered = repo.getMonthlyDeliveredOrders(6, 2026);
        List<String> deliveredNames = delivered.stream()
                .map(CustomerOrder::getProductName)
                .toList();

        for (CustomerOrder o : top) {
            assertTrue(deliveredNames.contains(o.getProductName()),
                    o.getProductName() + " should only come from DELIVERED orders");
        }
    }
}