package org.example.Repository;

import org.example.model.DeliveryAgent;

import java.util.List;

public interface DeliveryAgentRepository {
    DeliveryAgent addDeliveryAgent(DeliveryAgent da);
    DeliveryAgent updateDeliveryAgent(DeliveryAgent da);
    void deleteDeliveryAgent(String agentId);
    DeliveryAgent getDeliveryAgent(String agentId);
    List<DeliveryAgent> getAllDeliveryAgents();
}
