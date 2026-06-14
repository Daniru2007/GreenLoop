package org.example.utils;

import org.example.Repository.DeliveryAgentRepository;
import org.example.Repository.ProductRepository;
import org.example.Repository.Impl.DeliveryAgentImpl;
import org.example.Repository.Impl.ProductRepositoryImpl;
import org.example.config.EmailConfig;
import org.example.model.CustomerOrder;
import org.example.model.DeliveryAgent;
import org.example.model.OrderItem;

import java.awt.GraphicsEnvironment;
import javax.swing.*;

public class EmailUtils {
    private static DeliveryAgentRepository deliveryAgentRepository = new DeliveryAgentImpl();
    private static ProductRepository productRepository = new ProductRepositoryImpl();
    private static OrderItem orderItem;

    private static boolean lastEmailClientSuccess = true;
    private static boolean lastEmailAgentSuccess = true;

    public static boolean isLastEmailClientSuccess() {
        return lastEmailClientSuccess;
    }
    public static boolean isLastEmailAgentSuccess() {
        return lastEmailAgentSuccess;
    }

    private static String getLogoBase64() {
        try {
            try (java.io.InputStream is = EmailUtils.class.getResourceAsStream("/org/example/assets/image-removebg-preview.png")) {
                if (is != null) {
                    byte[] bytes = is.readAllBytes();
                    return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(bytes);
                }
            }
        } catch (Exception e) {
            // Ignore, try direct file load
        }
        try {
            java.io.File file = new java.io.File("src/main/java/org/example/assets/image-removebg-preview.png");
            if (file.exists()) {
                byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
                return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(bytes);
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }

    public static void sendEmailForDeliveryAgent(String recepientEmail, CustomerOrder customerOrder) {
        String subject = "New Delivery Assignment: Order #" + customerOrder.getMongoId();
        String body = """
                <html>
                                <body style="margin: 0; padding: 40px; font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f7f6;">
                
                                    <table width="100%%" border="0" cellspacing="0" cellpadding="0" style="max-width: 500px; margin: auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1);">
                
                                        <tr>
                                            <td style="padding: 30px; text-align: center; background-color: #224a37; color: #ffffff; ">
                                                <img src="%s" alt="Green Loop Logo" width="100" height="100">
                                                <h1 style="margin: 0; font-size: 15px; letter-spacing: 1px;">Green Loop</h1>
                                            </td>
                                        </tr>
                
                                        <tr>
                                            <td style="padding: 30px;">
                                                <h2 style="color: #224a37; margin-top: 0; text-align: center; font-size: 40px;">You have been assigned to a new order!</h2>
                                                <p style="color: #555; line-height: 1.6;">Dear %s,</p>
                                                <p style="color: #555; line-height: 1.6;">We are pleased to inform you that you have been assigned to a new order.</p>
                
                
                                                   <table border="0" width="100%%" style="border-collapse: collapse; margin-bottom: 25px;">
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



        String agentName = "Delivery Agent";
        if (customerOrder.getDeliveryAgentId() != null) {
            DeliveryAgent agent = deliveryAgentRepository.getDeliveryAgent(customerOrder.getDeliveryAgentId());
            if (agent != null) {
                agentName = agent.getName();
            }
        }

        String clientName = customerOrder.getCustomerName() != null ? customerOrder.getCustomerName() : "Customer";
        String clientEmail = "";
        String clientAddress = "";
        String clientPhone = "";

        if (customerOrder.getClient() != null) {
            clientName = customerOrder.getClient().getName();
            clientEmail = customerOrder.getClient().getEmail();
            clientAddress = customerOrder.getClient().getAddress();
            clientPhone = customerOrder.getClient().getPhone();
        }

        String formattedBody = String.format(
                 body,
                 getLogoBase64(),
                 agentName,
                orderItemListToHtmlList(customerOrder),
                clientName,
                clientEmail,
                clientAddress,
                clientPhone
        );

        boolean isSend = EmailConfig.sendEmail(recepientEmail, subject, formattedBody);
        lastEmailAgentSuccess = isSend;
        if (!isSend) {
            System.err.println("[EmailUtils] Failed to send email to delivery agent: " + recepientEmail);
            if (shouldShowDialogs()) {
                JOptionPane.showMessageDialog(null, "Failed to send email to delivery agent!", "Email Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("[EmailUtils] Email sent to delivery agent successfully: " + recepientEmail);
            if (shouldShowDialogs()) {
                JOptionPane.showMessageDialog(null, "Email sent to delivery agent successfully!", "Email Sent", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }




    public static void sendEmailForClient(String recepientEmail, CustomerOrder customerOrder) {
        String subject;
        String heading;
        String messageBody;
        
        if (customerOrder.getStatus() == null || "PENDING".equalsIgnoreCase(customerOrder.getStatus())) {
            subject = "Order Confirmation - Order #" + (customerOrder.getMongoId() != null ? customerOrder.getMongoId() : "N/A");
            heading = "Your order has been placed successfully!";
            messageBody = "Thank you for shopping with Green Loop. Your order has been received and is currently pending processing and delivery agent assignment. We will update you as soon as your order is dispatched.";
        } else {
            subject = "Your order has been dispatched!";
            heading = "Your Order is on the way!";
            messageBody = "We are pleased to inform you that your order has been dispatched and is currently in transit. You can expect your package to arrive shortly.";
        }

        String body = """
        <!DOCTYPE html>
        <html>
        <body style="margin: 0; padding: 20px; font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f7f6;">
        
            <table width="100%%" border="0" cellspacing="0" cellpadding="0" style="max-width: 500px; margin: auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1);">
                
                <tr>
                    <td style="padding: 30px; text-align: center; background-color: #224a37; color: #ffffff;">
                        <img src="%s" alt="Green Loop Logo" width="100" height="100">
                        <h1 style="margin: 0; font-size: 24px; letter-spacing: 1px;">Green Loop</h1>
                    </td>
                </tr>
        
                <tr>
                    <td style="padding: 30px;">
                        <h2 style="color: #224a37; margin-top: 0;">%s</h2>
                        <p style="color: #555; line-height: 1.6;">Dear Customer,</p>
                        <p style="color: #555; line-height: 1.6;">%s</p>
        
                        <table border="0" width="100%%" style="border-collapse: collapse; margin-bottom: 25px;">
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

        String agentPlate = "Not Assigned Yet";
        if (customerOrder.getDeliveryAgentId() != null) {
            DeliveryAgent agent = deliveryAgentRepository.getDeliveryAgent(customerOrder.getDeliveryAgentId());
            if (agent != null && agent.getVehiclePlate() != null) {
                agentPlate = agent.getVehiclePlate();
            }
        }

        String result = String.format(
                body,
                getLogoBase64(),
                heading,
                messageBody,
                orderItemListToHtmlList(customerOrder),
                customerOrder.getMongoId() != null ? customerOrder.getMongoId() : "N/A",
                agentPlate
        );

        boolean isSend = EmailConfig.sendEmail(recepientEmail, subject, result);
        lastEmailClientSuccess = isSend;
        if (!isSend) {
            System.err.println("[EmailUtils] Failed to send email to client: " + recepientEmail);
            if (shouldShowDialogs()) {
                JOptionPane.showMessageDialog(null, "Failed to send email to client!", "Email Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("[EmailUtils] Email sent to client successfully: " + recepientEmail);
            if (shouldShowDialogs()) {
                JOptionPane.showMessageDialog(null, "Email sent to client successfully!", "Email Sent", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private static String orderItemListToHtmlList(CustomerOrder customerOrder) {
        StringBuilder tblView = new StringBuilder();

        for (OrderItem item : customerOrder.getOrderItems()) {
            String category = "N/A";
            try {
                org.example.model.Product p = productRepository.getProductById(item.getProductId());
                if (p != null && p.getCategory() != null) {
                    category = p.getCategory();
                }
            } catch (Exception e) {
                // Ignore
            }
            double totalPrice = item.getQuantity() * item.getUnitPrice();
            tblView.append("<tr>")
                    .append("<td style=\"padding: 10px; border-bottom: 1px solid #eee;\">").append(item.getProductId()).append("</td>")
                    .append("<td style=\"padding: 10px; border-bottom: 1px solid #eee;\">").append(item.getProductName()).append("</td>")
                    .append("<td style=\"padding: 10px; border-bottom: 1px solid #eee;\">").append(category).append("</td>")
                    .append("<td style=\"padding: 10px; border-bottom: 1px solid #eee;\">").append(item.getQuantity()).append("</td>")
                    .append("<td style=\"padding: 10px; border-bottom: 1px solid #eee;\">Rs. ").append(totalPrice).append("</td>")
                    .append("</tr>");
        }

        return tblView.toString();
    }

    private static boolean shouldShowDialogs() {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return false;
        }
        String classPath = System.getProperty("java.class.path", "");
        if (classPath.contains("junit") || classPath.contains("surefire")) {
            return false;
        }
        boolean hasWindow = false;
        for (java.awt.Window w : java.awt.Window.getWindows()) {
            if (w.isShowing()) {
                hasWindow = true;
                break;
            }
        }
        return hasWindow;
    }

}


