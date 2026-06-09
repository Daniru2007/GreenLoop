package org.example;

import org.example.Repository.Impl.ProductRepositoryImpl;
import org.example.Repository.Impl.StockAuditImpl;
import org.example.controller.InventoryService;
import org.example.controller.ProductController;
import org.example.model.Product;
import org.example.model.StockAudit;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InventoryManagementTest {

    private static ProductController controller;
    private static ProductRepositoryImpl productRepo;
    private static StockAuditImpl auditRepo;
    private static String productId1;
    private static String productId2;

    @BeforeAll
    static void setup() {
        controller = new ProductController();
        productRepo = new ProductRepositoryImpl();
        auditRepo = new StockAuditImpl();
    }

    @Test
    @Order(1)
    void testAddProduct() {
        Product product = new Product("Eco Bag", "Bags", "Biodegradable", 15.50, 100, "pack", "GreenSupply Ltd", 30);
        Product saved = productRepo.addProduct(product);

        assertNotNull(saved, "Product should be saved");
        assertNotNull(saved.getMongoId(), "Product should have MongoDB ID");
        assertEquals("Eco Bag", saved.getName());
        assertEquals(100, saved.getStockQuantity());
        productId1 = saved.getMongoId();
    }

    @Test
    @Order(2)
    void testAddAnotherProduct() {
        Product product = new Product("Cardboard Box", "Boxes", "Recycled", 8.75, 50, "piece", "EcoBoxes Inc", 20);
        Product saved = productRepo.addProduct(product);

        assertNotNull(saved, "Second product should be saved");
        assertNotNull(saved.getMongoId());
        productId2 = saved.getMongoId();
    }

    @Test
    @Order(3)
    void testGetProductById() {
        Product product = productRepo.getProductById(productId1);

        assertNotNull(product, "Product should be found");
        assertEquals("Eco Bag", product.getName());
        assertEquals(100, product.getStockQuantity());
    }

    @Test
    @Order(4)
    void testGetAllProducts() {
        List<Product> all = productRepo.getAllProducts();

        assertNotNull(all, "Products list should not be null");
        assertTrue(all.size() >= 2, "Should have at least 2 products");
    }

    @Test
    @Order(5)
    void testUpdateProduct() {
        Product product = productRepo.getProductById(productId1);
        product.setPrice(16.00);
        product.setReorderLevel(40);

        Product updated = productRepo.updateProduct(product);

        assertNotNull(updated);
        assertEquals(16.00, updated.getPrice());
        assertEquals(40, updated.getReorderLevel());
    }

    @Test
    @Order(6)
    void testUpdateStock_Decrease() {
        Product before = productRepo.getProductById(productId1);
        int stockBefore = before.getStockQuantity();

        Product after = productRepo.updateStock(productId1, -10);

        assertNotNull(after);
        assertEquals(stockBefore - 10, after.getStockQuantity());
    }

    @Test
    @Order(7)
    void testUpdateStock_Increase() {
        Product before = productRepo.getProductById(productId1);
        int stockBefore = before.getStockQuantity();

        Product after = productRepo.updateStock(productId1, +20);

        assertNotNull(after);
        assertEquals(stockBefore + 20, after.getStockQuantity());
    }

    @Test
    @Order(8)
    void testStockIn() {
        Product product = new Product("Wrap", "Wraps", "Compostable", 5.25, 10, "roll", "NatureWrap Co", 25);
        Product saved = productRepo.addProduct(product);
        String wrapId = saved.getMongoId();

        int beforeStock = saved.getStockQuantity();
        Product restocked = productRepo.stockIn(wrapId, 50, "NatureWrap Co");

        assertNotNull(restocked);
        assertEquals(beforeStock + 50, restocked.getStockQuantity());
        assertEquals("NatureWrap Co", restocked.getSupplierName());
    }

    @Test
    @Order(9)
    void testGetLowStockItems() {
        List<Product> lowStock = productRepo.getLowStockItems();

        assertNotNull(lowStock, "Low stock list should not be null");
        // Verifies that all returned items are below reorder level
        for (Product p : lowStock) {
            assertTrue(p.getStockQuantity() < p.getReorderLevel());
        }
    }

    @Test
    @Order(10)
    void testGetProductsBySupplier() {
        List<Product> supplierProducts = productRepo.getProductsBySupplier("GreenSupply Ltd");

        assertNotNull(supplierProducts);
        assertFalse(supplierProducts.isEmpty(), "Should have at least 1 product from GreenSupply Ltd");
    }

    @Test
    @Order(11)
    void testAddAuditLog() {
        StockAudit audit = new StockAudit(productId1, "Eco Bag", "SALE", 100, 90, "Order #001");
        StockAudit saved = auditRepo.addAuditLog(audit);

        assertNotNull(saved);
        assertNotNull(saved.getMongoId());
        assertEquals("SALE", saved.getTransactionType());
    }

    @Test
    @Order(12)
    void testGetAuditsByProductId() {
        List<StockAudit> audits = auditRepo.getAuditsByProductId(productId1);

        assertNotNull(audits);
        assertFalse(audits.isEmpty(), "Should have audit logs for product");
    }

    @Test
    @Order(13)
    void testGetAuditsByType() {
        List<StockAudit> saleAudits = auditRepo.getAuditsByType("SALE");

        assertNotNull(saleAudits);
        assertTrue(saleAudits.size() >= 1, "Should have at least 1 SALE transaction");
    }

    /**
     * === INVENTORY SERVICE TESTS ===
     */

    @Test
    @Order(14)
    void testRegisterProductWithService() {
        System.out.println("TEST 14: Register Product with InventoryService");
        
        InventoryService service = new InventoryService();
        Product product = service.registerProduct(
                "Premium Bag",
                "Bags",
                "Biodegradable",
                20.00,
                75,
                "pack",
                "GreenSupply Ltd",
                35
        );

        assertNotNull(product, "Product should be registered");
        assertNotNull(product.getMongoId(), "Product should have ID");
        assertEquals("Premium Bag", product.getName(), "Name should match");
        
        System.out.println("✓ Product registered: " + product.getName() + "\n");
    }

    @Test
    @Order(15)
    void testProcessStockOut_Success() {
        System.out.println("TEST 15: Process Stock Out - Success");
        
        InventoryService service = new InventoryService();
        Product before = productRepo.getProductById(productId1);
        int stockBefore = before.getStockQuantity();

        boolean success = service.processStockOut(productId1, 5, "Order #002");

        assertTrue(success, "Stock out should succeed");
        Product after = productRepo.getProductById(productId1);
        assertEquals(stockBefore - 5, after.getStockQuantity(), "Stock should decrease by 5");
        
        System.out.println("✓ Stock out successful: " + stockBefore + " → " + after.getStockQuantity() + "\n");
    }

    @Test
    @Order(16)
    void testProcessStockOut_Insufficient() {
        System.out.println("TEST 16: Process Stock Out - Insufficient Stock");
        
        InventoryService service = new InventoryService();
        Product product = productRepo.getProductById(productId1);
        int currentStock = product.getStockQuantity();

        boolean success = service.processStockOut(productId1, currentStock + 100, "Invalid Order");

        assertFalse(success, "Stock out should fail when insufficient");
        
        System.out.println("✓ Stock out prevented (insufficient stock)\n");
    }

    @Test
    @Order(17)
    void testStockInWithService() {
        System.out.println("TEST 17: Stock In with InventoryService");
        
        InventoryService service = new InventoryService();
        Product before = productRepo.getProductById(productId2);
        int stockBefore = before.getStockQuantity();

        boolean success = service.stockIn(productId2, 30, "EcoBoxes Inc", 8.00);

        assertTrue(success, "Stock in should succeed");
        Product after = productRepo.getProductById(productId2);
        assertEquals(stockBefore + 30, after.getStockQuantity(), "Stock should increase by 30");
        
        System.out.println("✓ Stock in successful: " + stockBefore + " → " + after.getStockQuantity() + "\n");
    }

    @Test
    @Order(18)
    void testAdjustStock() {
        System.out.println("TEST 18: Adjust Stock");
        
        InventoryService service = new InventoryService();
        Product before = productRepo.getProductById(productId1);
        int stockBefore = before.getStockQuantity();

        boolean success = service.adjustStock(productId1, -3, "Damaged during storage");

        assertTrue(success, "Adjustment should succeed");
        Product after = productRepo.getProductById(productId1);
        assertEquals(stockBefore - 3, after.getStockQuantity(), "Stock should decrease by 3");
        
        System.out.println("✓ Stock adjusted: " + stockBefore + " → " + after.getStockQuantity() + "\n");
    }

    @Test
    @Order(19)
    void testGetReorderRecommendation() {
        System.out.println("TEST 19: Get Reorder Recommendation");
        
        InventoryService service = new InventoryService();
        Map<String, Object> rec = service.getReorderRecommendation(productId1, 5.0, 7);

        assertNotNull(rec, "Recommendation should not be null");
        assertFalse(rec.isEmpty(), "Recommendation should have data");
        assertNotNull(rec.get("productName"), "Should have product name");
        assertNotNull(rec.get("currentStock"), "Should have current stock");
        assertNotNull(rec.get("reorderPoint"), "Should have reorder point");
        
        System.out.println("✓ Reorder Recommendation:");
        System.out.println("  Product: " + rec.get("productName"));
        System.out.println("  Current Stock: " + rec.get("currentStock"));
        System.out.println("  Reorder Point: " + rec.get("reorderPoint"));
        System.out.println("  Recommended Qty: " + rec.get("recommendedQuantity"));
        System.out.println("  Needs Reorder: " + rec.get("needsReorder") + "\n");
    }

    @Test
    @Order(20)
    void testGetInventoryAnalytics() {
        System.out.println("TEST 20: Get Inventory Analytics");
        
        InventoryService service = new InventoryService();
        Map<String, Object> analytics = service.getInventoryAnalytics();

        assertNotNull(analytics, "Analytics should not be null");
        assertFalse(analytics.isEmpty(), "Analytics should have data");
        assertNotNull(analytics.get("totalProducts"), "Should have total products");
        assertNotNull(analytics.get("totalUnits"), "Should have total units");
        assertNotNull(analytics.get("totalInventoryValue"), "Should have total value");
        
        System.out.println("✓ Inventory Analytics:");
        System.out.println("  Total Products: " + analytics.get("totalProducts"));
        System.out.println("  Total Units: " + analytics.get("totalUnits"));
        System.out.println("  Total Value: $" + analytics.get("totalInventoryValue"));
        System.out.println("  Low Stock Items: " + analytics.get("lowStockCount"));
        System.out.println("  Products Above Reorder: " + analytics.get("productsAboveReorder"));
        System.out.println("  Avg Value per Product: $" + analytics.get("averageValuePerProduct") + "\n");
    }

    @Test
    @Order(21)
    void testGetProductTransactionHistory() {
        System.out.println("TEST 21: Get Product Transaction History");
        
        InventoryService service = new InventoryService();
        List<StockAudit> history = service.getProductTransactionHistory(productId1);

        assertNotNull(history, "History should not be null");
        
        System.out.println("✓ Transaction History for Product:");
        System.out.println(String.format("  %-12s %-10s %-10s %-12s %-20s", 
                "Type", "Before", "After", "Change", "Notes"));
        System.out.println("  ──────────────────────────────────────────────────────");
        for (StockAudit a : history) {
            System.out.println(String.format("  %-12s %-10d %-10d %-12d %-20s",
                    a.getTransactionType(),
                    a.getQuantityBefore(),
                    a.getQuantityAfter(),
                    a.getQuantityChanged(),
                    a.getNotes() != null ? a.getNotes() : ""));
        }
        System.out.println();
    }

    /**
     * === PRODUCT CONTROLLER TESTS ===
     */

    @Test
    @Order(22)
    void testControllerAddProduct() {
        System.out.println("TEST 22: ProductController - Add Product");
        
        Product product = controller.addProduct(
                "Test Product",
                "Test",
                "Compostable",
                10.00,
                50,
                "unit",
                "Test Supplier",
                20
        );

        assertNotNull(product, "Product should be added via controller");
        assertEquals("Test Product", product.getName(), "Name should match");
        
        System.out.println("✓ Product added via controller: " + product.getName() + "\n");
    }

    @Test
    @Order(23)
    void testControllerProcessSale() {
        System.out.println("TEST 23: ProductController - Process Sale");
        
        Product before = productRepo.getProductById(productId1);
        int stockBefore = before.getStockQuantity();

        boolean success = controller.processSale(productId1, 5, "Order #003");

        assertTrue(success, "Sale should be processed");
        Product after = productRepo.getProductById(productId1);
        assertEquals(stockBefore - 5, after.getStockQuantity(), "Stock should decrease");
        
        System.out.println("✓ Sale processed: " + stockBefore + " → " + after.getStockQuantity() + "\n");
    }

    @Test
    @Order(24)
    void testControllerReceiveStockFromSupplier() {
        System.out.println("TEST 24: ProductController - Receive Stock from Supplier");
        
        Product before = productRepo.getProductById(productId2);
        int stockBefore = before.getStockQuantity();

        boolean success = controller.receiveStockFromSupplier(productId2, 25, "EcoBoxes Inc", 8.00);

        assertTrue(success, "Stock receive should succeed");
        Product after = productRepo.getProductById(productId2);
        assertEquals(stockBefore + 25, after.getStockQuantity(), "Stock should increase");
        
        System.out.println("✓ Stock received: " + stockBefore + " → " + after.getStockQuantity() + "\n");
    }

    @Test
    @Order(25)
    void testControllerGetLowStockItems() {
        System.out.println("TEST 25: ProductController - Get Low Stock Items");
        
        List<Product> lowStock = controller.getLowStockItems();

        assertNotNull(lowStock, "Low stock list should not be null");
        
        System.out.println("✓ Low stock items: " + lowStock.size() + "\n");
    }

    @AfterAll
    static void teardown() {
        System.out.println("========== All Tests Completed Successfully ==========\n");
    }
}
