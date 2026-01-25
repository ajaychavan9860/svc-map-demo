package com.example.loggingservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "log_entries")
public class LogEntry {
    
    @Id
    private String id;
    private String message;
    private LocalDateTime timestamp;
    private String level;
    private String source;
    
    // Constructors
    public LogEntry() {}
    
    public LogEntry(String message, String level, String source) {
        this.message = message;
        this.level = level;
        this.source = source;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}