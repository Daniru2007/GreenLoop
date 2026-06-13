package org.example.controller;

import org.example.Repository.Impl.DeliveryAgentImpl;
import org.example.Repository.DeliveryAgentRepository;
import org.example.model.DeliveryAgent;

import java.util.List;

public class DeliveryAgentController {

    private final DeliveryAgentRepository agentRepo;

    public DeliveryAgentController() {
        this.agentRepo = new DeliveryAgentImpl();
    }

    public DeliveryAgent addAgent(DeliveryAgent agent) {
        if (isNullOrBlank(agent.getName())) {
            System.err.println("[DeliveryAgentController] Name is required.");
            return null;
        }
        if (isNullOrBlank(agent.getLicenseNumber())) {
            System.err.println("[DeliveryAgentController] License number is required.");
            return null;
        }
        if (isNullOrBlank(agent.getVehiclePlate())) {
            System.err.println("[DeliveryAgentController] Vehicle plate is required.");
            return null;
        }
        return agentRepo.addDeliveryAgent(agent);
    }

    public DeliveryAgent updateAgent(DeliveryAgent agent) {
        if (isNullOrBlank(agent.getMongoId())) {
            System.err.println("[DeliveryAgentController] Agent ID is required for update.");
            return null;
        }
        return agentRepo.updateDeliveryAgent(agent);
    }

    public void deleteAgent(String mongoId) {
        if (isNullOrBlank(mongoId)) {
            System.err.println("[DeliveryAgentController] Agent ID is required for delete.");
            return;
        }
        agentRepo.deleteDeliveryAgent(mongoId);
    }

    public DeliveryAgent getAgent(String mongoId) {
        if (isNullOrBlank(mongoId)) {
            System.err.println("[DeliveryAgentController] Agent ID is required.");
            return null;
        }
        return agentRepo.getDeliveryAgent(mongoId);
    }

    public List<DeliveryAgent> getAllAgents() {
        return agentRepo.getAllDeliveryAgents();
    }

    private boolean isNullOrBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}