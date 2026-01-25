package com.example.analyzer;

import com.example.analyzer.config.AnalyzerConfiguration;
import com.example.analyzer.model.*;
import com.example.analyzer.scanner.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;import java.util.ArrayList;import java.util.*;
import java.util.stream.Collectors;

@Component
public class MicroserviceAnalyzer {
    
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ObjectMapper jsonMapper = new ObjectMapper();
    
    public void analyzeProject(Path projectPath, Path configPath) throws IOException {
        // Load configuration
        AnalyzerConfiguration config = loadConfiguration(configPath);
        
        System.out.println("🔍 Discovering services...");
        
        // Discover services using generic patterns
        GenericServiceDiscovery serviceDiscovery = new GenericServiceDiscovery(config);
        List<ServiceInfo> services = serviceDiscovery.discoverServices(projectPath);
        
        System.out.println("📋 Found " + services.size() + " services:");
        services.forEach(service -> 
            System.out.println("   - " + service.getName() + " (" + service.getType() + ") at " + service.getPath()));
        
        System.out.println("🔗 Analyzing dependencies...");
        
        // Analyze dependencies for each service
        GenericDependencyScanner dependencyScanner = new GenericDependencyScanner(config);
        List<ServiceDependency> allDependencies = new ArrayList<>();
        
        for (ServiceInfo service : services) {
            List<ServiceDependency> serviceDependencies = dependencyScanner.scanDependencies(service, services);
            
            // Ensure fromService is properly set for arrow drawing
            for (ServiceDependency dep : serviceDependencies) {
                if (dep.getFromService() == null || dep.getFromService().isEmpty() || ".".equals(dep.getFromService())) {
                    dep.setFromService(service.getName());
                }
            }
            
            service.setDependencies(serviceDependencies);
            allDependencies.addAll(serviceDependencies);
        }
        
        // NOTE: Only using actual detected dependencies - no hardcoded assumptions
        // But add generic gateway routing since gateways legitimately route to services
        allDependencies.addAll(createGatewayRoutingDependencies(services));
        
        // Filter out noise: configuration dependencies and parent project
        allDependencies = filterBusinessDependencies(allDependencies);
        
        // Calculate total dependencies
        int totalDependencies = allDependencies.size();
        
        System.out.println("📊 Found " + totalDependencies + " dependency relationships");
        
        // Create analysis result
        AnalysisResult result = new AnalysisResult();
        result.setAnalysisDate(LocalDateTime.now());
        result.setProjectPath(projectPath.toString());
        result.setServices(services);
        result.setDependencies(allDependencies); // Add all dependencies to result
        result.setTotalServices(services.size());
        result.setTotalDependencies(totalDependencies);
        
        // Generate reports with Pure Java SVG generator
        System.out.println("📈 Generating reports...");
        
        Path outputDir = projectPath.resolve(AnalyzerConstants.DEPENDENCY_ANALYSIS_DIR);
        Files.createDirectories(outputDir);

        // Create GraphViz Java generator
        GraphVizJavaSvgGenerator graphVizJavaGenerator = new GraphVizJavaSvgGenerator();
        EnhancedReportGenerator reportGenerator = new EnhancedReportGenerator(graphVizJavaGenerator);
        reportGenerator.generateReports(result, outputDir, config);
        
        System.out.println("📂 Reports generated:");
        if (config.getOutputFormats().isHtml()) System.out.println("   ✅ " + AnalyzerConstants.HTML_REPORT_FILE);
        if (config.getOutputFormats().isJson()) System.out.println("   ✅ " + AnalyzerConstants.JSON_REPORT_FILE);
        if (config.getOutputFormats().isCsv()) System.out.println("   ✅ " + AnalyzerConstants.CSV_MATRIX_FILE);
        if (config.getOutputFormats().isMarkdown()) System.out.println("   ✅ " + AnalyzerConstants.IMPACT_ANALYSIS_FILE);
        if (config.getOutputFormats().isSvg()) {
            System.out.println("   🏆 " + AnalyzerConstants.SVG_DIAGRAM_FILE + " (Original GraphViz + Pure Java)");
        }
    }
    
    private AnalyzerConfiguration loadConfiguration(Path configPath) throws IOException {
        if (configPath != null && Files.exists(configPath)) {
            System.out.println("📖 Loading configuration from: " + configPath);
            return yamlMapper.readValue(configPath.toFile(), AnalyzerConfiguration.class);
        } else {
            System.out.println("⚙️ Using default configuration");
            return AnalyzerConfiguration.getDefault();
        }
    }
    
    // Create gateway routing dependencies in a generic way
    private List<ServiceDependency> createGatewayRoutingDependencies(List<ServiceInfo> services) {
        List<ServiceDependency> dependencies = new ArrayList<>();
        
        // Find any service that acts as a gateway (generic detection)
        List<ServiceInfo> gateways = services.stream()
            .filter(s -> s.getName().toLowerCase().contains("gateway") || 
                        s.getType().equalsIgnoreCase(AnalyzerConstants.GATEWAY_TYPE))
            .collect(Collectors.toList());
            
        // Find business services that gateways would route to (exclude parent project)
        List<ServiceInfo> businessServices = services.stream()
            .filter(s -> s.getType().equalsIgnoreCase(AnalyzerConstants.BUSINESS_TYPE) && 
                        !s.getName().toLowerCase().contains("gateway") &&
                        !isParentProject(s.getName()))
            .collect(Collectors.toList());
        
        // Create gateway routing relationships
        for (ServiceInfo gateway : gateways) {
            for (ServiceInfo businessService : businessServices) {
                dependencies.add(new ServiceDependency(
                    gateway.getName(), 
                    businessService.getName(), 
                    AnalyzerConstants.GATEWAY_DEPENDENCY_TYPE
                ));
            }
        }
        
        return dependencies;
    }
    
    // Filter to keep only business-critical dependencies
    private List<ServiceDependency> filterBusinessDependencies(List<ServiceDependency> dependencies) {
        return dependencies.stream()
            .filter(dep -> {
                // Remove parent project dependencies
                if (isParentProject(dep.getFromService()) || isParentProject(dep.getToService())) {
                    return false;
                }
                // Keep only business-critical dependency types
                String type = dep.getDependencyType().toLowerCase();
                return type.equals(AnalyzerConstants.GATEWAY_DEPENDENCY_TYPE) ||
                       type.equals(AnalyzerConstants.REST_TEMPLATE_TYPE) ||
                       type.equals(AnalyzerConstants.WEBCLIENT_TYPE) ||
                       type.equals(AnalyzerConstants.ASYNC_TYPE) ||
                       type.equals(AnalyzerConstants.MESSAGING_TYPE);
            })
            .collect(Collectors.toList());
    }
    
    // Check if service name represents parent project
    private boolean isParentProject(String serviceName) {
        return serviceName != null && 
               (serviceName.contains("demo") || 
                serviceName.contains("workspace") ||
                serviceName.contains("parent") ||
                serviceName.equals(".") ||
                serviceName.toLowerCase().endsWith("-demo"));
    }
}