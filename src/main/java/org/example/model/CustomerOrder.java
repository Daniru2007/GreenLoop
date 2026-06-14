package org.example.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomerOrder {

    // Order status constants
    public static final String STATUS_PENDING    = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_DELIVERED  = "DELIVERED";
    public static final String STATUS_CANCELLED  = "CANCELLED";
    public static final String STATUS_DISPATCH   = "DISPATCHED";

    private String mongoId;
    private Client client;
    private List<OrderItem> orderItems = new ArrayList<>();
    private double totalAmount;
    private LocalDate orderDate;
    private String status;
    private String deliveryAgentId;
    private String supplierName;

    public CustomerOrder() {}

    public CustomerOrder(String productId, String productName, int quantityOrdered,
                         double unitPrice, String customerName, LocalDate orderDate,
                         String deliveryAgentId) {
        // Construct Client object
        this.client = new Client(customerName, customerName.toLowerCase().replaceAll("\\s+", "") + "@example.com", "", "");
        // Construct OrderItem and add to list
        this.orderItems = new ArrayList<>();
        this.orderItems.add(new OrderItem(productId, productName, quantityOrdered, unitPrice));
        this.totalAmount     = quantityOrdered * unitPrice;
        this.orderDate       = orderDate;
        this.deliveryAgentId = deliveryAgentId;
        this.status          = STATUS_PENDING; // default on creation
    }

    public CustomerOrder(Client client, List<OrderItem> orderItems, LocalDate orderDate, String deliveryAgentId) {
        this.client = client;
        this.orderItems = orderItems;
        this.orderDate = orderDate;
        this.deliveryAgentId = deliveryAgentId;
        this.status = STATUS_PENDING;
        recalculateTotalAmount();
    }

    public void recalculateTotalAmount() {
        this.totalAmount = 0.0;
        if (orderItems != null) {
            for (OrderItem item : orderItems) {
                this.totalAmount += item.getQuantity() * item.getUnitPrice();
            }
        }
    }

    // Delegation getters/setters for compatibility with GUI and Services
    public String getCustomerName() {
        return client != null ? client.getName() : null;
    }

    public void setCustomerName(String customerName) {
        if (this.client == null) {
            this.client = new Client();
        }
        this.client.setName(customerName);
    }

    public String getProductId() {
        return (orderItems != null && !orderItems.isEmpty()) ? orderItems.get(0).getProductId() : null;
    }

    public void setProductId(String productId) {
        if (orderItems == null) {
            orderItems = new ArrayList<>();
        }
        if (orderItems.isEmpty()) {
            orderItems.add(new OrderItem(productId, "", 0, 0.0));
        } else {
            orderItems.get(0).setProductId(productId);
        }
    }

    public String getProductName() {
        return (orderItems != null && !orderItems.isEmpty()) ? orderItems.get(0).getProductName() : null;
    }

    public void setProductName(String productName) {
        if (orderItems == null) {
            orderItems = new ArrayList<>();
        }
        if (orderItems.isEmpty()) {
            orderItems.add(new OrderItem("", productName, 0, 0.0));
        } else {
            orderItems.get(0).setProductName(productName);
        }
    }

    public int getQuantityOrdered() {
        return (orderItems != null && !orderItems.isEmpty()) ? orderItems.get(0).getQuantity() : 0;
    }

    public void setQuantityOrdered(int quantityOrdered) {
        if (orderItems == null) {
            orderItems = new ArrayList<>();
        }
        if (orderItems.isEmpty()) {
            orderItems.add(new OrderItem("", "", quantityOrdered, 0.0));
        } else {
            orderItems.get(0).setQuantity(quantityOrdered);
        }
        recalculateTotalAmount();
    }

    public double getUnitPrice() {
        return (orderItems != null && !orderItems.isEmpty()) ? orderItems.get(0).getUnitPrice() : 0.0;
    }

    public void setUnitPrice(double unitPrice) {
        if (orderItems == null) {
            orderItems = new ArrayList<>();
        }
        if (orderItems.isEmpty()) {
            orderItems.add(new OrderItem("", "", 0, unitPrice));
        } else {
            orderItems.get(0).setUnitPrice(unitPrice);
        }
        recalculateTotalAmount();
    }

    // Standard getters/setters
    public String getMongoId()                           { return mongoId; }
    public void setMongoId(String mongoId)               { this.mongoId = mongoId; }

    public Client getClient()                            { return client; }
    public void setClient(Client client)                  { this.client = client; }

    public List<OrderItem> getOrderItems()               { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
        recalculateTotalAmount();
    }

    public double getTotalAmount()                       { return totalAmount; }
    public void setTotalAmount(double totalAmount)       { this.totalAmount = totalAmount; }

    public LocalDate getOrderDate()                      { return orderDate; }
    public void setOrderDate(LocalDate orderDate)        { this.orderDate = orderDate; }

    public String getStatus()                            { return status; }
    public void setStatus(String status)                 { this.status = status; }

    public String getDeliveryAgentId()                   { return deliveryAgentId; }
    public void setDeliveryAgentId(String id)            { this.deliveryAgentId = id; }

    public String getSupplierName()                      { return supplierName; }
    public void setSupplierName(String supplierName)    { this.supplierName = supplierName; }

    public boolean isDelivered() {
        return STATUS_DELIVERED.equals(this.status);
    }

    @Override
    public String toString() {
        return String.format(
                "Order{product='%s', customer='%s', qty=%d, total=%.2f, date=%s, status='%s', supplier='%s'}",
                getProductName(), getCustomerName(), getQuantityOrdered(), totalAmount, orderDate, status, supplierName
        );
    }
}
