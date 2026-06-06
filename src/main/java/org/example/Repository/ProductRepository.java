package org.example.Repository;

import org.example.model.Product;

/***
 * This is a layer where you can define methods for CRUD operations on products
 */
public interface ProductRepository {
    Product addProduct(Product product);
    Product updateProduct(Product product);
    void deleteProduct(int productId);
    Product getProductById(int productId);

}
