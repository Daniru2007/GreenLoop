package org.example.controller;

import org.example.Repository.ProductRepository;
import org.example.Repository.StockAuditRepository;
import org.example.Repository.Impl.ProductRepositoryImpl;
import org.example.Repository.Impl.StockAuditImpl;
import org.example.model.Product;
import org.example.model.StockAudit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * InventoryService handles business logic for inventory management
 * Coordinates between ProductRepository and StockAuditRepository
 * Provides inventory operations like stock-in, stock-out, and low-stock alerts
 */
public class InventoryService {
    private final ProductRepository productRepo;
    private final StockAuditRepository auditRepo;

    public InventoryService() {
        this.productRepo = new ProductRepositoryImpl();
        this.auditRepo = new StockAuditImpl();
    }

    /**
     * Register a new product with initial stock
     */
    public Product registerProduct(String name, String category, String material,
                                   double price, int initialStock, String unit,
                                   String supplierName, int reorderLevel) {
        try {
            Product product = new Product(name, category, material, price, initialStock,
                    unit, supplierName, reorderLevel);
            
            Product savedProduct = productRepo.addProduct(product);
            
            if (savedProduct != null) {
                // Create initial audit log
                StockAudit audit = new StockAudit(
                        savedProduct.getMongoId(),
                        name,
                        "INITIAL",
                        0,
                        initialStock,
                        "Initial stock registration"
                );
                auditRepo.addAuditLog(audit);
                System.out.println("[InventoryService] Product registered: " + name);
            }
            
            return savedProduct;
        } catch (Exception e) {
            System.err.println("[InventoryService] registerProduct error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Decrement stock when a sale occurs
     * Returns success if enough stock available
     */
    public boolean processStockOut(String productId, int quantity, String reason) {
        try {
            Product product = productRepo.getProductById(productId);
            if (product == null) {
                System.err.println("[InventoryService] Product not found: " + productId);
                return false;
            }
            
            if (product.getStockQuantity() < quantity) {
                System.err.println("[InventoryService] Insufficient stock. Available: " + 
                        product.getStockQuantity() + ", Requested: " + quantity);
                return false;
            }
            
            int beforeQuantity = product.getStockQuantity();
            Product updated = productRepo.updateStock(productId, -quantity);
            
            if (updated != null) {
                // Log the transaction
                StockAudit audit = new StockAudit(
                        productId,
                        product.getName(),
                        "SALE",
                        beforeQuantity,
                        beforeQuantity - quantity,
                        reason
                );
                auditRepo.addAuditLog(audit);
                System.out.println("[InventoryService] Stock out: " + product.getName() + 
                        " (" + quantity + " " + product.getUnit() + ")");
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("[InventoryService] processStockOut error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Increment stock when receiving from supplier
     */
    public boolean stockIn(String productId, int quantity, String supplierName, double costPerUnit) {
        try {
            Product product = productRepo.getProductById(productId);
            if (product == null) {
                System.err.println("[InventoryService] Product not found: " + productId);
                return false;
            }
            
            int beforeQuantity = product.getStockQuantity();
            Product updated = productRepo.stockIn(productId, quantity, supplierName);
            
            if (updated != null) {
                // Log the restock transaction
                StockAudit audit = new StockAudit(
                        productId,
                        product.getName(),
                        "RESTOCK",
                        beforeQuantity,
                        beforeQuantity + quantity,
                        "Stock received from supplier"
                );
                audit.setSupplierName(supplierName);
                audit.setCostPrice(costPerUnit);
                auditRepo.addAuditLog(audit);
                System.out.println("[InventoryService] Stock in: " + product.getName() + 
                        " (" + quantity + " " + product.getUnit() + ") from " + supplierName);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("[InventoryService] stockIn error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adjust stock for inventory corrections
     */
    public boolean adjustStock(String productId, int quantityChange, String reason) {
        try {
            Product product = productRepo.getProductById(productId);
            if (product == null) {
                System.err.println("[InventoryService] Product not found: " + productId);
                return false;
            }
            
            int beforeQuantity = product.getStockQuantity();
            int afterQuantity = beforeQuantity + quantityChange;
            
            if (afterQuantity < 0) {
                System.err.println("[InventoryService] Adjustment would result in negative stock");
                return false;
            }
            
            Product updated = productRepo.updateStock(productId, quantityChange);
            
            if (updated != null) {
                // Log the adjustment
                StockAudit audit = new StockAudit(
                        productId,
                        product.getName(),
                        "ADJUSTMENT",
                        beforeQuantity,
                        afterQuantity,
                        reason
                );
                auditRepo.addAuditLog(audit);
                System.out.println("[InventoryService] Stock adjusted: " + product.getName() + 
                        " by " + quantityChange);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("[InventoryService] adjustStock error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all products with low stock (below reorder level)
     */
    public List<Product> getLowStockAlert() {
        try {
            List<Product> lowStockItems = productRepo.getLowStockItems();
            System.out.println("[InventoryService] Low stock items found: " + lowStockItems.size());
            return lowStockItems;
        } catch (Exception e) {
            System.err.println("[InventoryService] getLowStockAlert error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get reorder recommendations based on usage patterns
     * Simple calculation: reorder point = (average daily usage * lead time days) + safety stock
     */
    public Map<String, Object> getReorderRecommendation(String productId, 
                                                        double avgDailyUsage, 
                                                        int leadTimeDays) {
        try {
            Product product = productRepo.getProductById(productId);
            if (product == null) {
                return new HashMap<>();
            }
            
            int safetyStock = product.getReorderLevel();
            int reorderPoint = (int) (avgDailyUsage * leadTimeDays) + safetyStock;
            int currentStock = product.getStockQuantity();
            
            Map<String, Object> recommendation = new HashMap<>();
            recommendation.put("productId", productId);
            recommendation.put("productName", product.getName());
            recommendation.put("currentStock", currentStock);
            recommendation.put("reorderPoint", reorderPoint);
            recommendation.put("recommendedQuantity", reorderPoint - currentStock);
            recommendation.put("supplier", product.getSupplierName());
            recommendation.put("needsReorder", currentStock < reorderPoint);
            
            return recommendation;
        } catch (Exception e) {
            System.err.println("[InventoryService] getReorderRecommendation error: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Get inventory value analysis
     */
    public Map<String, Object> getInventoryAnalytics() {
        try {
            List<Product> allProducts = productRepo.getAllProducts();
            List<Product> lowStockItems = productRepo.getLowStockItems();
            
            double totalInventoryValue = 0;
            int totalUnits = 0;
            int productsAboveReorder = 0;
            
            for (Product p : allProducts) {
                totalInventoryValue += p.getPrice() * p.getStockQuantity();
                totalUnits += p.getStockQuantity();
                if (p.getStockQuantity() >= p.getReorderLevel()) {
                    productsAboveReorder++;
                }
            }
            
            Map<String, Object> analytics = new HashMap<>();
            analytics.put("totalProducts", allProducts.size());
            analytics.put("totalUnits", totalUnits);
            analytics.put("totalInventoryValue", String.format("%.2f", totalInventoryValue));
            analytics.put("lowStockCount", lowStockItems.size());
            analytics.put("productsAboveReorder", productsAboveReorder);
            analytics.put("averageValuePerProduct", 
                    String.format("%.2f", allProducts.size() > 0 ? totalInventoryValue / allProducts.size() : 0));
            
            return analytics;
        } catch (Exception e) {
            System.err.println("[InventoryService] getInventoryAnalytics error: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Get stock transaction history for a product
     */
    public List<StockAudit> getProductTransactionHistory(String productId) {
        try {
            return auditRepo.getAuditsByProductId(productId);
        } catch (Exception e) {
            System.err.println("[InventoryService] getProductTransactionHistory error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get all products
     */
    public List<Product> getAllInventory() {
        try {
            return productRepo.getAllProducts();
        } catch (Exception e) {
            System.err.println("[InventoryService] getAllInventory error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Update product details
     */
    public Product updateProductDetails(String productId, String name, String category, 
                                       String material, double price, String unit,
                                       String supplierName, int reorderLevel) {
        try {
            Product product = productRepo.getProductById(productId);
            if (product == null) {
                return null;
            }
            
            product.setName(name);
            product.setCategory(category);
            product.setMaterial(material);
            product.setPrice(price);
            product.setUnit(unit);
            product.setSupplierName(supplierName);
            product.setReorderLevel(reorderLevel);
            
            return productRepo.updateProduct(product);
        } catch (Exception e) {
            System.err.println("[InventoryService] updateProductDetails error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete a product from inventory
     */
    public boolean deleteProduct(String productId) {
        try {
            productRepo.deleteProduct(productId);
            System.out.println("[InventoryService] Product deleted: " + productId);
            return true;
        } catch (Exception e) {
            System.err.println("[InventoryService] deleteProduct error: " + e.getMessage());
            return false;
        }
    }
}
