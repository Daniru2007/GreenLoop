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
        return String.format("#%s: %s (%d x %s)", 
                displayId,
                order.getCustomerName(), 
                order.getQuantityOrdered(), 
                order.getProductName());
    }
}
