package org.example.Repository;

import org.example.model.DeliveryAgent;

public interface DeliveryAgentRepository {
    DeliveryAgent addDeliveryAgent(DeliveryAgent da);
    DeliveryAgent updateDeliveryAgent(DeliveryAgent da);
    void deleteDeliveryAgent(String agentId);
    DeliveryAgent getDeliveryAgent(String agentId);
}
