package org.example;

import org.example.Repository.Impl.CustomerOrderImpl;
import org.example.Repository.Impl.DeliveryAgentImpl;
import org.example.controller.OrderService;
import org.example.controller.ProductController;
import org.example.model.CustomerOrder;
import org.example.model.DeliveryAgent;
import org.example.model.Product;
import org.example.model.OrderItem;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderProcessingTest {

    private static ProductController productController;
    private static OrderService orderService;
    private static DeliveryAgentImpl agentRepo;

    private static String productId;
    private static String agentId;
    private static String orderId1;
    private static String orderId2;

    @BeforeAll
    static void setup() {
        productController = new ProductController();
        orderService = new OrderService();
        agentRepo = new DeliveryAgentImpl();

        // Register a test product
        Product p = productController.addProduct(
                "Test Mug",
                "Mugs",
                "Bamboo",
                10.00,
                30,
                "piece",
                "EcoSupply Co",
                5
        );
        assertNotNull(p);
        productId = p.getMongoId();

        // Register a test delivery agent
        DeliveryAgent da = new DeliveryAgent();
        da.setName("Test Rider");
        da.setPhone("0719999999");
        da.setEmail("rider@test.com");
        da.setLicenseNumber("LK-R-123");
        da.setVehicleType("Bike");
        da.setVehiclePlate("WP-XX-1111");
        da.setVehicleModel("Yamaha");
        da.setAvailable(true);

        DeliveryAgent savedAgent = agentRepo.addDeliveryAgent(da);
        assertNotNull(savedAgent);
        agentId = savedAgent.getMongoId();
    }

    @Test
    @Order(1)
    void testPlaceOrder_Success() {
        Product before = productController.getProductById(productId);
        assertEquals(30, before.getStockQuantity());

        CustomerOrder order = orderService.placeOrder(
                productId,
                10,
                "John Doe",
                "john.doe@test.com",
                "0711111111",
                "123 Test Lane, Colombo",
                LocalDate.now()
        );

        assertNotNull(order);
        assertNotNull(order.getMongoId());
        assertEquals(productId, order.getProductId());
        assertEquals("John Doe", order.getCustomerName());
        assertEquals(10, order.getQuantityOrdered());
        assertEquals(100.00, order.getTotalAmount());
        assertEquals("PENDING", order.getStatus());
        assertEquals("EcoSupply Co", order.getSupplierName());
        assertNotNull(order.getClient());
        assertEquals("john.doe@test.com", order.getClient().getEmail());
        assertEquals("0711111111", order.getClient().getPhone());
        assertEquals("123 Test Lane, Colombo", order.getClient().getAddress());

        orderId1 = order.getMongoId();

        Product after = productController.getProductById(productId);
        assertEquals(20, after.getStockQuantity(), "Stock should decrease by 10");
    }

    @Test
    @Order(2)
    void testPlaceOrder_InsufficientStock() {
        Product before = productController.getProductById(productId);
        int currentStock = before.getStockQuantity();

        CustomerOrder order = orderService.placeOrder(
                productId,
                currentStock + 5,
                "Jane Doe",
                LocalDate.now()
        );

        assertNull(order, "Order should fail due to insufficient stock");

        Product after = productController.getProductById(productId);
        assertEquals(currentStock, after.getStockQuantity(), "Stock should remain unchanged");
    }

    @Test
    @Order(3)
    void testAssignDeliveryAgent_Success() {
        DeliveryAgent agentBefore = agentRepo.getDeliveryAgent(agentId);
        assertTrue(agentBefore.isAvailable());

        boolean success = orderService.assignDeliveryAgent(orderId1, agentId);
        assertTrue(success);

        CustomerOrder orderAfter = orderService.getOrderById(orderId1);
        assertEquals("PROCESSING", orderAfter.getStatus());
        assertEquals(agentId, orderAfter.getDeliveryAgentId());

        DeliveryAgent agentAfter = agentRepo.getDeliveryAgent(agentId);
        assertFalse(agentAfter.isAvailable(), "Agent should now be busy");
    }

    @Test
    @Order(4)
    void testAssignDeliveryAgent_AlreadyBusy() {
        // Create another order
        CustomerOrder order2 = orderService.placeOrder(
                productId,
                5,
                "Bob",
                LocalDate.now()
        );
        assertNotNull(order2);
        orderId2 = order2.getMongoId();

        // Attempt to assign the busy agent (should fail)
        boolean success = orderService.assignDeliveryAgent(orderId2, agentId);
        assertFalse(success, "Should fail to assign a busy agent");
    }

    @Test
    @Order(5)
    void testCompleteDelivery_Success() {
        boolean success = orderService.completeDelivery(orderId1);
        assertTrue(success);

        CustomerOrder order = orderService.getOrderById(orderId1);
        assertEquals("DELIVERED", order.getStatus());

        DeliveryAgent agent = agentRepo.getDeliveryAgent(agentId);
        assertTrue(agent.isAvailable(), "Agent should be released and available again");
    }

    @Test
    @Order(6)
    void testCancelOrder_PendingWithStockRefund() {
        Product before = productController.getProductById(productId);
        int stockBefore = before.getStockQuantity(); // should be 15 (30 initial - 10 order1 - 5 order2)

        boolean success = orderService.cancelOrder(orderId2);
        assertTrue(success);

        CustomerOrder order = orderService.getOrderById(orderId2);
        assertEquals("CANCELLED", order.getStatus());

        Product after = productController.getProductById(productId);
        assertEquals(stockBefore + 5, after.getStockQuantity(), "Stock of 5 should be refunded back");
    }

    @Test
    @Order(7)
    void testPlaceMultiProductOrder_SuccessAndCancel() {
        // Register another product
        Product p2 = productController.addProduct(
                "Test Cup",
                "Cups",
                "Glass",
                15.00,
                20,
                "piece",
                "GlassSupply Co",
                3
        );
        assertNotNull(p2);
        String productId2 = p2.getMongoId();

        Product prod1Before = productController.getProductById(productId);
        Product prod2Before = productController.getProductById(productId2);

        java.util.List<OrderItem> items = java.util.List.of(
                new OrderItem(productId, "", 5, 0.0),
                new OrderItem(productId2, "", 5, 0.0)
        );

        CustomerOrder order = orderService.placeOrder(
                items,
                "Jane Multi",
                "jane.multi@test.com",
                "0722222222",
                "456 Multi St, Colombo",
                LocalDate.now()
        );

        assertNotNull(order);
        assertNotNull(order.getMongoId());
        assertEquals(2, order.getOrderItems().size());
        assertEquals(125.00, order.getTotalAmount()); // 5*10 + 5*15 = 50 + 75 = 125
        assertEquals("PENDING", order.getStatus());

        Product prod1After = productController.getProductById(productId);
        Product prod2After = productController.getProductById(productId2);
        assertEquals(prod1Before.getStockQuantity() - 5, prod1After.getStockQuantity());
        assertEquals(prod2Before.getStockQuantity() - 5, prod2After.getStockQuantity());

        boolean success = orderService.cancelOrder(order.getMongoId());
        assertTrue(success);

        Product prod1Refunded = productController.getProductById(productId);
        Product prod2Refunded = productController.getProductById(productId2);
        assertEquals(prod1Before.getStockQuantity(), prod1Refunded.getStockQuantity());
        assertEquals(prod2Before.getStockQuantity(), prod2Refunded.getStockQuantity());

        // Clean up
        new CustomerOrderImpl().deleteOrder(order.getMongoId());
        productController.deleteProduct(productId2);
    }

    @Test
    @Order(8)
    void testCompleteDelivery_WithObjectIdAgentId_Success() {
        // Register another test product
        Product p = productController.addProduct("Test Pro", "Pro", "Wood", 10.00, 10, "piece", "Supply", 2);
        assertNotNull(p);
        String pid = p.getMongoId();

        // Place a pending order
        CustomerOrder order = orderService.placeOrder(pid, 2, "Test Client", LocalDate.now());
        assertNotNull(order);
        String oid = order.getMongoId();

        // Register a test agent
        DeliveryAgent da = new DeliveryAgent();
        da.setName("Test Rider Obj");
        da.setPhone("0719999991");
        da.setEmail("riderobj@test.com");
        da.setLicenseNumber("LK-R-1234");
        da.setVehicleType("Bike");
        da.setVehiclePlate("WP-XX-1112");
        da.setVehicleModel("Yamaha");
        da.setAvailable(true);
        DeliveryAgent savedAgent = agentRepo.addDeliveryAgent(da);
        assertNotNull(savedAgent);
        String aid = savedAgent.getMongoId();

        // Assign delivery agent normally
        boolean successAssign = orderService.assignDeliveryAgent(oid, aid);
        assertTrue(successAssign);

        // Manually update the database order document's delivery_agent_id to be a BSON ObjectId instead of a String, to test legacy/alternate format support
        com.mongodb.client.MongoDatabase db = org.example.config.DBManager.getDatabase();
        db.getCollection("orders").updateOne(
                com.mongodb.client.model.Filters.eq("_id", new org.bson.types.ObjectId(oid)),
                com.mongodb.client.model.Updates.set("delivery_agent_id", new org.bson.types.ObjectId(aid))
        );

        // Complete delivery
        boolean successDeliver = orderService.completeDelivery(oid);
        assertTrue(successDeliver);

        // Check order status
        CustomerOrder updatedOrder = orderService.getOrderById(oid);
        assertEquals("DELIVERED", updatedOrder.getStatus());

        // Check agent availability (should be true/released!)
        DeliveryAgent agent = agentRepo.getDeliveryAgent(aid);
        assertTrue(agent.isAvailable(), "Agent should be released and available again even when delivery_agent_id was stored as ObjectId");

        // Clean up
        new CustomerOrderImpl().deleteOrder(oid);
        agentRepo.deleteDeliveryAgent(aid);
        productController.deleteProduct(pid);
    }

    @AfterAll
    static void teardown() {
        // Clean up testing records
        if (orderId1 != null) {
            orderService.cancelOrder(orderId1); // cleanup helper if needed
            new CustomerOrderImpl().deleteOrder(orderId1);
        }
        if (orderId2 != null) {
            new CustomerOrderImpl().deleteOrder(orderId2);
        }
        if (agentId != null) {
            agentRepo.deleteDeliveryAgent(agentId);
        }
        if (productId != null) {
            productController.deleteProduct(productId);
        }
    }
}
