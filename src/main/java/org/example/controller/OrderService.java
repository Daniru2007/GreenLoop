package org.example.controller;

import org.example.Repository.CustomerOrderRepository;
import org.example.Repository.DeliveryAgentRepository;
import org.example.Repository.EmailServiceRepository;
import org.example.Repository.ProductRepository;
import org.example.Repository.StockAuditRepository;
import org.example.Repository.Impl.CustomerOrderImpl;
import org.example.Repository.Impl.DeliveryAgentImpl;
import org.example.Repository.Impl.EmailServiceImpl;
import org.example.Repository.Impl.ProductRepositoryImpl;
import org.example.Repository.Impl.StockAuditImpl;
import org.example.model.CustomerOrder;
import org.example.model.DeliveryAgent;
import org.example.model.Product;
import org.example.model.StockAudit;

import java.time.LocalDate;
import java.util.List;

public class OrderService {

    private final CustomerOrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final DeliveryAgentRepository agentRepo;
    private final StockAuditRepository auditRepo;
    private final EmailServiceRepository emailService;

    public OrderService() {
        this.orderRepo = new CustomerOrderImpl();
        this.productRepo = new ProductRepositoryImpl();
        this.agentRepo = new DeliveryAgentImpl();
        this.auditRepo = new StockAuditImpl();
        this.emailService = new EmailServiceImpl();
    }

    /**
     * Helper constructor for dependency injection / testing
     */
    public OrderService(CustomerOrderRepository orderRepo, ProductRepository productRepo,
                        DeliveryAgentRepository agentRepo, StockAuditRepository auditRepo,
                        EmailServiceRepository emailService) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.agentRepo = agentRepo;
        this.auditRepo = auditRepo;
        this.emailService = emailService;
    }

    /**
     * Create client order, calculate totals, check and decrement inventory stock, and log audit log.
     */
    public CustomerOrder placeOrder(String productId, int quantityOrdered, String customerName, LocalDate orderDate) {
        try {
            Product product = productRepo.getProductById(productId);
            if (product == null) {
                System.err.println("[OrderService] Product not found: " + productId);
                return null;
            }

            if (product.getStockQuantity() < quantityOrdered) {
                System.err.println("[OrderService] Insufficient stock for " + product.getName() +
                        ". Available: " + product.getStockQuantity() + ", Ordered: " + quantityOrdered);
                return null;
            }

            int quantityBefore = product.getStockQuantity();
            Product updatedProduct = productRepo.updateStock(productId, -quantityOrdered);
            if (updatedProduct == null) {
                System.err.println("[OrderService] Failed to update product stock: " + productId);
                return null;
            }

            // Create initial pending order
            CustomerOrder order = new CustomerOrder(
                    productId,
                    product.getName(),
                    quantityOrdered,
                    product.getPrice(),
                    customerName,
                    orderDate,
                    null
            );
            order.setSupplierName(product.getSupplierName());

            CustomerOrder savedOrder = orderRepo.addOrder(order);
            if (savedOrder != null) {
                // Log Stock Audit
                StockAudit audit = new StockAudit(
                        productId,
                        product.getName(),
                        "SALE",
                        quantityBefore,
                        quantityBefore - quantityOrdered,
                        "Client Order Placed: " + savedOrder.getMongoId()
                );
                auditRepo.addAuditLog(audit);

                // Notify Client (mock email)
                String clientEmail = customerName.toLowerCase().replaceAll("\\s+", "") + "@example.com";
                emailService.sendEmailForClient("no-reply@greenloop.com", clientEmail, savedOrder);
                System.out.println("[OrderService] Order placed successfully: " + savedOrder.getMongoId() +
                        " (Total: $" + savedOrder.getTotalAmount() + ")");
            } else {
                // Rollback stock if order creation fails
                productRepo.updateStock(productId, quantityOrdered);
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
                String clientEmail = order.getCustomerName().toLowerCase().replaceAll("\\s+", "") + "@example.com";
                emailService.sendEmailForClient("no-reply@greenloop.com", clientEmail, order);
                emailService.sendEmailForDeliveryAgent("no-reply@greenloop.com", agent.getEmail(), order);

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
                String clientEmail = order.getCustomerName().toLowerCase().replaceAll("\\s+", "") + "@example.com";
                emailService.sendEmailForClient("no-reply@greenloop.com", clientEmail, order);
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
                // 1. Return stock to inventory
                Product product = productRepo.getProductById(order.getProductId());
                if (product != null) {
                    int quantityBefore = product.getStockQuantity();
                    productRepo.updateStock(order.getProductId(), order.getQuantityOrdered());

                    // Log audit trail
                    StockAudit audit = new StockAudit(
                            order.getProductId(),
                            order.getProductName(),
                            "RESTOCK",
                            quantityBefore,
                            quantityBefore + order.getQuantityOrdered(),
                            "Order Cancelled Restock: " + orderId
                    );
                    auditRepo.addAuditLog(audit);
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
