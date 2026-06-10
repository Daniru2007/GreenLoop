package org.example;

import org.example.Repository.EmailServiceRepository;
import org.example.Repository.Impl.EmailServiceImpl;
import org.example.model.CustomerOrder;
import org.junit.jupiter.api.Test;

public class EmailTest {
   private static EmailServiceRepository emailServiceRepository = new EmailServiceImpl();

   @Test
    public void testSendEmailForClient(){
         String sendersEmail = "sithijahiripitiya16@gmail.com";
            String recepientEmail = "sithijahiripitiya16@gmail.com";
       CustomerOrder customerOrder = new CustomerOrder();
           customerOrder.setMongoId("1234567890");
           customerOrder.setProductName("Test Product");
              customerOrder.setQuantityOrdered(2);
                customerOrder.setTotalAmount(39.98);
                emailServiceRepository.sendEmailForClient(sendersEmail, recepientEmail, customerOrder);


   }

   @Test
   public void testEmailForDeliveryAgent(){
       String sendersEmail = "sithijahiripitiya16@gmail.com";
         String recepientEmail = "sithijahiripitiya16@gmail.com";
         CustomerOrder customerOrder = new CustomerOrder();
       customerOrder.setProductName("Test Product");
       customerOrder.setQuantityOrdered(2);
       customerOrder.setTotalAmount(39.98);
         emailServiceRepository.sendEmailForDeliveryAgent(sendersEmail, recepientEmail, customerOrder);

   }
}
