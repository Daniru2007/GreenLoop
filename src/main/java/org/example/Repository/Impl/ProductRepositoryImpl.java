package org.example.Repository.Impl;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.example.Repository.ProductRepository;
import org.example.config.DBManager;
import org.example.model.Product;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Add a new product to inventory
     */
    @Override
    public Product addProduct(Product product) {
        try {
            Document doc = new Document()
                    .append("name", product.getName())
                    .append("category", product.getCategory())
                    .append("material", product.getMaterial())
                    .append("price", product.getPrice())
                    .append("stock_quantity", product.getStockQuantity())
                    .append("unit", product.getUnit())
                    .append("supplier_name", product.getSupplierName())
                    .append("reorder_level", product.getReorderLevel());

            collection.insertOne(doc);
            product.setMongoId(doc.getObjectId("_id").toString());
            return product;
        } catch (Exception e) {
            System.err.println("[Product] addProduct error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Update product details
     */
    @Override
    public Product updateProduct(Product product) {
        try {
            collection.updateOne(
                    Filters.eq("_id", new ObjectId(product.getMongoId())),
                    Updates.combine(
                            Updates.set("name", product.getName()),
                            Updates.set("category", product.getCategory()),
                            Updates.set("material", product.getMaterial()),
                            Updates.set("price", product.getPrice()),
                            Updates.set("stock_quantity", product.getStockQuantity()),
                            Updates.set("unit", product.getUnit()),
                            Updates.set("supplier_name", product.getSupplierName()),
                            Updates.set("reorder_level", product.getReorderLevel())
                    )
            );
            return product;
        } catch (Exception e) {
            System.err.println("[Product] updateProduct error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete product by ID
     */
    @Override
    public void deleteProduct(String productId) {
        try {
            collection.deleteOne(Filters.eq("_id", new ObjectId(productId)));
        } catch (Exception e) {
            System.err.println("[Product] deleteProduct error: " + e.getMessage());
        }
    }

    /**
     * Get product by ID
     */
    @Override
    public Product getProductById(String productId) {
        try {
            Document doc = collection.find(Filters.eq("_id", new ObjectId(productId))).first();
            return doc != null ? mapDocToProduct(doc) : null;
        } catch (Exception e) {
            System.err.println("[Product] getProductById error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get all products
     */
    @Override
    public List<Product> getAllProducts() {
        try {
            List<Product> products = new ArrayList<>();
            for (Document doc : collection.find()) {
                products.add(mapDocToProduct(doc));
            }
            return products;
        } catch (Exception e) {
            System.err.println("[Product] getAllProducts error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Update stock quantity (increment/decrement)
     * @param productId Product MongoDB ID
     * @param quantityChange Positive for stock-in, negative for stock-out
     */
    @Override
    public Product updateStock(String productId, int quantityChange) {
        try {
            collection.updateOne(
                    Filters.eq("_id", new ObjectId(productId)),
                    Updates.inc("stock_quantity", quantityChange)
            );
            return getProductById(productId);
        } catch (Exception e) {
            System.err.println("[Product] updateStock error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get all products below reorder level (low stock alert)
     */
    @Override
    public List<Product> getLowStockItems() {
        try {
            List<Product> lowStockItems = new ArrayList<>();
            for (Document doc : collection.find()) {
                int stock = doc.getInteger("stock_quantity", 0);
                int reorderLevel = doc.getInteger("reorder_level", 0);
                if (stock < reorderLevel) {
                    lowStockItems.add(mapDocToProduct(doc));
                }
            }
            return lowStockItems;
        } catch (Exception e) {
            System.err.println("[Product] getLowStockItems error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get products by supplier name
     */
    @Override
    public List<Product> getProductsBySupplier(String supplierName) {
        try {
            List<Product> products = new ArrayList<>();
            for (Document doc : collection.find(Filters.eq("supplier_name", supplierName))) {
                products.add(mapDocToProduct(doc));
            }
            return products;
        } catch (Exception e) {
            System.err.println("[Product] getProductsBySupplier error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Stock in (receive inventory from supplier)
     * @param productId Product MongoDB ID
     * @param quantity Quantity received
     * @param supplierName Supplier name
     */
    @Override
    public Product stockIn(String productId, int quantity, String supplierName) {
        try {
            collection.updateOne(
                    Filters.eq("_id", new ObjectId(productId)),
                    Updates.combine(
                            Updates.inc("stock_quantity", quantity),
                            Updates.set("supplier_name", supplierName)
                    )
            );
            return getProductById(productId);
        } catch (Exception e) {
            System.err.println("[Product] stockIn error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper method to map MongoDB Document to Product object
     */
    private Product mapDocToProduct(Document doc) {
        Product p = new Product();
        Object id = doc.get("_id");
        p.setMongoId(id != null ? id.toString() : null);
        p.setName(doc.getString("name"));
        p.setCategory(doc.getString("category"));
        p.setMaterial(doc.getString("material"));
        p.setPrice(doc.get("price") != null ? ((Number) doc.get("price")).doubleValue() : 0.0);
        p.setStockQuantity(doc.get("stock_quantity") != null ? ((Number) doc.get("stock_quantity")).intValue() : 0);
        p.setUnit(doc.getString("unit"));
        p.setSupplierName(doc.getString("supplier_name"));
        p.setReorderLevel(doc.get("reorder_level") != null ? ((Number) doc.get("reorder_level")).intValue() : 0);
        return p;
    }
}
