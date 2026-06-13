package org.example;

import org.example.controller.DeliveryAgentController;
import org.example.model.DeliveryAgent;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DeliveryAgentControllerTest {

    private static DeliveryAgentController controller;

    @BeforeAll
    static void setup() {
        controller = new DeliveryAgentController();
    }

    // ── Add Validation ────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void testAddAgent_NullName() {
        DeliveryAgent agent = new DeliveryAgent();
        agent.setName(null);
        agent.setLicenseNumber("LK-001");
        agent.setVehiclePlate("WP-1234");

        assertNull(controller.addAgent(agent), "Should return null when name is null");
    }

    @Test
    @Order(2)
    void testAddAgent_BlankName() {
        DeliveryAgent agent = new DeliveryAgent();
        agent.setName("   ");
        agent.setLicenseNumber("LK-001");
        agent.setVehiclePlate("WP-1234");

        assertNull(controller.addAgent(agent), "Should return null when name is blank");
    }

    @Test
    @Order(3)
    void testAddAgent_NullLicense() {
        DeliveryAgent agent = new DeliveryAgent();
        agent.setName("Kamal Perera");
        agent.setLicenseNumber(null);
        agent.setVehiclePlate("WP-1234");

        assertNull(controller.addAgent(agent), "Should return null when license is null");
    }

    @Test
    @Order(4)
    void testAddAgent_BlankLicense() {
        DeliveryAgent agent = new DeliveryAgent();
        agent.setName("Kamal Perera");
        agent.setLicenseNumber("  ");
        agent.setVehiclePlate("WP-1234");

        assertNull(controller.addAgent(agent), "Should return null when license is blank");
    }

    @Test
    @Order(5)
    void testAddAgent_NullVehiclePlate() {
        DeliveryAgent agent = new DeliveryAgent();
        agent.setName("Kamal Perera");
        agent.setLicenseNumber("LK-001");
        agent.setVehiclePlate(null);

        assertNull(controller.addAgent(agent), "Should return null when vehicle plate is null");
    }

    @Test
    @Order(6)
    void testAddAgent_BlankVehiclePlate() {
        DeliveryAgent agent = new DeliveryAgent();
        agent.setName("Kamal Perera");
        agent.setLicenseNumber("LK-001");
        agent.setVehiclePlate("  ");

        assertNull(controller.addAgent(agent), "Should return null when vehicle plate is blank");
    }

    // ── Update Validation ─────────────────────────────────────────────────────

    @Test
    @Order(7)
    void testUpdateAgent_NullMongoId() {
        DeliveryAgent agent = new DeliveryAgent();
        agent.setMongoId(null);
        agent.setName("Kamal Perera");

        assertNull(controller.updateAgent(agent), "Should return null when mongoId is null");
    }

    @Test
    @Order(8)
    void testUpdateAgent_BlankMongoId() {
        DeliveryAgent agent = new DeliveryAgent();
        agent.setMongoId("  ");
        agent.setName("Kamal Perera");

        assertNull(controller.updateAgent(agent), "Should return null when mongoId is blank");
    }

    // ── Delete Validation ─────────────────────────────────────────────────────

    @Test
    @Order(9)
    void testDeleteAgent_NullMongoId() {
        assertDoesNotThrow(() -> controller.deleteAgent(null),
                "Should not throw when mongoId is null");
    }

    @Test
    @Order(10)
    void testDeleteAgent_BlankMongoId() {
        assertDoesNotThrow(() -> controller.deleteAgent("  "),
                "Should not throw when mongoId is blank");
    }

    // ── Get Validation ────────────────────────────────────────────────────────

    @Test
    @Order(11)
    void testGetAgent_NullMongoId() {
        assertNull(controller.getAgent(null),
                "Should return null when mongoId is null");
    }

    @Test
    @Order(12)
    void testGetAgent_BlankMongoId() {
        assertNull(controller.getAgent("  "),
                "Should return null when mongoId is blank");
    }
}