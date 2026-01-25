package com.example.emailservice.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailListener {

    @Autowired
    private JavaMailSender mailSender;

    @RabbitListener(queues = "email.notification.queue")
    public void handleEmailNotification(String emailMessage) {
        // Process incoming email requests from other services
        System.out.println("Processing email notification: " + emailMessage);
        
        // Send email (simplified)
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("customer@example.com");
            message.setSubject("Notification from Microservices");
            message.setText(emailMessage);
            mailSender.send(message);
            System.out.println("Email sent successfully!");
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}