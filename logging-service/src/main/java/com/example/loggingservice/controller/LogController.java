package com.example.loggingservice.controller;

import com.example.loggingservice.model.LogEntry;
import com.example.loggingservice.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    @Autowired
    private LogRepository logRepository;

    @PostMapping("/user-activity")
    public String logUserActivity(@RequestBody UserActivityRequest request) {
        try {
            LogEntry logEntry = new LogEntry();
            logEntry.setMessage("User Activity: " + request.getActivity() + " by user " + request.getUserId());
            logEntry.setTimestamp(LocalDateTime.now());
            logEntry.setLevel("INFO");
            logEntry.setSource("user-service");
            
            logRepository.save(logEntry);
            return "User activity logged successfully";
        } catch (Exception e) {
            return "Failed to log user activity: " + e.getMessage();
        }
    }

    @PostMapping("/system")
    public String logSystemEvent(@RequestBody SystemLogRequest request) {
        try {
            LogEntry logEntry = new LogEntry();
            logEntry.setMessage(request.getMessage());
            logEntry.setTimestamp(LocalDateTime.now());
            logEntry.setLevel(request.getLevel());
            logEntry.setSource(request.getSource());
            
            logRepository.save(logEntry);
            return "System event logged successfully";
        } catch (Exception e) {
            return "Failed to log system event: " + e.getMessage();
        }
    }

    @GetMapping("/recent")
    public List<LogEntry> getRecentLogs(@RequestParam(defaultValue = "10") int limit) {
        return logRepository.findTop10ByOrderByTimestampDesc();
    }

    @GetMapping("/health")
    public String health() {
        return "Logging service is healthy";
    }

    // Request DTOs
    public static class UserActivityRequest {
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

    public static class SystemLogRequest {
        private String message;
        private String level;
        private String source;
        
        // Getters and setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }
}