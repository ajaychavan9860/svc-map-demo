package com.demo.microservices.notification.controller;

import com.demo.microservices.notification.dto.NotificationRequest;
import com.demo.microservices.notification.model.Notification;
import com.demo.microservices.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @GetMapping
    public List<Notification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        Notification notification = notificationService.getNotificationById(id);
        return notification != null ? ResponseEntity.ok(notification) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/user/{userId}")
    public List<Notification> getNotificationsByUser(@PathVariable Long userId) {
        return notificationService.getNotificationsByUserId(userId);
    }
    
    @PostMapping
    public ResponseEntity<Notification> sendNotification(@RequestBody NotificationRequest request) {
        Notification notification = notificationService.sendNotification(request);
        return notification != null ? ResponseEntity.ok(notification) : ResponseEntity.badRequest().build();
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Notification> updateStatus(@PathVariable Long id, @RequestParam Notification.NotificationStatus status) {
        Notification updated = notificationService.updateNotificationStatus(id, status);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }
}