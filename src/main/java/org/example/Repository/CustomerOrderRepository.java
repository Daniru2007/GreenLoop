package org.example.Repository;

import org.example.model.CustomerOrder;
import java.util.List;

public interface CustomerOrderRepository {
    CustomerOrder addOrder(CustomerOrder order);
    CustomerOrder updateOrder(CustomerOrder order);
    void deleteOrder(String orderId);
    CustomerOrder getOrderById(String orderId);
    List<CustomerOrder> getAllOrders();
}
