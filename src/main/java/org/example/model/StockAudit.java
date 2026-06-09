package org.example.model;

import java.time.LocalDateTime;

/**
 * StockAudit tracks all inventory transactions for audit and compliance purposes
 */
public class StockAudit {
    private String mongoId;
    private String productId;
    private String productName;
    private String transactionType;  // SALE, RESTOCK, ADJUSTMENT, INITIAL
    private int quantityBefore;
    private int quantityAfter;
    private int quantityChanged;
    private String supplierName;     // For RESTOCK transactions
    private double costPrice;        // Cost per unit for RESTOCK
    private LocalDateTime timestamp;
    private String notes;
    private String performedBy;      // User/system that performed action

    public StockAudit() {
        this.timestamp = LocalDateTime.now();
    }

    public StockAudit(String productId, String productName, String transactionType,
                      int quantityBefore, int quantityAfter, String notes) {
        this.productId = productId;
        this.productName = productName;
        this.transactionType = transactionType;
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.quantityChanged = quantityAfter - quantityBefore;
        this.notes = notes;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getMongoId()                              { return mongoId; }
    public void setMongoId(String mongoId)                  { this.mongoId = mongoId; }

    public String getProductId()                            { return productId; }
    public void setProductId(String productId)              { this.productId = productId; }

    public String getProductName()                          { return productName; }
    public void setProductName(String productName)          { this.productName = productName; }

    public String getTransactionType()                      { return transactionType; }
    public void setTransactionType(String transactionType)  { this.transactionType = transactionType; }

    public int getQuantityBefore()                          { return quantityBefore; }
    public void setQuantityBefore(int quantityBefore)       { this.quantityBefore = quantityBefore; }

    public int getQuantityAfter()                           { return quantityAfter; }
    public void setQuantityAfter(int quantityAfter)         { this.quantityAfter = quantityAfter; }

    public int getQuantityChanged()                         { return quantityChanged; }
    public void setQuantityChanged(int quantityChanged)     { this.quantityChanged = quantityChanged; }

    public String getSupplierName()                         { return supplierName; }
    public void setSupplierName(String supplierName)        { this.supplierName = supplierName; }

    public double getCostPrice()                            { return costPrice; }
    public void setCostPrice(double costPrice)              { this.costPrice = costPrice; }

    public LocalDateTime getTimestamp()                     { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp)       { this.timestamp = timestamp; }

    public String getNotes()                                { return notes; }
    public void setNotes(String notes)                      { this.notes = notes; }

    public String getPerformedBy()                          { return performedBy; }
    public void setPerformedBy(String performedBy)          { this.performedBy = performedBy; }

    @Override
    public String toString() {
        return String.format(
                "StockAudit{product='%s', type=%s, before=%d, after=%d, change=%+d, time=%s}",
                productName, transactionType, quantityBefore, quantityAfter, quantityChanged, timestamp
        );
    }
}
