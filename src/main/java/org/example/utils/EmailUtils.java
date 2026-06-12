package org.example.utils;

import org.example.Repository.DeliveryAgentRepository;
import org.example.Repository.Impl.DeliveryAgentImpl;
import org.example.config.EmailConfig;
import org.example.model.CustomerOrder;
import org.example.model.DeliveryAgent;
import org.example.model.OrderItem;

import javax.swing.*;

public class EmailUtils {
    private static DeliveryAgentRepository deliveryAgentRepository = new DeliveryAgentImpl();
    private static OrderItem orderItem;

    public static void sendEmailForDeliveryAgent(String sendersEmail, String recepientEmail, CustomerOrder customerOrder) {
        String subject = "New Delivery Assignment: Order #" + customerOrder.getMongoId();
        String body = """
                <html>
                                <body style="margin: 0; padding: 40px; font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f7f6;">
                
                                    <table width="100%%" border="0" cellspacing="0" cellpadding="0" style="max-width: 500px; margin: auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1);">
                
                                        <tr>
                                            <td style="padding: 30px; text-align: center; background-color: #224a37; color: #ffffff; ">
                                                <img src=%s alt="" width="100" height="100"`>
                                                <h1 style="margin: 0; font-size: 15px; letter-spacing: 1px;">Green Loop</h1>
                                            </td>
                                        </tr>
                
                                        <tr>
                                            <td style="padding: 30px;">
                                                <h2 style="color: #224a37; margin-top: 0; text-align: center; font-size: 40px;">You have been assigned to a new order!</h2>
                                                <p style="color: #555; line-height: 1.6;">Dear %s,</p>
                                                <p style="color: #555; line-height: 1.6;">We are pleased to inform you that you have been assigned to a new order.</p>
                
                
                                                   <table border="0" width="100%" style="border-collapse: collapse; margin-bottom: 25px;">
                    <thead>
                        <tr style="background-color: #f4f4f4; text-align: left;">
                            <th style="padding: 10px; border-bottom: 2px solid #ddd;">Product Id</th>
                            <th style="padding: 10px; border-bottom: 2px solid #ddd;">Product Name</th>
                            <th style="padding: 10px; border-bottom: 2px solid #ddd;">Category</th>
                            <th style="padding: 10px; border-bottom: 2px solid #ddd;">Qty</th>
                            <th style="padding: 10px; border-bottom: 2px solid #ddd;">Price</th>
                        </tr>
                    </thead>
                    <tbody>
                
                        %s
                    </tbody>
                </table>
                
                
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
                deliveryAgentRepository.getDeliveryAgent(customerOrder.getDeliveryAgentId()).getName(),
                orderItemListToHtmlList(customerOrder),
                customerOrder.getClient().getName(),
                customerOrder.getClient().getName(),
                customerOrder.getClient().getAddress(),
                customerOrder.getClient().getPhone()
        );

       boolean isSend = EmailConfig.sendEmail(sendersEmail, recepientEmail, subject, formattedBody);
        if(!isSend){
            JOptionPane.showMessageDialog(null, "Failed to send email to delivery agent!", "Email Error", JOptionPane.ERROR_MESSAGE);
        }else{
            JOptionPane.showMessageDialog(null, "Email sent to delivery agent successfully!", "Email Sent", JOptionPane.INFORMATION_MESSAGE);
        }





    }




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
        
                        <table border="0" width="100%" style="border-collapse: collapse; margin-bottom: 25px;">
                    <thead>
                        <tr style="background-color: #f4f4f4; text-align: left;">
                            <th style="padding: 10px; border-bottom: 2px solid #ddd;">Product Id</th>
                            <th style="padding: 10px; border-bottom: 2px solid #ddd;">Product Name</th>
                            <th style="padding: 10px; border-bottom: 2px solid #ddd;">Category</th>
                            <th style="padding: 10px; border-bottom: 2px solid #ddd;">Qty</th>
                            <th style="padding: 10px; border-bottom: 2px solid #ddd;">Price</th>
                        </tr>
                    </thead>
                    <tbody>
                
                        %s
                    </tbody>
                </table>
        
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

        String result = String.format(
           body,
                "/org/example/assets/image-removebg-preview.png",
                orderItemListToHtmlList(customerOrder),
                customerOrder.getMongoId(),
                deliveryAgentRepository.getDeliveryAgent(customerOrder.getDeliveryAgentId()).getVehiclePlate()
        );

        boolean isSend = EmailConfig.sendEmail(sendersEmail, recepientEmail, subject, result);
        if(!isSend){
            JOptionPane.showMessageDialog(null, "Failed to send email to client!", "Email Error", JOptionPane.ERROR_MESSAGE);
        }else{
            JOptionPane.showMessageDialog(null, "Email sent to client successfully!", "Email Sent", JOptionPane.INFORMATION_MESSAGE);
        }




    }

    private static String orderItemListToHtmlList(CustomerOrder customerOrder) {
        StringBuilder tblView = new StringBuilder();

        for (OrderItem item : customerOrder.getOrderItems()) {
            tblView.append("<tr>")
                    .append("<td style=\"padding: 10px; border-bottom: 1px solid #eee;\">").append(item.getProductId()).append("</td>")
                    .append("<td style=\"padding: 10px; border-bottom: 1px solid #eee;\">").append(item.getProductName()).append("</td>")
                    .append("<td style=\"padding: 10px; border-bottom: 1px solid #eee;\">").append(item.getQuantity()).append("</td>")
                    .append("<td style=\"padding: 10px; border-bottom: 1px solid #eee;\">Rs. ").append(item.getUnitPrice()).append("</td>")
                    .append("</tr>");
        }

        return tblView.toString();
    }

}


