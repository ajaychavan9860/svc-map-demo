package com.example.analyzer.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class AnalyzerConfiguration {
    
    @JsonProperty("service_detection")
    private ServiceDetectionConfig serviceDetection;
    
    @JsonProperty("dependency_patterns")
    private DependencyPatternsConfig dependencyPatterns;
    
    @JsonProperty("output_formats")
    private OutputFormatsConfig outputFormats;
    
    @JsonProperty("visualization")
    private VisualizationConfig visualization;

    // Default configuration
    public static AnalyzerConfiguration getDefault() {
        AnalyzerConfiguration config = new AnalyzerConfiguration();
        
        config.serviceDetection = new ServiceDetectionConfig();
        config.serviceDetection.pomFiles = List.of("**/pom.xml");
        config.serviceDetection.buildFiles = List.of("**/build.gradle", "**/package.json");
        config.serviceDetection.configFiles = List.of("**/application.yml", "**/application.properties", "**/bootstrap.yml");
        config.serviceDetection.excludeDirectories = List.of("target", "build", "node_modules", ".git", ".idea");
        config.serviceDetection.includeDirectories = List.of("src/main");
        
        config.dependencyPatterns = new DependencyPatternsConfig();
        config.dependencyPatterns.feignClients = List.of("@FeignClient", "@Service", "FeignClient");
        config.dependencyPatterns.restTemplates = List.of("RestTemplate", "WebClient", "HttpClient", "restTemplate", "webClient");
        config.dependencyPatterns.gatewayRoutes = List.of("spring.cloud.gateway.routes", "zuul.routes");
        config.dependencyPatterns.messagingQueues = List.of("@RabbitListener", "@EventHandler", "@KafkaListener");
        config.dependencyPatterns.databases = List.of("@Repository", "JpaRepository", "MongoRepository");
        config.dependencyPatterns.httpAnnotations = List.of("@RestController", "@GetMapping", "@PostMapping", "@RequestMapping");
        
        config.outputFormats = new OutputFormatsConfig();
        config.outputFormats.html = true;
        config.outputFormats.json = true;
        config.outputFormats.csv = true;
        config.outputFormats.markdown = true;
        config.outputFormats.dot = true;
        config.outputFormats.svg = true;
        config.outputFormats.png = false;
        
        config.visualization = new VisualizationConfig();
        config.visualization.showServiceTypes = true;
        config.visualization.showDependencyTypes = true;
        config.visualization.colorByServiceType = true;
        config.visualization.includeConfigServices = true;
        config.visualization.includeGatewayServices = true;
        
        return config;
    }

    public static class ServiceDetectionConfig {
        public List<String> pomFiles;
        public List<String> buildFiles;
        public List<String> configFiles;
        public List<String> excludeDirectories;
        public List<String> includeDirectories;

        // Getters and setters
        public List<String> getPomFiles() { return pomFiles; }
        public void setPomFiles(List<String> pomFiles) { this.pomFiles = pomFiles; }
        
        public List<String> getBuildFiles() { return buildFiles; }
        public void setBuildFiles(List<String> buildFiles) { this.buildFiles = buildFiles; }
        
        public List<String> getConfigFiles() { return configFiles; }
        public void setConfigFiles(List<String> configFiles) { this.configFiles = configFiles; }
        
        public List<String> getExcludeDirectories() { return excludeDirectories; }
        public void setExcludeDirectories(List<String> excludeDirectories) { this.excludeDirectories = excludeDirectories; }
        
        public List<String> getIncludeDirectories() { return includeDirectories; }
        public void setIncludeDirectories(List<String> includeDirectories) { this.includeDirectories = includeDirectories; }
    }

    public static class DependencyPatternsConfig {
        public List<String> feignClients;
        public List<String> restTemplates;
        public List<String> gatewayRoutes;
        public List<String> messagingQueues;
        public List<String> databases;
        public List<String> httpAnnotations;

        // Getters and setters
        public List<String> getFeignClients() { return feignClients; }
        public void setFeignClients(List<String> feignClients) { this.feignClients = feignClients; }
        
        public List<String> getRestTemplates() { return restTemplates; }
        public void setRestTemplates(List<String> restTemplates) { this.restTemplates = restTemplates; }
        
        public List<String> getGatewayRoutes() { return gatewayRoutes; }
        public void setGatewayRoutes(List<String> gatewayRoutes) { this.gatewayRoutes = gatewayRoutes; }
        
        public List<String> getMessagingQueues() { return messagingQueues; }
        public void setMessagingQueues(List<String> messagingQueues) { this.messagingQueues = messagingQueues; }
        
        public List<String> getDatabases() { return databases; }
        public void setDatabases(List<String> databases) { this.databases = databases; }
        
        public List<String> getHttpAnnotations() { return httpAnnotations; }
        public void setHttpAnnotations(List<String> httpAnnotations) { this.httpAnnotations = httpAnnotations; }
    }

    public static class OutputFormatsConfig {
        public boolean html;
        public boolean json;
        public boolean csv;
        public boolean markdown;
        public boolean dot;
        public boolean svg;
        public boolean png;

        // Getters and setters
        public boolean isHtml() { return html; }
        public void setHtml(boolean html) { this.html = html; }
        
        public boolean isJson() { return json; }
        public void setJson(boolean json) { this.json = json; }
        
        public boolean isCsv() { return csv; }
        public void setCsv(boolean csv) { this.csv = csv; }
        
        public boolean isMarkdown() { return markdown; }
        public void setMarkdown(boolean markdown) { this.markdown = markdown; }
        
        public boolean isDot() { return dot; }
        public void setDot(boolean dot) { this.dot = dot; }
        
        public boolean isSvg() { return svg; }
        public void setSvg(boolean svg) { this.svg = svg; }
        
        public boolean isPng() { return png; }
        public void setPng(boolean png) { this.png = png; }
    }

    public static class VisualizationConfig {
        public boolean showServiceTypes;
        public boolean showDependencyTypes;
        public boolean colorByServiceType;
        public boolean includeConfigServices;
        public boolean includeGatewayServices;

        // Getters and setters
        public boolean isShowServiceTypes() { return showServiceTypes; }
        public void setShowServiceTypes(boolean showServiceTypes) { this.showServiceTypes = showServiceTypes; }
        
        public boolean isShowDependencyTypes() { return showDependencyTypes; }
        public void setShowDependencyTypes(boolean showDependencyTypes) { this.showDependencyTypes = showDependencyTypes; }
        
        public boolean isColorByServiceType() { return colorByServiceType; }
        public void setColorByServiceType(boolean colorByServiceType) { this.colorByServiceType = colorByServiceType; }
        
        public boolean isIncludeConfigServices() { return includeConfigServices; }
        public void setIncludeConfigServices(boolean includeConfigServices) { this.includeConfigServices = includeConfigServices; }
        
        public boolean isIncludeGatewayServices() { return includeGatewayServices; }
        public void setIncludeGatewayServices(boolean includeGatewayServices) { this.includeGatewayServices = includeGatewayServices; }
    }

    // Main getters and setters
    public ServiceDetectionConfig getServiceDetection() { return serviceDetection; }
    public void setServiceDetection(ServiceDetectionConfig serviceDetection) { this.serviceDetection = serviceDetection; }
    
    public DependencyPatternsConfig getDependencyPatterns() { return dependencyPatterns; }
    public void setDependencyPatterns(DependencyPatternsConfig dependencyPatterns) { this.dependencyPatterns = dependencyPatterns; }
    
    public OutputFormatsConfig getOutputFormats() { return outputFormats; }
    public void setOutputFormats(OutputFormatsConfig outputFormats) { this.outputFormats = outputFormats; }
    
    public VisualizationConfig getVisualization() { return visualization; }
    public void setVisualization(VisualizationConfig visualization) { this.visualization = visualization; }
}