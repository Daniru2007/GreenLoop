package org.example.controller;

import org.example.Repository.CustomerOrderRepository;
import org.example.Repository.DeliveryAgentRepository;
import org.example.Repository.ProductRepository;
import org.example.Repository.StockAuditRepository;
import org.example.Repository.Impl.CustomerOrderImpl;
import org.example.Repository.Impl.DeliveryAgentImpl;
import org.example.Repository.Impl.ProductRepositoryImpl;
import org.example.Repository.Impl.StockAuditImpl;
import org.example.model.Client;
import org.example.model.CustomerOrder;
import org.example.model.DeliveryAgent;
import org.example.model.Product;
import org.example.model.StockAudit;
import org.example.model.OrderItem;
import org.example.utils.EmailUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private final CustomerOrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final DeliveryAgentRepository agentRepo;
    private final StockAuditRepository auditRepo;

    public OrderService() {
        this.orderRepo = new CustomerOrderImpl();
        this.productRepo = new ProductRepositoryImpl();
        this.agentRepo = new DeliveryAgentImpl();
        this.auditRepo = new StockAuditImpl();
    }

    /**
     * Helper constructor for dependency injection / testing
     */
    public OrderService(CustomerOrderRepository orderRepo, ProductRepository productRepo,
                        DeliveryAgentRepository agentRepo, StockAuditRepository auditRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.agentRepo = agentRepo;
        this.auditRepo = auditRepo;
    }

    /**
     * Backward-compatible placeOrder for 4 arguments.
     */
    public CustomerOrder placeOrder(String productId, int quantityOrdered, String customerName, LocalDate orderDate) {
        String customerEmail = customerName.toLowerCase().replaceAll("\\s+", "") + "@example.com";
        return placeOrder(productId, quantityOrdered, customerName, customerEmail, "", "", orderDate);
    }

    /**
     * Create client order, calculate totals, check and decrement inventory stock, and log audit log.
     */
    public CustomerOrder placeOrder(String productId, int quantityOrdered, String customerName, 
                                     String customerEmail, String customerPhone, String customerAddress, 
                                     LocalDate orderDate) {
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(productId, "", quantityOrdered, 0.0));
        return placeOrder(items, customerName, customerEmail, customerPhone, customerAddress, orderDate);
    }

    /**
     * Create client order with multiple products, check and decrement stock for all products,
     * record audit logs, save order, and email client.
     */
    public CustomerOrder placeOrder(List<OrderItem> items, String customerName, 
                                     String customerEmail, String customerPhone, String customerAddress, 
                                     LocalDate orderDate) {
        try {
            // First pass: Validate stock for all products
            for (OrderItem item : items) {
                Product product = productRepo.getProductById(item.getProductId());
                if (product == null) {
                    System.err.println("[OrderService] Product not found: " + item.getProductId());
                    return null;
                }
                if (product.getStockQuantity() < item.getQuantity()) {
                    System.err.println("[OrderService] Insufficient stock for " + product.getName() +
                            ". Available: " + product.getStockQuantity() + ", Ordered: " + item.getQuantity());
                    return null;
                }
            }

            // Populate unit prices and product names from DB, and decrement stock
            List<OrderItem> populatedItems = new ArrayList<>();
            List<Product> rolledBackProducts = new ArrayList<>();
            List<Integer> rolledBackQuantities = new ArrayList<>();
            boolean success = true;

            for (OrderItem item : items) {
                Product product = productRepo.getProductById(item.getProductId());
                int quantityBefore = product.getStockQuantity();
                Product updatedProduct = productRepo.updateStock(item.getProductId(), -item.getQuantity());
                if (updatedProduct == null) {
                    System.err.println("[OrderService] Failed to update product stock: " + item.getProductId());
                    success = false;
                    break;
                }
                rolledBackProducts.add(product);
                rolledBackQuantities.add(item.getQuantity());

                populatedItems.add(new OrderItem(
                        item.getProductId(),
                        product.getName(),
                        item.getQuantity(),
                        product.getPrice()
                ));
            }

            if (!success) {
                // Rollback stock for successfully decremented items
                for (int i = 0; i < rolledBackProducts.size(); i++) {
                    productRepo.updateStock(rolledBackProducts.get(i).getMongoId(), rolledBackQuantities.get(i));
                }
                return null;
            }

            // Create initial pending order
            CustomerOrder order = new CustomerOrder();
            order.setClient(new Client(customerName, customerEmail, customerPhone, customerAddress));
            order.setOrderItems(populatedItems);
            order.setOrderDate(orderDate);
            order.setStatus(CustomerOrder.STATUS_PENDING);
            if (!rolledBackProducts.isEmpty()) {
                order.setSupplierName(rolledBackProducts.get(0).getSupplierName());
            }

            CustomerOrder savedOrder = orderRepo.addOrder(order);
            if (savedOrder != null) {
                // Log Stock Audit for each product
                for (int i = 0; i < rolledBackProducts.size(); i++) {
                    Product prod = rolledBackProducts.get(i);
                    int qty = rolledBackQuantities.get(i);
                    int qtyBefore = prod.getStockQuantity();
                    StockAudit audit = new StockAudit(
                            prod.getMongoId(),
                            prod.getName(),
                            "SALE",
                            qtyBefore,
                            qtyBefore - qty,
                            "Client Order Placed: " + savedOrder.getMongoId()
                    );
                    auditRepo.addAuditLog(audit);
                }

                // Notify Client
                EmailUtils.sendEmailForClient("sithijahiripitiya16@gmail.com", customerEmail, savedOrder);
                System.out.println("[OrderService] Order placed successfully: " + savedOrder.getMongoId() +
                        " (Total: $" + savedOrder.getTotalAmount() + ")");
            } else {
                // Rollback all stock if order creation fails
                for (int i = 0; i < rolledBackProducts.size(); i++) {
                    productRepo.updateStock(rolledBackProducts.get(i).getMongoId(), rolledBackQuantities.get(i));
                }
                System.err.println("[OrderService] Failed to save order. Stock rolled back.");
            }

            return savedOrder;
        } catch (Exception e) {
            System.err.println("[OrderService] placeOrder error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Assign an available delivery agent to a pending order and transition status to PROCESSING.
     */
    public boolean assignDeliveryAgent(String orderId, String agentId) {
        try {
            CustomerOrder order = orderRepo.getOrderById(orderId);
            if (order == null) {
                System.err.println("[OrderService] Order not found: " + orderId);
                return false;
            }

            if (!CustomerOrder.STATUS_PENDING.equals(order.getStatus())) {
                System.err.println("[OrderService] Order status must be PENDING to assign agent. Current: " + order.getStatus());
                return false;
            }

            DeliveryAgent agent = agentRepo.getDeliveryAgent(agentId);
            if (agent == null) {
                System.err.println("[OrderService] Delivery agent not found: " + agentId);
                return false;
            }

            if (!agent.isAvailable()) {
                System.err.println("[OrderService] Delivery agent is not available: " + agent.getName());
                return false;
            }

            // Update order details
            order.setDeliveryAgentId(agentId);
            order.setStatus(CustomerOrder.STATUS_PROCESSING);

            // Update agent details
            agent.setAvailable(false);

            CustomerOrder updatedOrder = orderRepo.updateOrder(order);
            DeliveryAgent updatedAgent = agentRepo.updateDeliveryAgent(agent);

            if (updatedOrder != null && updatedAgent != null) {
                // Notify both
                String clientEmail = (order.getClient() != null && order.getClient().getEmail() != null && !order.getClient().getEmail().isEmpty())
                        ? order.getClient().getEmail()
                        : order.getCustomerName().toLowerCase().replaceAll("\\s+", "") + "@example.com";
                EmailUtils.sendEmailForClient("sithijahiripitiya16@gmail.com", clientEmail, order);
                EmailUtils.sendEmailForDeliveryAgent("sithijahiripitiya16@gmail.com", agent.getEmail(), order);

                System.out.println("[OrderService] Agent " + agent.getName() + " assigned to order " + orderId);
                return true;
            }

            return false;
        } catch (Exception e) {
            System.err.println("[OrderService] assignDeliveryAgent error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Complete order delivery, transition status to DELIVERED, and release the delivery agent.
     */
    public boolean completeDelivery(String orderId) {
        try {
            CustomerOrder order = orderRepo.getOrderById(orderId);
            if (order == null) {
                System.err.println("[OrderService] Order not found: " + orderId);
                return false;
            }

            if (CustomerOrder.STATUS_DELIVERED.equals(order.getStatus())) {
                return true;
            }

            if (!CustomerOrder.STATUS_PROCESSING.equals(order.getStatus())) {
                System.err.println("[OrderService] Order must be in PROCESSING state to deliver. Current: " + order.getStatus());
                return false;
            }

            // Update order status
            order.setStatus(CustomerOrder.STATUS_DELIVERED);
            CustomerOrder updatedOrder = orderRepo.updateOrder(order);

            // Release agent
            String agentId = order.getDeliveryAgentId();
            if (agentId != null) {
                DeliveryAgent agent = agentRepo.getDeliveryAgent(agentId);
                if (agent != null) {
                    agent.setAvailable(true);
                    agentRepo.updateDeliveryAgent(agent);
                }
            }

            if (updatedOrder != null) {
                String clientEmail = (order.getClient() != null && order.getClient().getEmail() != null && !order.getClient().getEmail().isEmpty())
                        ? order.getClient().getEmail()
                        : order.getCustomerName().toLowerCase().replaceAll("\\s+", "") + "@example.com";
                EmailUtils.sendEmailForClient("sithijahiripitiya16@gmail.com", clientEmail, order);
                System.out.println("[OrderService] Order " + orderId + " delivered successfully!");
                return true;
            }

            return false;
        } catch (Exception e) {
            System.err.println("[OrderService] completeDelivery error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cancel client order, return reserved inventory stock, log adjustment audit log, and release assigned agent.
     */
    public boolean cancelOrder(String orderId) {
        try {
            CustomerOrder order = orderRepo.getOrderById(orderId);
            if (order == null) {
                System.err.println("[OrderService] Order not found: " + orderId);
                return false;
            }

            if (CustomerOrder.STATUS_CANCELLED.equals(order.getStatus()) ||
                    CustomerOrder.STATUS_DELIVERED.equals(order.getStatus())) {
                System.err.println("[OrderService] Cannot cancel order in " + order.getStatus() + " status.");
                return false;
            }

            String oldStatus = order.getStatus();

            // Transition status
            order.setStatus(CustomerOrder.STATUS_CANCELLED);
            CustomerOrder updatedOrder = orderRepo.updateOrder(order);

            if (updatedOrder != null) {
                // 1. Return stock to inventory for all items
                if (order.getOrderItems() != null) {
                    for (OrderItem item : order.getOrderItems()) {
                        Product product = productRepo.getProductById(item.getProductId());
                        if (product != null) {
                            int quantityBefore = product.getStockQuantity();
                            productRepo.updateStock(item.getProductId(), item.getQuantity());

                            // Log audit trail
                            StockAudit audit = new StockAudit(
                                    item.getProductId(),
                                    item.getProductName(),
                                    "RESTOCK",
                                    quantityBefore,
                                    quantityBefore + item.getQuantity(),
                                    "Order Cancelled Restock: " + orderId
                            );
                            auditRepo.addAuditLog(audit);
                        }
                    }
                }

                // 2. Release agent if assigned
                if (CustomerOrder.STATUS_PROCESSING.equals(oldStatus) && order.getDeliveryAgentId() != null) {
                    DeliveryAgent agent = agentRepo.getDeliveryAgent(order.getDeliveryAgentId());
                    if (agent != null) {
                        agent.setAvailable(true);
                        agentRepo.updateDeliveryAgent(agent);
                    }
                }

                System.out.println("[OrderService] Order " + orderId + " has been cancelled.");
                return true;
            }

            return false;
        } catch (Exception e) {
            System.err.println("[OrderService] cancelOrder error: " + e.getMessage());
            return false;
        }
    }

    public CustomerOrder getOrderById(String orderId) {
        return orderRepo.getOrderById(orderId);
    }

    public List<CustomerOrder> getAllOrders() {
        return orderRepo.getAllOrders();
    }
}
