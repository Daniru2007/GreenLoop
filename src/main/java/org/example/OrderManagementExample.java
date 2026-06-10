package org.example;

import org.example.Repository.Impl.DeliveryAgentImpl;
import org.example.controller.OrderService;
import org.example.controller.ProductController;
import org.example.model.CustomerOrder;
import org.example.model.DeliveryAgent;
import org.example.model.Product;

import javax.swing.SwingUtilities;
import java.time.LocalDate;

/**
 * OrderManagementExample demonstrates how to use the Client Order and Delivery Agent management system
 */
public class OrderManagementExample {
    public static void main(String[] args) {
        if (args.length == 0 || !"cli".equalsIgnoreCase(args[0])) {
            System.out.println("Launching GUI Interface... (Pass 'cli' as an argument to run the console flow instead)");
            SwingUtilities.invokeLater(() -> {
                new OrderManagementGUI().setVisible(true);
            });
            return;
        }

        System.out.println("=====================================================================");
        System.out.println("                GREENLOOP ORDER & DELIVERY SYSTEM                    ");
        System.out.println("=====================================================================");

        ProductController productController = new ProductController();
        OrderService orderService = new OrderService();
        DeliveryAgentImpl agentRepo = new DeliveryAgentImpl();
        org.example.Repository.Impl.CustomerOrderImpl orderRepo = new org.example.Repository.Impl.CustomerOrderImpl();

        // 1. REGISTER NEW PRODUCTS FOR TESTING
        System.out.println("\n1. Registering Products...\n");
        Product mug = productController.addProduct(
                "Eco Coffee Mug",
                "Mugs",
                "Bamboo Fiber",
                12.50,
                50,
                "piece",
                "EcoSupply Co",
                15
        );
        System.out.println("✓ Registered: " + mug.getName() + " | Stock: " + mug.getStockQuantity() + " | Price: $" + mug.getPrice());

        Product bag = productController.addProduct(
                "Reusable Cotton Tote",
                "Bags",
                "Organic Cotton",
                6.99,
                100,
                "piece",
                "ToteMasters Ltd",
                20
        );
        System.out.println("✓ Registered: " + bag.getName() + " | Stock: " + bag.getStockQuantity() + " | Price: $" + bag.getPrice());

        // 2. REGISTER DELIVERY AGENTS
        System.out.println("\n2. Registering Delivery Agents...\n");
        DeliveryAgent agentA = new DeliveryAgent();
        agentA.setName("Kamal Perera");
        agentA.setPhone("0714567890");
        agentA.setEmail("kamal.perera@greenloop.com");
        agentA.setLicenseNumber("LK-K-9812");
        agentA.setVehicleType("TukTuk");
        agentA.setVehiclePlate("WP-QR-1234");
        agentA.setVehicleModel("Bajaj RE");
        agentA.setAvailable(true);

        agentRepo.addDeliveryAgent(agentA);
        System.out.println("✓ Registered Agent: " + agentA.getName() + " | Vehicle: " + agentA.getVehicleType() + " | Available: " + agentA.isAvailable());

        DeliveryAgent agentB = new DeliveryAgent();
        agentB.setName("Nimal Silva");
        agentB.setPhone("0779876543");
        agentB.setEmail("nimal.silva@greenloop.com");
        agentB.setLicenseNumber("LK-N-5543");
        agentB.setVehicleType("Motorbike");
        agentB.setVehiclePlate("WP-BE-9876");
        agentB.setVehicleModel("Yamaha FZ");
        agentB.setAvailable(true);

        agentRepo.addDeliveryAgent(agentB);
        System.out.println("✓ Registered Agent: " + agentB.getName() + " | Vehicle: " + agentB.getVehicleType() + " | Available: " + agentB.isAvailable());

        // 3. PROCESS CLIENT ORDERS
        System.out.println("\n3. Placing Customer Orders...\n");
        System.out.println("Placing order for Reusable Cotton Tote (Qty: 10) for 'Alice Smith'...");
        CustomerOrder order1 = orderService.placeOrder(
                bag.getMongoId(),
                10,
                "Alice Smith",
                LocalDate.now()
        );
        if (order1 != null) {
            System.out.println("✓ Order Placed: " + order1);
            Product updatedBag = productController.getProductById(bag.getMongoId());
            System.out.println("  Remaining stock for Reusable Cotton Tote: " + updatedBag.getStockQuantity() + " (was 100)");
        }

        System.out.println("\nPlacing order for Eco Coffee Mug (Qty: 5) for 'Bob Jones'...");
        CustomerOrder order2 = orderService.placeOrder(
                mug.getMongoId(),
                5,
                "Bob Jones",
                LocalDate.now()
        );
        if (order2 != null) {
            System.out.println("✓ Order Placed: " + order2);
            Product updatedMug = productController.getProductById(mug.getMongoId());
            System.out.println("  Remaining stock for Eco Coffee Mug: " + updatedMug.getStockQuantity() + " (was 50)");
        }

        // 4. ASSIGN DELIVERY AGENTS TO ORDERS
        System.out.println("\n4. Assigning Delivery Agents...\n");
        System.out.println("Assigning Agent '" + agentA.getName() + "' to Alice Smith's order...");
        boolean assign1 = orderService.assignDeliveryAgent(order1.getMongoId(), agentA.getMongoId());
        if (assign1) {
            System.out.println("✓ Assignment Successful!");
            CustomerOrder currentOrder1 = orderService.getOrderById(order1.getMongoId());
            DeliveryAgent currentAgentA = agentRepo.getDeliveryAgent(agentA.getMongoId());
            System.out.println("  Order Status: " + currentOrder1.getStatus() + " (Assigned Agent ID: " + currentOrder1.getDeliveryAgentId() + ")");
            System.out.println("  Agent Availability: " + currentAgentA.isAvailable());
        }

        // Try to assign the same busy agent to Bob's order (should fail)
        System.out.println("\nAttempting to assign the busy Agent '" + agentA.getName() + "' to Bob Jones's order...");
        boolean assign2 = orderService.assignDeliveryAgent(order2.getMongoId(), agentA.getMongoId());
        System.out.println("  Assignment status: " + (assign2 ? "SUCCESS" : "FAILED (Expected)"));

        // Assign available agent B to Bob's order
        System.out.println("Assigning available Agent '" + agentB.getName() + "' to Bob Jones's order...");
        boolean assign3 = orderService.assignDeliveryAgent(order2.getMongoId(), agentB.getMongoId());
        if (assign3) {
            System.out.println("✓ Assignment Successful!");
            CustomerOrder currentOrder2 = orderService.getOrderById(order2.getMongoId());
            DeliveryAgent currentAgentB = agentRepo.getDeliveryAgent(agentB.getMongoId());
            System.out.println("  Order Status: " + currentOrder2.getStatus() + " (Assigned Agent ID: " + currentOrder2.getDeliveryAgentId() + ")");
            System.out.println("  Agent Availability: " + currentAgentB.isAvailable());
        }

        // 5. TRACK AND UPDATE DELIVERY STATUS
        System.out.println("\n5. Completing Deliveries...\n");
        System.out.println("Completing delivery for Alice Smith's order...");
        boolean deliver1 = orderService.completeDelivery(order1.getMongoId());
        if (deliver1) {
            System.out.println("✓ Delivery Completed!");
            CustomerOrder finalOrder1 = orderService.getOrderById(order1.getMongoId());
            DeliveryAgent finalAgentA = agentRepo.getDeliveryAgent(agentA.getMongoId());
            System.out.println("  Order Status: " + finalOrder1.getStatus());
            System.out.println("  Agent Availability: " + finalAgentA.isAvailable() + " (Released for new tasks)");
        }

        // 6. CANCEL AN ORDER (Demonstrate Stock Return Refund)
        System.out.println("\n6. Order Cancellation & Stock Refund...\n");
        System.out.println("Placing a new order for Eco Coffee Mug (Qty: 20) for 'Charlie Brown'...");
        CustomerOrder order3 = orderService.placeOrder(
                mug.getMongoId(),
                20,
                "Charlie Brown",
                LocalDate.now()
        );
        Product mugBeforeCancel = productController.getProductById(mug.getMongoId());
        System.out.println("✓ Order Placed. Mug Stock: " + mugBeforeCancel.getStockQuantity());

        System.out.println("Cancelling Charlie Brown's order to refund stock...");
        boolean cancelStatus = orderService.cancelOrder(order3.getMongoId());
        if (cancelStatus) {
            System.out.println("✓ Order Cancelled!");
            CustomerOrder finalOrder3 = orderService.getOrderById(order3.getMongoId());
            Product mugAfterCancel = productController.getProductById(mug.getMongoId());
            System.out.println("  Cancelled Order Status: " + finalOrder3.getStatus());
            System.out.println("  Mug Stock after refund: " + mugAfterCancel.getStockQuantity() + " (Successfully refunded 20 pieces)");
        }

        // Clean up test data
        System.out.println("\n7. Cleaning Up Test Database Entries...\n");
        orderRepo.deleteOrder(order1.getMongoId());
        orderRepo.deleteOrder(order2.getMongoId());
        orderRepo.deleteOrder(order3.getMongoId());
        agentRepo.deleteDeliveryAgent(agentA.getMongoId());
        agentRepo.deleteDeliveryAgent(agentB.getMongoId());
        productController.deleteProduct(mug.getMongoId());
        productController.deleteProduct(bag.getMongoId());
        System.out.println("✓ Database cleaned up successfully.");

        System.out.println("\n=====================================================================");
        System.out.println("                GREENLOOP DEMONSTRATION COMPLETE                    ");
        System.out.println("=====================================================================");
    }
}
