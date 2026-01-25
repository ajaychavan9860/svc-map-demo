package com.demo.microservices.notification.service;

import com.demo.microservices.notification.dto.NotificationRequest;
import com.demo.microservices.notification.model.Notification;
import com.demo.microservices.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
    
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id).orElse(null);
    }
    
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserId(userId);
    }
    
    public Notification sendNotification(NotificationRequest request) {
        Notification notification = new Notification(
            request.getUserId(),
            request.getRecipient(),
            request.getSubject(),
            request.getMessage(),
            request.getType()
        );
        
        // Simulate sending notification
        boolean sent = simulateSendNotification(notification);
        if (sent) {
            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } else {
            notification.setStatus(Notification.NotificationStatus.FAILED);
        }
        
        return notificationRepository.save(notification);
    }
    
    public Notification updateNotificationStatus(Long id, Notification.NotificationStatus status) {
        Notification notification = notificationRepository.findById(id).orElse(null);
        if (notification != null) {
            notification.setStatus(status);
            if (status == Notification.NotificationStatus.SENT || status == Notification.NotificationStatus.DELIVERED) {
                notification.setSentAt(LocalDateTime.now());
            }
            return notificationRepository.save(notification);
        }
        return null;
    }
    
    private boolean simulateSendNotification(Notification notification) {
        // Simulate notification sending with 95% success rate
        return Math.random() > 0.05;
    }
}