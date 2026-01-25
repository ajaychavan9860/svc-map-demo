package com.example.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserActivityService {

    @Autowired
    private RestTemplate restTemplate;

    public void logUserActivity(Long userId, String activity) {
        try {
            // Call logging-service directly via HTTP
            String loggingServiceUrl = "http://logging-service:8088/api/logs/user-activity";
            
            UserActivityLog activityLog = new UserActivityLog();
            activityLog.setUserId(userId);
            activityLog.setActivity(activity);
            activityLog.setTimestamp(System.currentTimeMillis());
            
            restTemplate.postForObject(loggingServiceUrl, activityLog, String.class);
            System.out.println("User activity logged: " + activity + " for user: " + userId);
            
        } catch (Exception e) {
            System.err.println("Failed to log user activity: " + e.getMessage());
        }
    }

    public void sendWelcomeEmail(Long userId, String email, String firstName) {
        try {
            // Call email-service for new user welcome
            String emailServiceUrl = "http://email-service:8087/api/email/send";
            
            WelcomeEmailRequest emailRequest = new WelcomeEmailRequest();
            emailRequest.setTo(email);
            emailRequest.setSubject("Welcome to Our Platform!");
            emailRequest.setMessage("Hello " + firstName + ", welcome to our microservices platform! Your user ID is: " + userId);
            
            restTemplate.postForObject(emailServiceUrl, emailRequest, String.class);
            System.out.println("Welcome email sent to user: " + userId);
            
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }
    
    // Inner classes for requests
    public static class UserActivityLog {
        private Long userId;
        private String activity;
        private Long timestamp;
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getActivity() { return activity; }
        public void setActivity(String activity) { this.activity = activity; }
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    }
    
    public static class WelcomeEmailRequest {
        private String to;
        private String subject;
        private String message;
        
        // Getters and setters
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}