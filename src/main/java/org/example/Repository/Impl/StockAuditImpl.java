package org.example.Repository.Impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.Repository.StockAuditRepository;
import org.example.config.DBManager;
import org.example.model.StockAudit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of StockAuditRepository for MongoDB
 * Handles persistence of inventory transaction audit logs
 */
public class StockAuditImpl implements StockAuditRepository {
    private final MongoCollection<Document> collection;

    public StockAuditImpl() {
        this.collection = DBManager.getDatabase().getCollection("stock_audits");
    }

    /**
     * Add a new audit log entry
     */
    @Override
    public StockAudit addAuditLog(StockAudit audit) {
        try {
            Document doc = new Document()
                    .append("product_id", audit.getProductId())
                    .append("product_name", audit.getProductName())
                    .append("transaction_type", audit.getTransactionType())
                    .append("quantity_before", audit.getQuantityBefore())
                    .append("quantity_after", audit.getQuantityAfter())
                    .append("quantity_changed", audit.getQuantityChanged())
                    .append("supplier_name", audit.getSupplierName())
                    .append("cost_price", audit.getCostPrice())
                    .append("timestamp", audit.getTimestamp().toString())
                    .append("notes", audit.getNotes())
                    .append("performed_by", audit.getPerformedBy());

            collection.insertOne(doc);
            audit.setMongoId(doc.getObjectId("_id").toString());
            return audit;
        } catch (Exception e) {
            System.err.println("[StockAudit] addAuditLog error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get all audit logs for a specific product
     */
    @Override
    public List<StockAudit> getAuditsByProductId(String productId) {
        try {
            List<StockAudit> audits = new ArrayList<>();
            for (Document doc : collection.find(Filters.eq("product_id", productId))) {
                audits.add(mapDocToAudit(doc));
            }
            return audits;
        } catch (Exception e) {
            System.err.println("[StockAudit] getAuditsByProductId error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get all audit logs by transaction type (SALE, RESTOCK, ADJUSTMENT, INITIAL)
     */
    @Override
    public List<StockAudit> getAuditsByType(String transactionType) {
        try {
            List<StockAudit> audits = new ArrayList<>();
            for (Document doc : collection.find(Filters.eq("transaction_type", transactionType))) {
                audits.add(mapDocToAudit(doc));
            }
            return audits;
        } catch (Exception e) {
            System.err.println("[StockAudit] getAuditsByType error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get all audit logs
     */
    @Override
    public List<StockAudit> getAllAudits() {
        try {
            List<StockAudit> audits = new ArrayList<>();
            for (Document doc : collection.find()) {
                audits.add(mapDocToAudit(doc));
            }
            return audits;
        } catch (Exception e) {
            System.err.println("[StockAudit] getAllAudits error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get audit logs within a date range
     */
    @Override
    public List<StockAudit> getAuditsByDateRange(String startDate, String endDate) {
        try {
            List<StockAudit> audits = new ArrayList<>();
            for (Document doc : collection.find(
                    Filters.and(
                            Filters.gte("timestamp", startDate),
                            Filters.lte("timestamp", endDate)
                    )
            )) {
                audits.add(mapDocToAudit(doc));
            }
            return audits;
        } catch (Exception e) {
            System.err.println("[StockAudit] getAuditsByDateRange error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Helper method to map MongoDB Document to StockAudit object
     */
    private StockAudit mapDocToAudit(Document doc) {
        StockAudit audit = new StockAudit();
        audit.setMongoId(doc.getObjectId("_id").toString());
        audit.setProductId(doc.getString("product_id"));
        audit.setProductName(doc.getString("product_name"));
        audit.setTransactionType(doc.getString("transaction_type"));
        audit.setQuantityBefore(doc.getInteger("quantity_before", 0));
        audit.setQuantityAfter(doc.getInteger("quantity_after", 0));
        audit.setQuantityChanged(doc.getInteger("quantity_changed", 0));
        audit.setSupplierName(doc.getString("supplier_name"));
        audit.setCostPrice(doc.getDouble("cost_price"));
        
        String timestampStr = doc.getString("timestamp");
        if (timestampStr != null) {
            audit.setTimestamp(LocalDateTime.parse(timestampStr));
        }
        
        audit.setNotes(doc.getString("notes"));
        audit.setPerformedBy(doc.getString("performed_by"));
        return audit;
    }
}
