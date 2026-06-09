package org.example.Repository;

import org.example.model.Product;
import java.util.List;

/***
 * This is a layer where you can define methods for CRUD operations on products
 */
public interface ProductRepository {
    // Basic CRUD
    Product addProduct(Product product);
    Product updateProduct(Product product);
    void deleteProduct(String productId);
    Product getProductById(String productId);
    List<Product> getAllProducts();

    // Inventory Management
    Product updateStock(String productId, int quantityChange);
    List<Product> getLowStockItems();
    List<Product> getProductsBySupplier(String supplierName);
    Product stockIn(String productId, int quantity, String supplierName);
}
