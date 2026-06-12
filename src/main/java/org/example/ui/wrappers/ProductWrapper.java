package org.example.ui.wrappers;

import org.example.model.Product;

public class ProductWrapper {
    private final Product product;
    public ProductWrapper(Product product) { this.product = product; }
    public Product getProduct() { return product; }
    @Override
    public String toString() {
        return String.format("%s (Rs. %.2f)", product.getName(), product.getPrice());
    }
}
