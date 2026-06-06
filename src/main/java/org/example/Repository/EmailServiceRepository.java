package org.example.Repository;

public interface EmailServiceRepository {
    public void sendEmailForClient(String clientEmail);
    public void sendEmailForDeliveryAgent(String deliveryAgentEmail);
}
