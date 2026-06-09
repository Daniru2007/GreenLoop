package org.example.model;

import java.time.LocalDate;


public class CustomerOrder {

    // Order status constants
    public static final String STATUS_PENDING    = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_DELIVERED  = "DELIVERED";
    public static final String STATUS_CANCELLED  = "CANCELLED";

    private String mongoId;
    private String productId;
    private String productName;
    private int quantityOrdered;
    private double unitPrice;
    private double totalAmount;
    private String customerName;
    private LocalDate orderDate;
    private String status;
    private String deliveryAgentId;

    public CustomerOrder() {}

    public CustomerOrder(String productId, String productName, int quantityOrdered,
                         double unitPrice, String customerName, LocalDate orderDate,
                         String deliveryAgentId) {
        this.productId       = productId;
        this.productName     = productName;
        this.quantityOrdered = quantityOrdered;
        this.unitPrice       = unitPrice;
        this.totalAmount     = quantityOrdered * unitPrice;
        this.customerName    = customerName;
        this.orderDate       = orderDate;
        this.deliveryAgentId = deliveryAgentId;
        this.status          = STATUS_PENDING; // default on creation
    }

    public String getMongoId()                           { return mongoId; }
    public void setMongoId(String mongoId)               { this.mongoId = mongoId; }

    public String getProductId()                         { return productId; }
    public void setProductId(String productId)           { this.productId = productId; }

    public String getProductName()                       { return productName; }
    public void setProductName(String productName)       { this.productName = productName; }

    public int getQuantityOrdered()                      { return quantityOrdered; }
    public void setQuantityOrdered(int quantityOrdered)  { this.quantityOrdered = quantityOrdered; }

    public double getUnitPrice()                         { return unitPrice; }
    public void setUnitPrice(double unitPrice)           { this.unitPrice = unitPrice; }

    public double getTotalAmount()                       { return totalAmount; }
    public void setTotalAmount(double totalAmount)       { this.totalAmount = totalAmount; }

    public String getCustomerName()                      { return customerName; }
    public void setCustomerName(String customerName)     { this.customerName = customerName; }

    public LocalDate getOrderDate()                      { return orderDate; }
    public void setOrderDate(LocalDate orderDate)        { this.orderDate = orderDate; }

    public String getStatus()                            { return status; }
    public void setStatus(String status)                 { this.status = status; }

    public String getDeliveryAgentId()                   { return deliveryAgentId; }
    public void setDeliveryAgentId(String id)            { this.deliveryAgentId = id; }

    public boolean isDelivered() {
        return STATUS_DELIVERED.equals(this.status);
    }

    @Override
    public String toString() {
        return String.format(
                "Order{product='%s', customer='%s', qty=%d, total=%.2f, date=%s, status='%s'}",
                productName, customerName, quantityOrdered, totalAmount, orderDate, status
        );
    }
}