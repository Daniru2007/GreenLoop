package org.example;

import org.example.Repository.Impl.DeliveryAgentImpl;
import org.example.model.DeliveryAgent;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DeliveryAgentConnectionTest {

    private static DeliveryAgentImpl repo;
    private static String testMongoId;

    @BeforeAll
    static void setup() {
        repo = new DeliveryAgentImpl();
    }

    @Test
    @Order(1)
    void testAddDeliveryAgent() {
        DeliveryAgent da = new DeliveryAgent();
        da.setName("Test Agent");
        da.setPhone("0771234567");
        da.setEmail("test@example.com");
        da.setLicenseNumber("LK-TEST-001");
        da.setVehicleType("Bike");
        da.setVehiclePlate("WP-9999");
        da.setVehicleModel("Honda CB150");
        da.setAvailable(true);

        DeliveryAgent result = repo.addDeliveryAgent(da);

        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getMongoId(), "MongoDB ID should be set after insert");

        testMongoId = result.getMongoId(); // save for later tests
        System.out.println("Added agent with ID: " + testMongoId);
    }

    @Test
    @Order(2)
    void testGetDeliveryAgent() {
        assertNotNull(testMongoId, "testMongoId must be set by addAgent test");

        DeliveryAgent result = repo.getDeliveryAgent(testMongoId);

        assertNotNull(result, "Should find the agent we just added");
        assertEquals("Test Agent",   result.getName());
        assertEquals("0771234567",   result.getPhone());
        assertEquals("LK-TEST-001",  result.getLicenseNumber());
        assertEquals("WP-9999",      result.getVehiclePlate());
        assertTrue(result.isAvailable());

        System.out.println("Fetched: " + result.getName());
    }

    @Test
    @Order(3)
    void testUpdateDeliveryAgent() {
        assertNotNull(testMongoId, "testMongoId must be set by addAgent test");

        DeliveryAgent da = new DeliveryAgent();
        da.setMongoId(testMongoId);
        da.setName("Updated Agent");
        da.setPhone("0789999999");
        da.setEmail("updated@example.com");
        da.setLicenseNumber("LK-TEST-001");
        da.setVehicleType("Van");
        da.setVehiclePlate("WP-9999");
        da.setVehicleModel("Toyota KDH");
        da.setAvailable(false);

        DeliveryAgent result = repo.updateDeliveryAgent(da);

        assertNotNull(result, "Result should not be null");
        assertEquals("Updated Agent", result.getName());
        assertEquals("Van",           result.getVehicleType());
        assertFalse(result.isAvailable());

        // verify update actually persisted in DB
        DeliveryAgent fromDb = repo.getDeliveryAgent(testMongoId);
        assertEquals("Updated Agent", fromDb.getName());
        assertEquals("Toyota KDH",    fromDb.getVehicleModel());

        System.out.println("Updated agent: " + fromDb.getName());
    }

    @Test
    @Order(4)
    void testDeleteDeliveryAgent() {
        assertNotNull(testMongoId, "testMongoId must be set by addAgent test");

        repo.deleteDeliveryAgent(testMongoId);

        // confirm it's gone
        DeliveryAgent result = repo.getDeliveryAgent(testMongoId);
        assertNull(result, "Agent should be null after deletion");

        System.out.println("Deleted agent with ID: " + testMongoId);
    }

    @Test
    @Order(5)
    void testGetNonExistentAgent() {
        DeliveryAgent result = repo.getDeliveryAgent("000000000000000000000000");
        assertNull(result, "Should return null for non-existent agent");
    }

    @Test
    @Order(6)
    void testDeleteNonExistentAgent() {
        assertDoesNotThrow(() -> repo.deleteDeliveryAgent("000000000000000000000000"));
    }
}