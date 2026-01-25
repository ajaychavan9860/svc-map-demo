package com.demo.microservices.notification.dto;

import com.demo.microservices.notification.model.Notification;

public class NotificationRequest {
    private Long userId;
    private String recipient;
    private String subject;
    private String message;
    private Notification.NotificationType type;
    
    // Constructors
    public NotificationRequest() {}
    
    public NotificationRequest(Long userId, String recipient, String subject, String message, Notification.NotificationType type) {
        this.userId = userId;
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
        this.type = type;
    }
    
    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Notification.NotificationType getType() { return type; }
    public void setType(Notification.NotificationType type) { this.type = type; }
}