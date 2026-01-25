package com.example.analyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class ServiceInfo {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("type")
    private String type; // gateway, business, config, discovery, etc.
    
    @JsonProperty("path")
    private String path;
    
    @JsonProperty("port")
    private Integer port;
    
    @JsonProperty("framework")
    private String framework; // spring-boot, spring-cloud, etc.
    
    @JsonProperty("build_tool")
    private String buildTool; // maven, gradle, npm, etc.
    
    @JsonProperty("language")
    private String language; // java, javascript, python, etc.
    
    @JsonProperty("config_files")
    private List<String> configFiles = new ArrayList<>();
    
    @JsonProperty("main_class")
    private String mainClass;
    
    @JsonProperty("dependencies")
    private List<ServiceDependency> dependencies = new ArrayList<>();
    
    @JsonProperty("exposes_endpoints")
    private List<String> exposedEndpoints = new ArrayList<>();
    
    @JsonProperty("database_connections")
    private List<String> databaseConnections = new ArrayList<>();

    // Constructors
    public ServiceInfo() {}
    
    public ServiceInfo(String name, String type, String path) {
        this.name = name;
        this.type = type;
        this.path = path;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public String getBuildTool() {
        return buildTool;
    }

    public void setBuildTool(String buildTool) {
        this.buildTool = buildTool;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<String> getConfigFiles() {
        return configFiles;
    }

    public void setConfigFiles(List<String> configFiles) {
        this.configFiles = configFiles;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public List<ServiceDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<ServiceDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public List<String> getExposedEndpoints() {
        return exposedEndpoints;
    }

    public void setExposedEndpoints(List<String> exposedEndpoints) {
        this.exposedEndpoints = exposedEndpoints;
    }

    public List<String> getDatabaseConnections() {
        return databaseConnections;
    }

    public void setDatabaseConnections(List<String> databaseConnections) {
        this.databaseConnections = databaseConnections;
    }
}