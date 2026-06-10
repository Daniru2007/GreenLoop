package org.example.Repository;

import org.example.model.CustomerOrder;

public interface EmailServiceRepository {

    public void sendEmailForClient(String sendersEmail, String recepientEmail, CustomerOrder customerOrder);
    public void sendEmailForDeliveryAgent(String sendersEmail, String recepientEmail, CustomerOrder customerOrder);

}
