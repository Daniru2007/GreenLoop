package org.example.Repository.Impl;

import com.mongodb.DB;
import com.mongodb.client.MongoCollection;
import org.example.Repository.ProductRepository;
import org.example.config.DBManager;
import org.example.model.Product;

import org.bson.Document;

/**
 * In this folder, you have to implement the methods that defined in the product repository interface.
 * Consideration -
 * Don't use extra logics for the database in this layer and just implement the methods and return the particular things.
 *
 */
public class ProductRepositoryImpl implements ProductRepository {
    /**
     * We use "final" keyword to make sure that the collection reference is immutable and can't reassigned after initialization
     *
     * **/
    private final  MongoCollection<Document> collection;


    public ProductRepositoryImpl() {
        this.collection = DBManager.getDatabase().getCollection("products");
    }


    @Override
    public Product addProduct(Product product) {
        return null;
    }

    @Override
    public Product updateProduct(Product product) {
        return null;
    }

    @Override
    public void deleteProduct(int productId) {

    }

    @Override
    public Product getProductById(int productId) {
        return null;
    }
}
