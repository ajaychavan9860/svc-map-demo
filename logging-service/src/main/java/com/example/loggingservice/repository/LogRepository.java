package com.example.loggingservice.repository;

import com.example.loggingservice.model.LogEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogRepository extends MongoRepository<LogEntry, String> {
    
    List<LogEntry> findTop10ByOrderByTimestampDesc();
    
    List<LogEntry> findBySourceOrderByTimestampDesc(String source);
    
    List<LogEntry> findByLevelOrderByTimestampDesc(String level);
}