package com.example.loggingservice.consumer;

import com.example.loggingservice.model.LogEntry;
import com.example.loggingservice.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogEventConsumer {

    @Autowired
    private LogRepository logRepository;

    @KafkaListener(topics = "microservices-logs", groupId = "logging-service-group")
    public void consumeLogEvent(String logMessage) {
        // Process incoming log events from other microservices
        System.out.println("Received log event: " + logMessage);
        
        try {
            // Parse and store log entry
            LogEntry logEntry = new LogEntry();
            logEntry.setMessage(logMessage);
            logEntry.setTimestamp(LocalDateTime.now());
            logEntry.setLevel("INFO");
            logEntry.setSource("microservice");
            
            logRepository.save(logEntry);
            System.out.println("Log entry saved to MongoDB");
        } catch (Exception e) {
            System.err.println("Failed to process log event: " + e.getMessage());
        }
    }
}