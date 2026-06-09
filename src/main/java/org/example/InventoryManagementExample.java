package org.example;

import org.example.controller.ProductController;
import org.example.model.Product;
import org.example.model.StockAudit;
import java.util.List;
import java.util.Map;

/**
 * InventoryManagementExample demonstrates how to use the Stock/Inventory management system
 * This example shows all major inventory operations
 */
public class InventoryManagementExample {
    
    public static void main(String[] args) {
        ProductController controller = new ProductController();
        
        System.out.println("========== GreenLoop Inventory Management System ==========\n");
        
        // ===== 1. REGISTER NEW PRODUCTS =====
        System.out.println("1. Registering new products...\n");
        
        Product bag = controller.addProduct(
                "Eco Bag",
                "Bags",
                "Biodegradable",
                15.50,
                100,      // Initial stock
                "pack",
                "GreenSupply Ltd",
                30        // Reorder level
        );
        System.out.println("✓ Registered: " + bag + "\n");
        
        Product box = controller.addProduct(
                "Cardboard Box",
                "Boxes",
                "Recycled",
                8.75,
                50,
                "piece",
                "EcoBoxes Inc",
                20
        );
        System.out.println("✓ Registered: " + box + "\n");
        
        Product wrap = controller.addProduct(
                "Compostable Wrap",
                "Wraps",
                "Compostable",
                5.25,
                15,
                "roll",
                "NatureWrap Co",
                25
        );
        System.out.println("✓ Registered: " + wrap + "\n");
        
        // ===== 2. PROCESS SALES (Stock Out) =====
        System.out.println("2. Processing sales (stock out)...\n");
        
        boolean sale1 = controller.processSale(
                bag.getMongoId(),
                10,
                "Order #001"
        );
        System.out.println("✓ Sale processed: 10 packs of Eco Bag - " + (sale1 ? "SUCCESS" : "FAILED") + "\n");
        
        boolean sale2 = controller.processSale(
                box.getMongoId(),
                25,
                "Order #002"
        );
        System.out.println("✓ Sale processed: 25 pieces of Cardboard Box - " + (sale2 ? "SUCCESS" : "FAILED") + "\n");
        
        // ===== 3. CHECK LOW STOCK ALERTS =====
        System.out.println("3. Checking low stock items...\n");
        List<Product> lowStockItems = controller.getLowStockItems();
        if (lowStockItems.isEmpty()) {
            System.out.println("✓ No low stock items\n");
        } else {
            System.out.println("✓ Low stock alerts (" + lowStockItems.size() + "):");
            for (Product p : lowStockItems) {
                System.out.println("  - " + p.getName() + ": " + p.getStockQuantity() + 
                        " (Reorder level: " + p.getReorderLevel() + ")\n");
            }
        }
        
        // ===== 4. RECEIVE STOCK FROM SUPPLIER =====
        System.out.println("4. Receiving stock from supplier...\n");
        
        boolean restock1 = controller.receiveStockFromSupplier(
                wrap.getMongoId(),
                50,
                "NatureWrap Co",
                5.00    // Cost per unit
        );
        System.out.println("✓ Received 50 rolls of Compostable Wrap - " + (restock1 ? "SUCCESS" : "FAILED") + "\n");
        
        // ===== 5. INVENTORY ADJUSTMENTS =====
        System.out.println("5. Adjusting inventory (for damage/loss)...\n");
        
        boolean adjust1 = controller.adjustInventory(
                bag.getMongoId(),
                -5,
                "Damaged during storage"
        );
        System.out.println("✓ Adjusted Eco Bag inventory by -5 - " + (adjust1 ? "SUCCESS" : "FAILED") + "\n");
        
        // ===== 6. VIEW ALL INVENTORY =====
        System.out.println("6. Current Inventory Status:\n");
        List<Product> allProducts = controller.getAllProducts();
        System.out.println(String.format("%-25s %-15s %-10s %-12s %-15s", 
                "Product", "Unit", "Stock", "Reorder Lvl", "Supplier"));
        System.out.println("─────────────────────────────────────────────────────────────");
        for (Product p : allProducts) {
            System.out.println(String.format("%-25s %-15s %-10d %-12d %-15s",
                    p.getName(),
                    p.getUnit(),
                    p.getStockQuantity(),
                    p.getReorderLevel(),
                    p.getSupplierName()));
        }
        System.out.println();
        
        // ===== 7. REORDER RECOMMENDATIONS =====
        System.out.println("7. Reorder Recommendations:\n");
        
        Map<String, Object> rec1 = controller.getReorderRecommendation(
                bag.getMongoId(),
                5.0,        // Expected 5 packs/day
                7           // 7 days lead time
        );
        if (!rec1.isEmpty()) {
            System.out.println("Product: " + rec1.get("productName"));
            System.out.println("  Current Stock: " + rec1.get("currentStock"));
            System.out.println("  Reorder Point: " + rec1.get("reorderPoint"));
            System.out.println("  Needs Reorder: " + rec1.get("needsReorder"));
            System.out.println("  Recommended Qty: " + rec1.get("recommendedQuantity") + "\n");
        }
        
        // ===== 8. INVENTORY ANALYTICS =====
        System.out.println("8. Inventory Analytics:\n");
        Map<String, Object> analytics = controller.getInventoryAnalytics();
        System.out.println("Total Products: " + analytics.get("totalProducts"));
        System.out.println("Total Units in Stock: " + analytics.get("totalUnits"));
        System.out.println("Total Inventory Value: $" + analytics.get("totalInventoryValue"));
        System.out.println("Low Stock Items: " + analytics.get("lowStockCount"));
        System.out.println("Products Above Reorder: " + analytics.get("productsAboveReorder"));
        System.out.println("Avg Value Per Product: $" + analytics.get("averageValuePerProduct") + "\n");
        
        // ===== 9. TRANSACTION HISTORY =====
        System.out.println("9. Transaction History for Eco Bag:\n");
        List<StockAudit> history = controller.getProductTransactionHistory(bag.getMongoId());
        if (history.isEmpty()) {
            System.out.println("No transactions yet\n");
        } else {
            System.out.println(String.format("%-12s %-10s %-10s %-12s %-30s", 
                    "Type", "Before", "After", "Change", "Notes"));
            System.out.println("────────────────────────────────────────────────────────────────");
            for (StockAudit audit : history) {
                System.out.println(String.format("%-12s %-10d %-10d %-12d %-30s",
                        audit.getTransactionType(),
                        audit.getQuantityBefore(),
                        audit.getQuantityAfter(),
                        audit.getQuantityChanged(),
                        audit.getNotes() != null ? audit.getNotes() : ""));
            }
            System.out.println();
        }
        
        // ===== 10. PRODUCTS BY SUPPLIER =====
        System.out.println("10. Products from GreenSupply Ltd:\n");
        List<Product> supplierProducts = controller.getProductsBySupplier("GreenSupply Ltd");
        for (Product p : supplierProducts) {
            System.out.println("  • " + p.getName() + " (" + p.getStockQuantity() + " " + p.getUnit() + ")");
        }
        System.out.println();
        
        System.out.println("========== Inventory Management Example Complete ==========");
    }
}
