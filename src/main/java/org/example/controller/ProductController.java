package org.example.controller;


import org.example.Repository.Impl.ProductRepositoryImpl;
import org.example.Repository.ProductRepository;
import org.example.model.Product;
import org.example.model.StockAudit;
import java.util.List;
import java.util.Map;

/**
 * ProductController handles all product and inventory operations
 * Routes requests to appropriate services (ProductRepository, InventoryService)
 */
public class ProductController {
    /***
     * Use this object to call the methods in the product repository
     * consideration - Don't use this object in static variables
     */
    private final ProductRepository productRepo = new ProductRepositoryImpl();
    private final InventoryService inventoryService = new InventoryService();

    /**
     * === BASIC PRODUCT OPERATIONS ===
     */

    /**
     * Add a new product to inventory with initial stock
     */
    public Product addProduct(String name, String category, String material,
                              double price, int initialStock, String unit,
                              String supplierName, int reorderLevel) {
        return inventoryService.registerProduct(name, category, material, price,
                initialStock, unit, supplierName, reorderLevel);
    }

    /**
     * Update product details
     */
    public Product updateProduct(String productId, String name, String category, String material, double price, int stock,
                                 String unit, String supplier, int reorderLevel) {
        Product existing = getProductById(productId);
        if (existing == null) {
            return null;
        }

        Product updated = inventoryService.updateProductDetails(productId, name, category, material, price, unit, supplier, reorderLevel);

        if (updated != null && existing.getStockQuantity() != stock) {
            int diff = stock - existing.getStockQuantity();
            inventoryService.adjustStock(productId, diff, "Manual adjustment during product details update");
            updated.setStockQuantity(stock);
        }
        return updated;
    }

    /**
     * Get product by ID
     */
    public Product getProductById(String productId) {
        return productRepo.getProductById(productId);
    }

    /**
     * Get all products
     */
    public List<Product> getAllProducts() {
        return inventoryService.getAllInventory();
    }

    /**
     * Delete a product
     */
    public boolean deleteProduct(String productId) {
        return inventoryService.deleteProduct(productId);
    }

    /**
     * === STOCK MANAGEMENT OPERATIONS ===
     */

    /**
     * Process a sale - decrement stock
     *
     * @return true if successful, false if insufficient stock
     */
    public boolean processSale(String productId, int quantity, String orderDetails) {
        return inventoryService.processStockOut(productId, quantity,
                "Order: " + orderDetails);
    }

    /**
     * Receive stock from supplier - increment inventory
     */
    public boolean receiveStockFromSupplier(String productId, int quantity,
                                            String supplierName, double costPerUnit) {
        return inventoryService.stockIn(productId, quantity, supplierName, costPerUnit);
    }

    /**
     * Adjust stock for inventory corrections (shrinkage, damage, etc.)
     * quantityChange can be positive or negative
     */
    public boolean adjustInventory(String productId, int quantityChange, String reason) {
        return inventoryService.adjustStock(productId, quantityChange, reason);
    }

    /**
     * === INVENTORY ALERTS & MONITORING ===
     */

    /**
     * Get all products below reorder level
     */
    public List<Product> getLowStockItems() {
        return inventoryService.getLowStockAlert();
    }

    /**
     * Get reorder recommendation for a product
     *
     * @param avgDailyUsage Expected average daily usage
     * @param leadTimeDays  Days required for supplier delivery
     */
    public Map<String, Object> getReorderRecommendation(String productId,
                                                        double avgDailyUsage,
                                                        int leadTimeDays) {
        return inventoryService.getReorderRecommendation(productId, avgDailyUsage, leadTimeDays);
    }

    /**
     * Get inventory analytics and summary
     */
    public Map<String, Object> getInventoryAnalytics() {
        return inventoryService.getInventoryAnalytics();
    }

    /**
     * === REPORTING & AUDIT ===
     */

    /**
     * Get transaction history for a product
     */
    public List<StockAudit> getProductTransactionHistory(String productId) {
        return inventoryService.getProductTransactionHistory(productId);
    }


    /**
     * Get products by supplier
     */
    public List<Product> getProductsBySupplier(String supplierName) {
        return productRepo.getProductsBySupplier(supplierName);
    }
}
