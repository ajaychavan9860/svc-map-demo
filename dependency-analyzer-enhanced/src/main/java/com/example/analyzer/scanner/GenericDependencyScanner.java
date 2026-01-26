package com.example.analyzer.scanner;

import com.example.analyzer.AnalyzerConstants;
import com.example.analyzer.config.AnalyzerConfiguration;
import com.example.analyzer.model.ServiceInfo;
import com.example.analyzer.model.ServiceDependency;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class GenericDependencyScanner {

    private static final Logger logger = LoggerFactory.getLogger(GenericDependencyScanner.class);

    private final AnalyzerConfiguration config;
    private final JavaParser javaParser = new JavaParser();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    
    public GenericDependencyScanner(AnalyzerConfiguration config) {
        this.config = config;
    }
    
    public List<ServiceDependency> scanDependencies(ServiceInfo service, List<ServiceInfo> allServices, Path projectRoot) {
        List<ServiceDependency> dependencies = new ArrayList<>();
        
        Path servicePath = projectRoot.resolve(service.getPath());
        
        try {
            // Scan Java files for Feign clients, REST templates, etc.
            if ("java".equals(service.getLanguage())) {
                dependencies.addAll(scanJavaFiles(servicePath, allServices));
            }
            
            // Scan configuration files for gateway routes, etc.
            dependencies.addAll(scanConfigurationFiles(servicePath, allServices));
            
            // Scan for messaging dependencies
            dependencies.addAll(scanMessagingDependencies(servicePath, allServices));
            
        } catch (Exception e) {
            logger.error("Error scanning dependencies for {}: {}", service.getName(), e.getMessage(), e);
        }
        
        return dependencies;
    }
    
    private List<ServiceDependency> scanJavaFiles(Path servicePath, List<ServiceInfo> allServices) {
        List<ServiceDependency> dependencies = new ArrayList<>();
        
        try {
            PathMatcher javaMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.java");
            
            try (var stream = Files.walk(servicePath)) {
                List<Path> javaFiles = stream
                    .filter(javaMatcher::matches)
                    .filter(path -> !path.toString().contains("test"))
                    .collect(Collectors.toList());
                
                for (Path javaFile : javaFiles) {
                    dependencies.addAll(analyzeJavaFile(javaFile, servicePath, allServices));
                }
            }
            
        } catch (Exception e) {
            logger.error("Error scanning Java files in path: {}", servicePath, e);
        }
        
        return dependencies;
    }
    
    private List<ServiceDependency> analyzeJavaFile(Path javaFile, Path servicePath, List<ServiceInfo> allServices) {
        List<ServiceDependency> dependencies = new ArrayList<>();
        
        try {
            CompilationUnit cu = javaParser.parse(javaFile).getResult().orElse(null);
            if (cu == null) {
                return dependencies;
            }
            
            // Look for Feign clients
            cu.findAll(AnnotationExpr.class).forEach(annotation -> {
                String annotationName = annotation.getNameAsString();
                
                for (String feignPattern : config.getDependencyPatterns().getFeignClients()) {
                    if (annotationName.contains(feignPattern) || annotationName.equals("FeignClient")) {
                        ServiceDependency dependency = extractFeignDependency(annotation, javaFile, servicePath, allServices);
                        if (dependency != null) {
                            dependencies.add(dependency);
                        }
                    }
                }
            });
            
            // Look for RestTemplate usage
            cu.findAll(MethodDeclaration.class).forEach(method -> {
                String methodBody = method.toString();
                
                for (String restPattern : config.getDependencyPatterns().getRestTemplates()) {
                    if (methodBody.contains(restPattern)) {
                        ServiceDependency dependency = extractRestTemplateDependency(method, javaFile, servicePath, allServices);
                        if (dependency != null) {
                            dependencies.add(dependency);
                        }
                    }
                }
            });
            
            // Look for controller endpoints
            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                classDecl.getAnnotations().forEach(annotation -> {
                    if (annotation.getNameAsString().equals(AnalyzerConstants.REST_CONTROLLER)) {
                        extractControllerEndpoints(classDecl, javaFile, servicePath).forEach(endpoint -> {
                            // Add to service exposed endpoints
                            // This is for future reference
                        });
                    }
                });
            });
            
        } catch (Exception e) {
            logger.error("Error analyzing Java file {}: {}", javaFile, e.getMessage(), e);
        }
        
        return dependencies;
    }
    
    private ServiceDependency extractFeignDependency(AnnotationExpr annotation, Path javaFile, Path servicePath, List<ServiceInfo> allServices) {
        try {
            String annotationStr = annotation.toString();
            
            // Extract service name from @FeignClient annotation
            // Supports patterns like:
            // @FeignClient(name = "service-name")
            // @FeignClient(value = "service-name")
            // @FeignClient("service-name")
            // @FeignClient(name = "service-name", url = "${service.url}")
            String targetServiceName = null;
            
            if (annotationStr.contains("name") || annotationStr.contains("value") || annotationStr.contains("FeignClient(\"")) {
                // Parse @FeignClient(name = "service-name") or @FeignClient("service-name")
                String[] parts = annotationStr.split("[\"\']");
                for (int i = 0; i < parts.length - 1; i++) {
                    if (parts[i].contains("name") || parts[i].contains("value") || parts[i].contains("FeignClient(")) {
                        // Get the next quoted string
                        targetServiceName = parts[i + 1];
                        break;
                    }
                }
            }
            
            if (targetServiceName != null && !targetServiceName.trim().isEmpty()) {
                // Clean up service name (remove any trailing spaces or special chars)
                targetServiceName = targetServiceName.trim();
                
                // Try to match with actual service names using fuzzy matching
                String matchedServiceName = findMatchingServiceName(targetServiceName, allServices);
                if (matchedServiceName == null) {
                    matchedServiceName = targetServiceName; // Keep original if no match found
                }
                
                // Extract source service name from the service path
                String sourceServiceName = servicePath.getFileName().toString();
                
                String relativeFile = servicePath.relativize(javaFile).toString();
                ServiceDependency dependency = new ServiceDependency(
                    sourceServiceName,        // fromService
                    matchedServiceName,       // targetService (matched to actual service)
                    "feign-client"           // dependencyType
                );
                dependency.setDescription("Feign client call to " + matchedServiceName);
                dependency.setSourceFile(relativeFile);
                dependency.setLineNumber(annotation.getBegin().map(pos -> pos.line).orElse(null));
                
                logger.debug("Found Feign dependency: {} -> {} (original: {})", 
                    sourceServiceName, matchedServiceName, targetServiceName);
                
                return dependency;
            }
            
        } catch (Exception e) {
            logger.error("Error extracting Feign dependency: {}", e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Fuzzy matching to find the actual service name from Feign client name
     * Handles variations like:
     * - task-service -> TaskManagementService
     * - excel-generation-service -> excel-service
     * - user-service -> UserService
     */
    private String findMatchingServiceName(String feignClientName, List<ServiceInfo> allServices) {
        // Direct match first
        for (ServiceInfo service : allServices) {
            if (service.getName().equals(feignClientName)) {
                return service.getName();
            }
        }
        
        // Normalize the feign client name for comparison
        String normalizedFeignName = normalizeServiceName(feignClientName);
        
        // Try normalized matching
        for (ServiceInfo service : allServices) {
            String normalizedServiceName = normalizeServiceName(service.getName());
            if (normalizedServiceName.equals(normalizedFeignName)) {
                return service.getName();
            }
        }
        
        // Try partial matching (contains)
        for (ServiceInfo service : allServices) {
            String normalizedServiceName = normalizeServiceName(service.getName());
            // Check if one contains the other
            if (normalizedServiceName.contains(normalizedFeignName) || 
                normalizedFeignName.contains(normalizedServiceName)) {
                return service.getName();
            }
        }
        
        // No match found
        return null;
    }
    
    /**
     * Normalize service name for matching
     * Converts: TaskManagementService, task-management-service, task_management_service
     * To: taskmanagementservice
     */
    private String normalizeServiceName(String serviceName) {
        return serviceName
            .toLowerCase()
            .replaceAll("[-_]", "")  // Remove hyphens and underscores
            .replaceAll("service$", "");  // Remove trailing "service"
    }
    
    private ServiceDependency extractRestTemplateDependency(MethodDeclaration method, Path javaFile, Path servicePath, List<ServiceInfo> allServices) {
        try {
            String methodBody = method.toString();
            
            // Look for URL patterns in RestTemplate calls - using direct text search
            AtomicReference<String> foundUrl = new AtomicReference<>();
            
            // Simple approach: just search the method text directly for HTTP URLs
            String[] lines = methodBody.split("\n");
            
            for (String line : lines) {
                String trimmed = line.trim();
                
                // Look for any HTTP URLs in the line
                if (trimmed.contains(AnalyzerConstants.HTTP_PREFIX) || trimmed.contains(AnalyzerConstants.HTTPS_PREFIX)) {
                    String url = extractUrlFromLine(trimmed);
                    if (url != null) {
                        foundUrl.set(url);
                        break;
                    }
                }
            }
            
            // If we found a URL, check if this method uses RestTemplate/WebClient
            if (foundUrl.get() != null) {
                boolean hasRestTemplate = false;
                for (String line : lines) {
                    if (line.contains("restTemplate") || line.contains("webClient") || 
                        line.contains("RestTemplate") || line.contains("WebClient")) {
                        hasRestTemplate = true;
                        break;
                    }
                }
                
                if (hasRestTemplate) {
                    String targetServiceName = extractServiceNameFromUrl(foundUrl.get(), allServices);
                    if (targetServiceName != null) {
                        String relativeFile = servicePath.relativize(javaFile).toString();
                        
                        // Extract the source service name from the service path
                        String sourceServiceName = servicePath.getFileName().toString();
                        
                        ServiceDependency dependency = new ServiceDependency(
                            sourceServiceName,        // fromService
                            targetServiceName,        // targetService 
                            AnalyzerConstants.REST_TEMPLATE_TYPE          // dependencyType
                        );
                        dependency.setDescription("REST call to " + targetServiceName);
                        dependency.setSourceFile(relativeFile);
                        dependency.setEndpoint(foundUrl.get());
                        return dependency;
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error extracting RestTemplate dependency: {}", e.getMessage(), e);
        }
        
        return null;
    }
    
    private List<String> extractControllerEndpoints(ClassOrInterfaceDeclaration classDecl, Path javaFile, Path servicePath) {
        List<String> endpoints = new ArrayList<>();
        
        classDecl.getMethods().forEach(method -> {
            method.getAnnotations().forEach(annotation -> {
                String annotationName = annotation.getNameAsString();
                if (annotationName.contains("Mapping")) {
                    // Extract endpoint path
                    String endpoint = extractEndpointPath(annotation);
                    if (endpoint != null) {
                        endpoints.add(endpoint);
                    }
                }
            });
        });
        
        return endpoints;
    }
    
    private String extractEndpointPath(AnnotationExpr annotation) {
        String annotationStr = annotation.toString();
        
        // Extract path from @GetMapping("/path") or @RequestMapping(value = "/path")
        String[] parts = annotationStr.split("[\"\']");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.startsWith("/") && !part.contains("(") && !part.contains(")")) {
                return part;
            }
        }
        
        return null;
    }
    
    private String extractUrlFromLine(String line) {
        // Look for URLs in quotes
        String trimmed = line.trim();
        
        // Look for any HTTP URL in quotes regardless of context
        if (trimmed.contains(AnalyzerConstants.HTTP_PREFIX) || trimmed.contains(AnalyzerConstants.HTTPS_PREFIX)) {
            
            // Pattern 1: String variable assignment like 'String emailServiceUrl = "http://email-service:8087/api/email/send";'
            if (trimmed.contains("= \"http")) {
                int startQuote = trimmed.indexOf("\"http");
                int endQuote = trimmed.indexOf("\"", startQuote + 1);
                if (startQuote >= 0 && endQuote > startQuote) {
                    String url = trimmed.substring(startQuote + 1, endQuote);
                    return url;
                }
            }
            
            // Pattern 2: Method call with URI like '.uri("http://user-service/api/users/count")'
            if (trimmed.contains(".uri(\"http")) {
                int startQuote = trimmed.indexOf("\"http");
                int endQuote = trimmed.indexOf("\"", startQuote + 1);
                if (startQuote >= 0 && endQuote > startQuote) {
                    String url = trimmed.substring(startQuote + 1, endQuote);
                    return url;
                }
            }
            
            // Pattern 3: General quote-based extraction (fallback)
            int httpPos = trimmed.indexOf(AnalyzerConstants.HTTP_PREFIX);
            if (httpPos == -1) {
                httpPos = trimmed.indexOf(AnalyzerConstants.HTTPS_PREFIX);
            }
            
            // Look for quotes around the URL
            int startQuote = trimmed.lastIndexOf('"', httpPos);
            int endQuote = trimmed.indexOf('"', httpPos);
            
            if (startQuote >= 0 && endQuote > httpPos) {
                String url = trimmed.substring(startQuote + 1, endQuote);
                // Remove any trailing characters that might be after the closing quote
                url = url.replaceAll("[;,)\"'\\s]*$", "");
                return url;
            }
            
            // If no quotes, try to extract URL differently 
            // Look for URL patterns with common delimiters
            String[] words = trimmed.split("\\s+");
            for (String word : words) {
                if (word.contains(AnalyzerConstants.HTTP_PREFIX) || word.contains(AnalyzerConstants.HTTPS_PREFIX)) {
                    // Clean up any trailing characters
                    word = word.replaceAll("[;,)\"']*$", "");
                    if (word.startsWith("\"")) {
                        word = word.substring(1);
                    }
                    return word;
                }
            }
        }
        
        return null;
    }
    
    private String extractServiceNameFromUrl(String url, List<ServiceInfo> allServices) {
        // Try to match URL pattern to known services
        for (ServiceInfo service : allServices) {
            if (url.contains(service.getName())) {
                return service.getName();
            }
            if (service.getPort() != null && url.contains(":" + service.getPort())) {
                return service.getName();
            }
        }
        
        // Extract service name from URL pattern (e.g., http://user-service/api/users)
        if (url.contains("://")) {
            String[] parts = url.split("://");
            if (parts.length > 1) {
                String hostPart = parts[1].split("/")[0];
                String serviceName = hostPart.split(":")[0];
                return serviceName;
            }
        }
        
        return null;
    }
    
    private List<ServiceDependency> scanConfigurationFiles(Path servicePath, List<ServiceInfo> allServices) {
        List<ServiceDependency> dependencies = new ArrayList<>();
        
        try {
            // Look for application.yml and application.properties
            String[] configFiles = {"application.yml", "application.yaml", "application.properties"};
            
            for (String configFile : configFiles) {
                Path configPath = servicePath.resolve("src/main/resources/" + configFile);
                if (Files.exists(configPath)) {
                    dependencies.addAll(analyzeConfigFile(configPath, servicePath, allServices));
                }
            }
            
        } catch (Exception e) {
            logger.error("Error scanning configuration files: {}", e.getMessage(), e);
        }
        
        return dependencies;
    }
    
    private List<ServiceDependency> analyzeConfigFile(Path configFile, Path servicePath, List<ServiceInfo> allServices) {
        List<ServiceDependency> dependencies = new ArrayList<>();
        
        try {
            String content = Files.readString(configFile);
            
            // Look for gateway routes
            if (content.contains("routes") && (content.contains("gateway") || content.contains("zuul"))) {
                dependencies.addAll(extractGatewayRoutes(content, configFile, servicePath, allServices));
            }
            
            // Look for other service references
            for (ServiceInfo service : allServices) {
                if (content.contains(service.getName()) && !service.getName().equals(servicePath.getFileName().toString())) {
                    ServiceDependency dependency = new ServiceDependency(
                        service.getName(),
                        "configuration",
                        "Configuration reference to " + service.getName()
                    );
                    dependency.setSourceFile("src/main/resources/" + configFile.getFileName().toString());
                    dependencies.add(dependency);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error analyzing config file: {}", e.getMessage(), e);
        }
        
        return dependencies;
    }
    
    private List<ServiceDependency> extractGatewayRoutes(String content, Path configFile, Path servicePath, List<ServiceInfo> allServices) {
        List<ServiceDependency> dependencies = new ArrayList<>();
        
        try {
            String[] lines = content.split("\n");
            String currentRoute = null;
            
            for (String line : lines) {
                line = line.trim();
                
                // Look for route definitions
                if (line.contains("- id:") || line.contains("id:")) {
                    currentRoute = extractRouteId(line);
                }
                
                if (line.contains("uri:") && currentRoute != null) {
                    String uri = extractUri(line);
                    if (uri != null) {
                        String serviceName = extractServiceNameFromUri(uri, allServices);
                        if (serviceName != null) {
                            ServiceDependency dependency = new ServiceDependency(
                                serviceName,
                                "gateway-route",
                                "Gateway route to " + serviceName + " (route: " + currentRoute + ")"
                            );
                            dependency.setSourceFile("src/main/resources/" + configFile.getFileName().toString());
                            dependencies.add(dependency);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error extracting gateway routes: {}", e.getMessage(), e);
        }
        
        return dependencies;
    }
    
    private String extractRouteId(String line) {
        String[] parts = line.split(":");
        if (parts.length > 1) {
            return parts[1].trim().replaceAll("[\"\']", "");
        }
        return null;
    }
    
    private String extractUri(String line) {
        String[] parts = line.split(":");
        if (parts.length > 1) {
            return parts[1].trim().replaceAll("[\"\']", "");
        }
        return null;
    }
    
    private String extractServiceNameFromUri(String uri, List<ServiceInfo> allServices) {
        // Handle lb://service-name format
        if (uri.startsWith("lb://")) {
            return uri.substring(5);
        }
        
        // Handle http://service-name format
        if (uri.startsWith(AnalyzerConstants.HTTP_PREFIX)) {
            String serviceName = uri.substring(7).split("/")[0].split(":")[0];
            return serviceName;
        }
        
        return null;
    }
    
    private List<ServiceDependency> scanMessagingDependencies(Path servicePath, List<ServiceInfo> allServices) {
        List<ServiceDependency> dependencies = new ArrayList<>();
        
        // Look for messaging patterns in Java files
        try {
            PathMatcher javaMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.java");
            
            try (var stream = Files.walk(servicePath)) {
                List<Path> javaFiles = stream
                    .filter(javaMatcher::matches)
                    .collect(Collectors.toList());
                
                for (Path javaFile : javaFiles) {
                    String content = Files.readString(javaFile);
                    
                    for (String messagingPattern : config.getDependencyPatterns().getMessagingQueues()) {
                        if (content.contains(messagingPattern)) {
                            // This indicates messaging dependency but hard to determine target service
                            // We'll add it as a general messaging dependency
                            ServiceDependency dependency = new ServiceDependency(
                                "messaging-system",
                                "messaging",
                                "Uses messaging pattern: " + messagingPattern
                            );
                            dependency.setSourceFile(servicePath.relativize(javaFile).toString());
                            dependencies.add(dependency);
                            break;
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error scanning messaging dependencies: {}", e.getMessage(), e);
        }
        
        return dependencies;
    }
}