package com.example.analyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class AnalysisResult {
    
    @JsonProperty("analysis_date")
    private LocalDateTime analysisDate;
    
    @JsonProperty("project_path")
    private String projectPath;
    
    @JsonProperty("total_services")
    private int totalServices;
    
    @JsonProperty("total_dependencies")
    private int totalDependencies;
    
    @JsonProperty("services")
    private List<ServiceInfo> services;
    
    @JsonProperty("dependencies")
    private List<ServiceDependency> dependencies;
    
    @JsonProperty("analysis_summary")
    private AnalysisSummary summary;

    // Constructors
    public AnalysisResult() {}

    // Getters and setters
    public LocalDateTime getAnalysisDate() {
        return analysisDate;
    }

    public void setAnalysisDate(LocalDateTime analysisDate) {
        this.analysisDate = analysisDate;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public int getTotalServices() {
        return totalServices;
    }

    public void setTotalServices(int totalServices) {
        this.totalServices = totalServices;
    }

    public int getTotalDependencies() {
        return totalDependencies;
    }

    public void setTotalDependencies(int totalDependencies) {
        this.totalDependencies = totalDependencies;
    }

    public List<ServiceInfo> getServices() {
        return services;
    }

    public void setServices(List<ServiceInfo> services) {
        this.services = services;
    }

    public List<ServiceDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<ServiceDependency> dependencies) {
        this.dependencies = dependencies;
    }

    // Alias for compatibility
    public LocalDateTime getAnalysisTimestamp() {
        return analysisDate;
    }

    public AnalysisSummary getSummary() {
        return summary;
    }

    public void setSummary(AnalysisSummary summary) {
        this.summary = summary;
    }
    
    public static class AnalysisSummary {
        @JsonProperty("service_types")
        private List<String> serviceTypes;
        
        @JsonProperty("dependency_types")
        private List<String> dependencyTypes;
        
        @JsonProperty("critical_services")
        private List<String> criticalServices;
        
        @JsonProperty("isolated_services")
        private List<String> isolatedServices;

        // Getters and setters
        public List<String> getServiceTypes() {
            return serviceTypes;
        }

        public void setServiceTypes(List<String> serviceTypes) {
            this.serviceTypes = serviceTypes;
        }

        public List<String> getDependencyTypes() {
            return dependencyTypes;
        }

        public void setDependencyTypes(List<String> dependencyTypes) {
            this.dependencyTypes = dependencyTypes;
        }

        public List<String> getCriticalServices() {
            return criticalServices;
        }

        public void setCriticalServices(List<String> criticalServices) {
            this.criticalServices = criticalServices;
        }

        public List<String> getIsolatedServices() {
            return isolatedServices;
        }

        public void setIsolatedServices(List<String> isolatedServices) {
            this.isolatedServices = isolatedServices;
        }
    }
}