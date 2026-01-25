package com.example.analyzer.scanner;

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

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class GenericDependencyScanner {
    
    private final AnalyzerConfiguration config;
    private final JavaParser javaParser = new JavaParser();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    
    public GenericDependencyScanner(AnalyzerConfiguration config) {
        this.config = config;
    }
    
    public List<ServiceDependency> scanDependencies(ServiceInfo service, List<ServiceInfo> allServices) {
        List<ServiceDependency> dependencies = new ArrayList<>();
        
        Path servicePath = Paths.get(service.getPath());
        
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
            System.err.println("Error scanning dependencies for " + service.getName() + ": " + e.getMessage());
        }
        
        return dependencies;
    }
    
    private List<ServiceDependency> scanJavaFiles(Path servicePath, List<ServiceInfo> allServices) {
        List<ServiceDependency> dependencies = new ArrayList<>();
        
        System.out.println("URLTEST: *** Scanning Java files in service path: " + servicePath);
        
        try {
            PathMatcher javaMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.java");
            
            try (var stream = Files.walk(servicePath)) {
                List<Path> javaFiles = stream
                    .filter(javaMatcher::matches)
                    .filter(path -> !path.toString().contains("test"))
                    .collect(Collectors.toList());
                
                System.out.println("URLTEST: Found " + javaFiles.size() + " Java files in " + servicePath);
                for (Path javaFile : javaFiles) {
                    System.out.println("URLTEST: Processing Java file: " + javaFile);
                    dependencies.addAll(analyzeJavaFile(javaFile, servicePath, allServices));
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error scanning Java files in path: " + servicePath);
            System.err.println("Exception: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("URLTEST: Found " + dependencies.size() + " dependencies from " + servicePath);
        return dependencies;
    }
    
    private List<ServiceDependency> analyzeJavaFile(Path javaFile, Path servicePath, List<ServiceInfo> allServices) {
        List<ServiceDependency> dependencies = new ArrayList<>();
        
        try {
            String fileName = javaFile.getFileName().toString();
            String filePath = javaFile.toString();
            
            // Only debug OrderNotificationService for now
            boolean isTargetFile = filePath.contains("OrderNotificationService") || filePath.contains("PaymentNotificationService") || filePath.contains("UserActivityService");
            
            if (isTargetFile) {
                System.out.println("URLTEST: *** TARGET FILE - Analyzing Java file: " + fileName + " at " + filePath);
            }
            
            CompilationUnit cu = javaParser.parse(javaFile).getResult().orElse(null);
            if (cu == null) {
                if (isTargetFile) {
                    System.out.println("URLTEST: *** TARGET FILE - Failed to parse: " + fileName);
                }
                return dependencies;
            }
            
            if (isTargetFile) {
                System.out.println("URLTEST: *** TARGET FILE - Successfully parsed: " + fileName);
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
            if (isTargetFile) {
                System.out.println("URLTEST: *** TARGET FILE - Looking for methods in: " + fileName);
            }
            var methods = cu.findAll(MethodDeclaration.class);
            if (isTargetFile) {
                System.out.println("URLTEST: *** TARGET FILE - Found " + methods.size() + " methods in: " + fileName);
            }
            
            cu.findAll(MethodDeclaration.class).forEach(method -> {
                String methodBody = method.toString();
                if (isTargetFile) {
                    System.out.println("URLTEST: *** TARGET FILE - Found method: " + method.getNameAsString() + " in " + fileName);
                }
                
                for (String restPattern : config.getDependencyPatterns().getRestTemplates()) {
                    if (isTargetFile) {
                        System.out.println("URLTEST: *** TARGET FILE - Checking RestTemplate pattern: " + restPattern + " in method " + method.getNameAsString());
                    }
                    if (methodBody.contains(restPattern)) {
                        if (isTargetFile) {
                            System.out.println("URLTEST: *** TARGET FILE - PATTERN MATCH! " + restPattern + " found in method " + method.getNameAsString());
                        }
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
                    if (annotation.getNameAsString().equals("RestController")) {
                        extractControllerEndpoints(classDecl, javaFile, servicePath).forEach(endpoint -> {
                            // Add to service exposed endpoints
                            // This is for future reference
                        });
                    }
                });
            });
            
        } catch (Exception e) {
            System.err.println("Error analyzing Java file " + javaFile + ": " + e.getMessage());
        }
        
        return dependencies;
    }
    
    private ServiceDependency extractFeignDependency(AnnotationExpr annotation, Path javaFile, Path servicePath, List<ServiceInfo> allServices) {
        try {
            String annotationStr = annotation.toString();
            
            // Extract service name from @FeignClient annotation
            String serviceName = null;
            if (annotationStr.contains("name") || annotationStr.contains("value")) {
                // Parse @FeignClient(name = "service-name")
                String[] parts = annotationStr.split("[\"\']");
                for (int i = 0; i < parts.length - 1; i++) {
                    if (parts[i].contains("name") || parts[i].contains("value")) {
                        serviceName = parts[i + 1];
                        break;
                    }
                }
            }
            
            if (serviceName != null) {
                String relativeFile = servicePath.relativize(javaFile).toString();
                ServiceDependency dependency = new ServiceDependency(
                    serviceName, 
                    "feign-client", 
                    "Feign client call to " + serviceName
                );
                dependency.setSourceFile(relativeFile);
                dependency.setLineNumber(annotation.getBegin().map(pos -> pos.line).orElse(null));
                return dependency;
            }
            
        } catch (Exception e) {
            System.err.println("Error extracting Feign dependency: " + e.getMessage());
        }
        
        return null;
    }
    
    private ServiceDependency extractRestTemplateDependency(MethodDeclaration method, Path javaFile, Path servicePath, List<ServiceInfo> allServices) {
        try {
            String methodBody = method.toString();
            String fileName = javaFile.getFileName().toString();
            
            // Look for URL patterns in RestTemplate calls - using direct text search
            AtomicReference<String> foundUrl = new AtomicReference<>();
            
            // Simple approach: just search the method text directly for HTTP URLs
            String[] lines = methodBody.split("\n");
            System.out.println("URLTEST: Analyzing method " + method.getNameAsString() + " in " + fileName + " with " + lines.length + " lines");
            
            for (String line : lines) {
                String trimmed = line.trim();
                
                // Look for any HTTP URLs in the line
                if (trimmed.contains("http://") || trimmed.contains("https://")) {
                    System.out.println("URLTEST: Found HTTP line: " + trimmed);
                    String url = extractUrlFromLine(trimmed);
                    if (url != null) {
                        System.out.println("URLTEST: Extracted URL: " + url);
                        foundUrl.set(url);
                        break;
                    } else {
                        System.out.println("URLTEST: Failed to extract URL from: " + trimmed);
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
                            "rest-template"          // dependencyType
                        );
                        dependency.setDescription("REST call to " + targetServiceName);
                        dependency.setSourceFile(relativeFile);
                        dependency.setEndpoint(foundUrl.get());
                        System.out.println("URLTEST: *** CREATED DEPENDENCY: " + sourceServiceName + " -> " + targetServiceName + " from " + foundUrl.get());
                        return dependency;
                    } else {
                        System.out.println("URLTEST: Could not extract target service name from URL: " + foundUrl.get());
                    }
                } else {
                    System.out.println("URLTEST: Method has URL but no RestTemplate/WebClient");
                }
            } else {
                System.out.println("URLTEST: No URLs found in method " + method.getNameAsString());
            }
            
        } catch (Exception e) {
            System.err.println("Error extracting RestTemplate dependency: " + e.getMessage());
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
        if (trimmed.contains("http://") || trimmed.contains("https://")) {
            System.out.println("URLTEST: Processing line: " + trimmed);
            
            // Pattern 1: String variable assignment like 'String emailServiceUrl = "http://email-service:8087/api/email/send";'
            if (trimmed.contains("= \"http")) {
                int startQuote = trimmed.indexOf("\"http");
                int endQuote = trimmed.indexOf("\"", startQuote + 1);
                if (startQuote >= 0 && endQuote > startQuote) {
                    String url = trimmed.substring(startQuote + 1, endQuote);
                    System.out.println("URLTEST: Extracted URL (pattern 1) from: " + trimmed + " -> " + url);
                    return url;
                }
            }
            
            // Pattern 2: Method call with URI like '.uri("http://user-service/api/users/count")'
            if (trimmed.contains(".uri(\"http")) {
                int startQuote = trimmed.indexOf("\"http");
                int endQuote = trimmed.indexOf("\"", startQuote + 1);
                if (startQuote >= 0 && endQuote > startQuote) {
                    String url = trimmed.substring(startQuote + 1, endQuote);
                    System.out.println("URLTEST: Extracted URL (pattern 2) from: " + trimmed + " -> " + url);
                    return url;
                }
            }
            
            // Pattern 3: General quote-based extraction (fallback)
            int httpPos = trimmed.indexOf("http://");
            if (httpPos == -1) {
                httpPos = trimmed.indexOf("https://");
            }
            
            // Look for quotes around the URL
            int startQuote = trimmed.lastIndexOf('"', httpPos);
            int endQuote = trimmed.indexOf('"', httpPos);
            
            if (startQuote >= 0 && endQuote > httpPos) {
                String url = trimmed.substring(startQuote + 1, endQuote);
                // Remove any trailing characters that might be after the closing quote
                url = url.replaceAll("[;,)\"'\\s]*$", "");
                System.out.println("URLTEST: Extracted URL (pattern 3) from: " + trimmed + " -> " + url);
                return url;
            }
            
            // If no quotes, try to extract URL differently 
            // Look for URL patterns with common delimiters
            String[] words = trimmed.split("\\s+");
            for (String word : words) {
                if (word.contains("http://") || word.contains("https://")) {
                    // Clean up any trailing characters
                    word = word.replaceAll("[;,)\"']*$", "");
                    if (word.startsWith("\"")) {
                        word = word.substring(1);
                    }
                    System.out.println("URLTEST: Extracted URL (no quotes) from: " + trimmed + " -> " + word);
                    return word;
                }
            }
            
            System.out.println("URLTEST: Failed to extract URL from: " + trimmed);
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
            System.err.println("Error scanning configuration files: " + e.getMessage());
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
            System.err.println("Error analyzing config file: " + e.getMessage());
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
            System.err.println("Error extracting gateway routes: " + e.getMessage());
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
        if (uri.startsWith("http://")) {
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
            System.err.println("Error scanning messaging dependencies: " + e.getMessage());
        }
        
        return dependencies;
    }
}