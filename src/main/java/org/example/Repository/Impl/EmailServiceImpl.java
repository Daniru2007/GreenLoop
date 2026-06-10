package org.example.Repository.Impl;

import org.example.Repository.EmailServiceRepository;
import org.example.config.EmailConfig;
import org.example.model.CustomerOrder;

import javax.swing.*;

public class EmailServiceImpl implements EmailServiceRepository {

    /**
     * Once the order has been dispatched, invoke this method
     *
     * */
    @Override
    public void sendEmailForClient(String sendersEmail, String recepientEmail, CustomerOrder customerOrder) {
           String subject = "Your order has been dispatched! ";
        String body = """
        <!DOCTYPE html>
        <html>
        <body style="margin: 0; padding: 20px; font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f7f6;">
        
            <table width="100%%" border="0" cellspacing="0" cellpadding="0" style="max-width: 500px; margin: auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1);">
                
                <tr>
                    <td style="padding: 30px; text-align: center; background-color: #224a37; color: #ffffff;">
                        <img src=%s alt="" width="100" height="100"`>
                        <h1 style="margin: 0; font-size: 24px; letter-spacing: 1px;">Green Loop</h1>
                    </td>
                </tr>
        
                <tr>
                    <td style="padding: 30px;">
                        <h2 style="color: #224a37; margin-top: 0;">Your Order is on the way!</h2>
                        <p style="color: #555; line-height: 1.6;">Dear Customer,</p>
                        <p style="color: #555; line-height: 1.6;">We are pleased to inform you that your order has been dispatched and is currently in transit. You can expect your package to arrive shortly.</p>
        
                        <div style="background-color: #f0f7f4; padding: 20px; border-radius: 8px; border-left: 5px solid #224a37; margin: 25px 0;">
                            <h3 style="margin-top: 0; color: #224a37; font-size: 18px;">Order Details</h3>
                            <p style="margin: 5px 0; color: #333;"><strong>Order Number:</strong> %s</p>
                            <p style="margin: 5px 0; color: #333;"><strong>Product:</strong> %s</p>
                            <p style="margin: 5px 0; color: #333;"><strong>Quantity:</strong> %s</p>
                            <p style="margin: 5px 0; color: #333;"><strong>Price:</strong>Rs. %s</p>
                        </div>
        
                        <div style="border: 1px solid #e0e0e0; padding: 20px; border-radius: 8px; margin-bottom: 25px;">
                            <h3 style="margin-top: 0; color: #224a37; font-size: 18px;">Delivery Information</h3>
                            <p style="margin: 5px 0; color: #333;"><strong>Tracking Number:</strong> %s</p>
                            <p style="margin: 5px 0; color: #333;"><strong>Vehicle Plate:</strong>%s </p>
                        </div>
        
                        <p style="color: #555; line-height: 1.6;">If you have any questions, please feel free to reach out to our support team.</p>
                        <p style="color: #555; font-weight: bold;">Thank you for choosing Green Loop!</p>
                    </td>
                </tr>
        
                <tr>
                    <td style="padding: 20px; text-align: center; background-color: #f9f9f9; color: #888; font-size: 12px;">
                        &copy; 2026 Green Loop. All rights reserved.
                    </td>
                </tr>
            </table>
        
        </body>
        </html>
        """;

        String formattedBody = String.format(
                body,
                "/org/example/assets/image-removebg-preview.png",
                customerOrder.getMongoId(),
                customerOrder.getProductName(),
                customerOrder.getQuantityOrdered(),
                customerOrder.getTotalAmount(),
                "1234567890", // TODO: get tracking number from delivery agent info
                "ABC-1234" // TODO: get vehicle plate from delivery agent info

        );
        // TODO : This should be temporarily set to DISPATCHED for testing
        /**
         * This should be removed if you send the real status via the constructor
         * **/
        customerOrder.setStatus(customerOrder.STATUS_DISPATCH);

        if(customerOrder.getStatus().equals(customerOrder.STATUS_DISPATCH)){
            EmailConfig.sendEmail(sendersEmail, recepientEmail, subject, formattedBody);
            JOptionPane.showMessageDialog(null, "Email sent successfully to the client!");
        }else{
            JOptionPane.showMessageDialog(null, "Order is not yet dispatched. Email will be sent once the order is dispatched.");
        }


    }

    @Override
    public void sendEmailForDeliveryAgent(String sendersEmail, String recepientEmail, CustomerOrder customerOrder) {
        String subject = "New Delivery Assignment: Order #" + customerOrder.getMongoId();
        String body = """
                <html>
                <body style="margin: 0; padding: 40px; font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f7f6;">
                
                    <table width="100%%" border="0" cellspacing="0" cellpadding="0" style="max-width: 500px; margin: auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1);">
                
                        <tr>
                            <td style="padding: 30px; text-align: center; background-color: #224a37; color: #ffffff; ">
                                <img src=%s alt="" width="100" height="100"`>
                                <h1 style="margin: 0; font-size: 24px; letter-spacing: 1px;">Green Loop</h1>
                            </td>
                        </tr>
                
                        <tr>
                            <td style="padding: 30px;">
                                <h2 style="color: #224a37; margin-top: 0; text-align: center; font-size: 40px;">You have been assigned to a new order!</h2>
                                <p style="color: #555; line-height: 1.6;">Dear %s,</p>
                                <p style="color: #555; line-height: 1.6;">We are pleased to inform you that you have been assigned to a new order.</p>
                
                                <div style="background-color: #f0f7f4; padding: 20px; border-radius: 8px; border-left: 5px solid #224a37; margin: 25px 0;">
                                    <h3 style="margin-top: 0; color: #224a37; font-size: 18px;">Order Details</h3>
                                    <p style="margin: 5px 0; color: #333;"><strong>Order Number:</strong> %s</p>
                                    <p style="margin: 5px 0; color: #333;"><strong>Product:</strong>%s</p>
                                    <p style="margin: 5px 0; color: #333;"><strong>Quantity:</strong> %s</p>
                                    <p style="margin: 5px 0; color: #333;"><strong>Price:</strong>Rs. %s</p>
                                </div>
                
                                <div style="border: 1px solid #e0e0e0; padding: 20px; border-radius: 8px; margin-bottom: 25px;">
                                    <h3 style="margin-top: 0; color: #224a37; font-size: 18px;">Customer Information</h3>
                                    <p style="margin: 5px 0; color: #333;"><strong>Customer Name:</strong> %s</p>
                                    <p style="margin: 5px 0; color: #333;"><strong>Customer Email:</strong> %s</p>
                                    <p style="margin: 5px 0; color: #333;"><strong>Customer Address:</strong> %s</p>
                                    <p style="margin: 5px 0; color: #333;"><strong>Customer Phone:</strong> %s</p>
                
                                </div>
                 <p style="color: #555; line-height: 1.6; margin-bottom: 35px;">If you have any questions, please feel free to reach out to our support team.</p>
                                <p style="color: #555; font-weight: bold;">Best regards,</p>
                                <p style="color: #555; font-weight: bold;">The Green Loop Team</p>
                
                            </td>
                
                        </tr>
                
                        <tr>
                            <td style="padding: 20px; text-align: center; background-color: #f9f9f9; color: #888; font-size: 12px;">
                                &copy; 2026 Green Loop. All rights reserved.
                            </td>
                        </tr>
                    </table>
                
                </body>
                </html>
                """;

         String formattedBody = String.format(
                body,
                "/org/example/assets/image-removebg-preview.png",
                "Kasun Perera",
                customerOrder.getMongoId(),
                customerOrder.getProductName(),
                customerOrder.getQuantityOrdered(),
                customerOrder.getTotalAmount(),
                customerOrder.getCustomerName(),
                "CustomerEmail",
                "CustomerAddress",
                "CustomerPhone"
        );

        customerOrder.setStatus(customerOrder.STATUS_DISPATCH);

        if(customerOrder.getStatus().equals(customerOrder.STATUS_DISPATCH)){
            EmailConfig.sendEmail(sendersEmail, recepientEmail, subject, formattedBody);
            JOptionPane.showMessageDialog(null, "Email sent successfully to the delivery agent!");
        }else{
            JOptionPane.showMessageDialog(null, "Order is not yet dispatched. Email will be sent once the order is dispatched.");
        }


    }
}
