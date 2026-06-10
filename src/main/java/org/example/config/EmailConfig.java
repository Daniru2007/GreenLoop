package org.example.config;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailConfig {
   public static void sendEmail(String sendersEmail,String recipientEmail, String subject, String body ){

       Properties props = new Properties();
       props.put("mail.smtp.auth", "true");
       props.put("mail.smtp.starttls.enable", "true");
       props.put("mail.smtp.host", "smtp.gmail.com");
       props.put("mail.smtp.port", "587");

       // TODO: set the credentials securely
       Session session = Session.getInstance(props, new Authenticator() {
           @Override
           protected PasswordAuthentication getPasswordAuthentication() {
               return new PasswordAuthentication(
                       sendersEmail,
                       "jvpy thfj cnco diog");

           }
       });

       try {
           Message message = new MimeMessage(session);
           message.setFrom(new InternetAddress(sendersEmail));
           message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
           message.setSubject(subject);
           message.setContent(body, "text/html; charset=utf-8");

           Transport.send(message);
           System.out.println("Email sent successfully!");

       } catch (MessagingException e) {
           e.printStackTrace();

       }
   }
}
