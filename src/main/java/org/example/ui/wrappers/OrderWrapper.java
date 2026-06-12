package org.example.ui.wrappers;

import org.example.model.CustomerOrder;

public class OrderWrapper {
    private final CustomerOrder order;
    public OrderWrapper(CustomerOrder order) { this.order = order; }
    public CustomerOrder getOrder() { return order; }
    @Override
    public String toString() {
        String mongoId = order.getMongoId();
        String displayId = (mongoId != null) ? mongoId.substring(Math.max(0, mongoId.length() - 6)) : "NEW";
        StringBuilder prodSummary = new StringBuilder();
        if (order.getOrderItems() != null) {
            for (int i = 0; i < order.getOrderItems().size(); i++) {
                org.example.model.OrderItem item = order.getOrderItems().get(i);
                if (i > 0) prodSummary.append(", ");
                prodSummary.append(item.getProductName()).append(" (x").append(item.getQuantity()).append(")");
            }
        }
        return String.format("#%s: %s - %s (Total: Rs. %.2f)", 
                displayId,
                order.getCustomerName(), 
                prodSummary.toString(),
                order.getTotalAmount());
    }
}
