package com.example.analyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceDependency {
    
    @JsonProperty("from_service")
    private String fromService;
    
    @JsonProperty("target_service")
    private String targetService;
    
    @JsonProperty("dependency_type")
    private String dependencyType; // feign, rest-template, gateway-route, messaging, database
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("source_file")
    private String sourceFile;
    
    @JsonProperty("line_number")
    private Integer lineNumber;
    
    @JsonProperty("endpoint")
    private String endpoint;
    
    @JsonProperty("http_method")
    private String httpMethod;

    // Constructors
    public ServiceDependency() {}
    
    public ServiceDependency(String fromService, String targetService, String dependencyType) {
        this.fromService = fromService;
        this.targetService = targetService;
        this.dependencyType = dependencyType;
    }

    // Getters and setters
    public String getFromService() {
        return fromService;
    }

    public void setFromService(String fromService) {
        this.fromService = fromService;
    }

    public String getTargetService() {
        return targetService;
    }

    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }

    public String getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(String dependencyType) {
        this.dependencyType = dependencyType;
    }

    // Compatibility aliases
    public String getToService() {
        return targetService;
    }

    public String getType() {
        return dependencyType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
}