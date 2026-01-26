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

import java.io.FileReader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class GenericDependencyScanner {

    private static final Logger logger = LoggerFactory.getLogger(GenericDependencyScanner.class);

    private final AnalyzerConfiguration config;
    private final JavaParser javaParser = new JavaParser();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private Map<String, String> serviceProperties = new HashMap<>();
    private Map<String, List<String>> serviceEndpointsMap = new HashMap<>(); // service -> list of endpoints
    
    public GenericDependencyScanner(AnalyzerConfiguration config) {
        this.config = config;
    }
    
    /**
     * Build a map of all internal services and their endpoints FIRST (ENDPOINT-FIRST STRATEGY)
     * This allows us to:
     * 1. Match HTTP calls to services by endpoint (most accurate!)
     * 2. Validate dependencies against actual endpoints
     * 3. Filter out external API calls
     * 
     * Strategy: Scan all controller classes in each service, extract all API endpoints
     */
    public void buildServiceEndpointsMap(List<ServiceInfo> allServices, Path projectRoot) {
        serviceEndpointsMap.clear();
        
        int totalEndpoints = 0;
        logger.info("📋 Building endpoint map for {} services...", allServices.size());
        
        for (ServiceInfo service : allServices) {
            Path servicePath = projectRoot.resolve(service.getPath());
            List<String> endpoints = extractServiceEndpoints(servicePath);
            serviceEndpointsMap.put(service.getName(), endpoints);
            
            if (!endpoints.isEmpty()) {
                logger.info("   ✓ {}: {} endpoints", service.getName(), endpoints.size());
                for (String endpoint : endpoints) {
                    logger.debug("      - {}", endpoint);
                }
                totalEndpoints += endpoints.size();
            } else {
                logger.debug("   ○ {}: no endpoints found (might not expose REST APIs)", service.getName());
            }
        }
        
        logger.info("📊 Total endpoints mapped: {} across {} services", totalEndpoints, allServices.size());
    }
    
    /**
     * Extract all controller endpoints from a service
     */
    private List<String> extractServiceEndpoints(Path servicePath) {
        List<String> endpoints = new ArrayList<>();
        
        try {
            PathMatcher javaMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.java");
            
            try (var stream = Files.walk(servicePath)) {
                List<Path> javaFiles = stream
                    .filter(javaMatcher::matches)
                    .filter(path -> !path.toString().contains("test"))
                    .collect(Collectors.toList());
                
                for (Path javaFile : javaFiles) {
                    endpoints.addAll(extractEndpointsFromController(javaFile));
                }
            }
        } catch (Exception e) {
            logger.debug("Error extracting endpoints from {}: {}", servicePath, e.getMessage());
        }
        
        return endpoints;
    }
    
    /**
     * Extract endpoints from @RestController classes
     * Handles @RequestMapping, @GetMapping, @PostMapping, etc.
     */
    private List<String> extractEndpointsFromController(Path javaFile) {
        List<String> endpoints = new ArrayList<>();
        
        try {
            CompilationUnit cu = javaParser.parse(javaFile).getResult().orElse(null);
            if (cu == null) {
                return endpoints;
            }
            
            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                // Check if it's a controller
                boolean isController = classDecl.getAnnotations().stream()
                    .anyMatch(ann -> ann.getNameAsString().contains("RestController") || 
                                   ann.getNameAsString().contains("Controller"));
                
                if (isController) {
                    // Get base path from class-level @RequestMapping
                    final String[] basePathHolder = {""};
                    for (AnnotationExpr ann : classDecl.getAnnotations()) {
                        if (ann.getNameAsString().contains("RequestMapping")) {
                            String path = extractPathFromAnnotation(ann.toString());
                            if (path != null) {
                                basePathHolder[0] = path;
                            }
                        }
                    }
                    
                    // Get all method-level mappings
                    String finalBasePath = basePathHolder[0];
                    classDecl.getMethods().forEach(method -> {
                        for (AnnotationExpr ann : method.getAnnotations()) {
                            String annName = ann.getNameAsString();
                            if (annName.contains("Mapping")) { // Covers all *Mapping annotations
                                String methodPath = extractPathFromAnnotation(ann.toString());
                                if (methodPath != null) {
                                    String fullPath = combinePaths(finalBasePath, methodPath);
                                    endpoints.add(fullPath);
                                    logger.debug("Found endpoint: {}", fullPath);
                                }
                            }
                        }
                    });
                }
            });
            
        } catch (Exception e) {
            logger.debug("Error parsing controller file {}: {}", javaFile, e.getMessage());
        }
        
        return endpoints;
    }
    
    /**
     * Extract path from mapping annotation like @GetMapping("/users") or @RequestMapping(value = "/users")
     */
    private String extractPathFromAnnotation(String annotation) {
        // Try to find path in quotes
        String[] parts = annotation.split("[\"\']");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.startsWith("/")) {
                return part;
            }
        }
        return null;
    }
    
    /**
     * Combine base path and method path
     */
    private String combinePaths(String basePath, String methodPath) {
        if (basePath.isEmpty()) {
            return methodPath;
        }
        if (methodPath.isEmpty()) {
            return basePath;
        }
        // Ensure no double slashes
        if (basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        if (!methodPath.startsWith("/")) {
            methodPath = "/" + methodPath;
        }
        return basePath + methodPath;
    }
    
    public List<ServiceDependency> scanDependencies(ServiceInfo service, List<ServiceInfo> allServices, Path projectRoot) {
        List<ServiceDependency> dependencies = new ArrayList<>();
        
        Path servicePath = projectRoot.resolve(service.getPath());
        
        try {
            // Load service properties first (for resolving ${...} placeholders)
            loadServiceProperties(servicePath);
            
            // Scan pom.xml for Maven dependencies (catches library/module dependencies)
            if ("java".equals(service.getLanguage())) {
                dependencies.addAll(scanMavenDependencies(servicePath, allServices, projectRoot));
            }
            
            // Scan Java files for Feign clients, REST templates, etc.
            if ("java".equals(service.getLanguage())) {
                dependencies.addAll(scanJavaFiles(servicePath, allServices));
            }
            
            // Scan configuration files for gateway routes, etc.
            dependencies.addAll(scanConfigurationFiles(servicePath, allServices));
            
            // Scan for messaging dependencies
            dependencies.addAll(scanMessagingDependencies(servicePath, allServices));
            
            // ENDPOINT-FIRST DETECTION: Search for this service using other services' endpoints
            dependencies.addAll(scanForEndpointUsageByThisService(service, allServices, projectRoot));
            
            // Deduplicate dependencies: only one arrow per source->target pair
            dependencies = deduplicateDependencies(dependencies);
            
        } catch (Exception e) {
            logger.error("Error scanning dependencies for {}: {}", service.getName(), e.getMessage(), e);
        }
        
        return dependencies;
    }
    
    /**
     * Scan pom.xml for dependencies on other services in the same project
     * Catches library/module dependencies where one service depends on another as a JAR
     */
    private List<ServiceDependency> scanMavenDependencies(Path servicePath, List<ServiceInfo> allServices, Path projectRoot) {
        List<ServiceDependency> dependencies = new ArrayList<>();
        
        Path pomFile = servicePath.resolve("pom.xml");
        if (!Files.exists(pomFile)) {
            logger.debug("No pom.xml found in {}", servicePath);
            return dependencies;
        }
        
        try {
            org.apache.maven.model.io.xpp3.MavenXpp3Reader reader = new org.apache.maven.model.io.xpp3.MavenXpp3Reader();
            org.apache.maven.model.Model model = reader.read(new FileReader(pomFile.toFile()));
            
            String sourceServiceName = servicePath.getFileName().toString();
            
            // Get source service groupId for logging
            String sourceGroupId = model.getGroupId() != null ? model.getGroupId() : 
                                  (model.getParent() != null ? model.getParent().getGroupId() : null);
            
            logger.debug("📦 Scanning Maven dependencies for {} (groupId: {})", sourceServiceName, sourceGroupId);
            
            int dependencyCount = model.getDependencies().size();
            logger.debug("   Found {} dependencies in pom.xml", dependencyCount);
            
            // Get all dependencies from pom.xml
            for (org.apache.maven.model.Dependency dep : model.getDependencies()) {
                String groupId = dep.getGroupId();
                String artifactId = dep.getArtifactId();
                String scope = dep.getScope();
                
                logger.debug("   Checking dependency: {}:{} (scope: {})", groupId, artifactId, scope);
                
                // Skip test and provided scope dependencies
                if ("test".equals(scope) || "provided".equals(scope)) {
                    logger.debug("   ⏭️  Skipping {} scope dependency: {}", scope, artifactId);
                    continue;
                }
                
                boolean matched = false;
                // Check if this dependency matches any of our internal services
                for (ServiceInfo targetService : allServices) {
                    if (isMatchingService(targetService, groupId, artifactId, projectRoot)) {
                        ServiceDependency dependency = new ServiceDependency(
                            sourceServiceName,
                            targetService.getName(),
                            "maven-dependency"
                        );
                        dependency.setDescription("Maven dependency on " + targetService.getName());
                        dependency.setSourceFile("pom.xml");
                        
                        logger.info("✅ Found Maven dependency: {} -> {} ({}:{})", 
                            sourceServiceName, targetService.getName(), groupId, artifactId);
                        
                        dependencies.add(dependency);
                        matched = true;
                        break;
                    }
                }
                
                if (!matched && groupId != null && groupId.equals(sourceGroupId)) {
                    logger.debug("   ⚠️  Potential internal dependency not matched: {}:{}", groupId, artifactId);
                    logger.debug("      Available services: {}", allServices.stream()
                        .map(ServiceInfo::getName).collect(java.util.stream.Collectors.joining(", ")));
                }
            }
            
        } catch (Exception e) {
            logger.warn("Error scanning Maven dependencies in {}: {}", pomFile, e.getMessage());
        }
        
        return dependencies;
    }
    
    /**
     * Check if a Maven dependency matches an internal service
     * Matches by artifactId or service folder name
     */
    private boolean isMatchingService(ServiceInfo service, String groupId, String artifactId, Path projectRoot) {
        String serviceName = service.getName();
        
        // Direct match by artifactId
        if (artifactId != null && artifactId.equals(serviceName)) {
            logger.debug("      ✓ Direct match: {} == {}", artifactId, serviceName);
            return true;
        }
        
        // Fuzzy match by artifactId (normalized)
        if (artifactId != null) {
            String normalizedArtifact = normalizeServiceName(artifactId);
            String normalizedService = normalizeServiceName(serviceName);
            if (normalizedArtifact.equals(normalizedService)) {
                logger.debug("      ✓ Fuzzy match: {} == {} (normalized: {} == {})", 
                    artifactId, serviceName, normalizedArtifact, normalizedService);
                return true;
            }
        }
        
        // Try to read the target service's pom.xml and match groupId:artifactId
        Path targetPomFile = projectRoot.resolve(service.getPath()).resolve("pom.xml");
        if (Files.exists(targetPomFile)) {
            try {
                org.apache.maven.model.io.xpp3.MavenXpp3Reader reader = new org.apache.maven.model.io.xpp3.MavenXpp3Reader();
                org.apache.maven.model.Model targetModel = reader.read(new FileReader(targetPomFile.toFile()));
                
                String targetGroupId = targetModel.getGroupId() != null ? targetModel.getGroupId() : 
                                      (targetModel.getParent() != null ? targetModel.getParent().getGroupId() : null);
                String targetArtifactId = targetModel.getArtifactId();
                
                // Exact match by groupId:artifactId
                if (groupId != null && groupId.equals(targetGroupId) && 
                    artifactId != null && artifactId.equals(targetArtifactId)) {
                    logger.debug("      ✓ Exact match: {}:{} == {}:{}", 
                        groupId, artifactId, targetGroupId, targetArtifactId);
                    return true;
                }
                
            } catch (Exception e) {
                logger.debug("      Error reading target pom.xml for {}: {}", serviceName, e.getMessage());
            }
        }
        
        return false;
    }
    
    /**
     * Deduplicate dependencies so that service A -> service B appears only once
     * Even if there are multiple Feign client methods or multiple RestTemplate calls
     */
    private List<ServiceDependency> deduplicateDependencies(List<ServiceDependency> dependencies) {
        Map<String, ServiceDependency> uniqueDeps = new LinkedHashMap<>();
        
        for (ServiceDependency dep : dependencies) {
            String key = dep.getFromService() + " -> " + dep.getTargetService();
            
            // Keep the first occurrence, or prefer feign-client over other types
            if (!uniqueDeps.containsKey(key)) {
                uniqueDeps.put(key, dep);
            } else {
                ServiceDependency existing = uniqueDeps.get(key);
                // Prefer feign-client type over others, then maven-dependency
                if ("feign-client".equals(dep.getDependencyType()) && 
                    !"feign-client".equals(existing.getDependencyType())) {
                    logger.info("🔄 Replacing {} with {} for {}", existing.getDependencyType(), dep.getDependencyType(), key);
                    uniqueDeps.put(key, dep);
                } else if ("maven-dependency".equals(dep.getDependencyType()) && 
                          !"feign-client".equals(existing.getDependencyType()) &&
                          !"maven-dependency".equals(existing.getDependencyType())) {
                    logger.info("🔄 Replacing {} with {} for {}", existing.getDependencyType(), dep.getDependencyType(), key);
                    uniqueDeps.put(key, dep);
                } else {
                    logger.info("⏭️  Skipping duplicate {}: {} (keeping {})", key, dep.getDependencyType(), existing.getDependencyType());
                }
            }
        }
        
        logger.info("📊 Deduplicated dependencies: {} -> {} unique", dependencies.size(), uniqueDeps.size());
        
        return new ArrayList<>(uniqueDeps.values());
    }
    
    /**
     * ENDPOINT-FIRST DETECTION STRATEGY
     * 
     * For each OTHER service that exposes endpoints:
     * 1. Get their endpoint paths
     * 2. Search if THIS service's code references those endpoints
     * 3. If found, create dependency: this -> other
     * 
     * This detects dependencies based on actual endpoint usage rather than service name matching.
     */
    private List<ServiceDependency> scanForEndpointUsageByThisService(ServiceInfo sourceService, List<ServiceInfo> allServices, Path projectRoot) {
        List<ServiceDependency> dependencies = new ArrayList<>();
        
        String sourceServiceName = sourceService.getName();
        Path sourceServicePath = projectRoot.resolve(sourceService.getPath());
        
        logger.debug("🔍 Checking if {} calls endpoints from other services", sourceServiceName);
        
        // For each OTHER service, check if THIS service calls their endpoints
        for (ServiceInfo targetService : allServices) {
            if (targetService.getName().equals(sourceServiceName)) {
                continue; // Skip self
            }
            
            String targetServiceName = targetService.getName();
            
            // Get all endpoints exposed by the target service
            List<String> targetEndpoints = serviceEndpointsMap.getOrDefault(targetServiceName, new ArrayList<>());
            
            if (targetEndpoints.isEmpty()) {
                continue; // No endpoints to check
            }
            
            // Search if THIS service's code contains any of the target's endpoints
            for (String endpoint : targetEndpoints) {
                if (searchForEndpointInService(endpoint, sourceServicePath, targetServiceName)) {
                    // Found! Create dependency: this -> target
                    ServiceDependency dependency = new ServiceDependency(
                        sourceServiceName,
                        targetServiceName,
                        "endpoint-call"
                    );
                    dependency.setDescription("Calls endpoint " + endpoint + " on " + targetServiceName);
                    dependencies.add(dependency);
                    
                    logger.info("✅ Found endpoint usage: {} calls {} on {}", 
                        sourceServiceName, endpoint, targetServiceName);
                    
                    // Don't need to check other endpoints for this service pair
                    break;
                }
            }
        }
        
        return dependencies;
    }
    
    /**
     * Search for an endpoint string in a service's Java files.
     * Returns true if found in files containing Feign/RestTemplate/WebClient indicators.
     */
    private boolean searchForEndpointInService(String endpoint, Path servicePath, String targetServiceName) {
        logger.debug("   🔎 Searching for endpoint '{}' in {}", endpoint, servicePath.getFileName());
        
        try {
            PathMatcher javaMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.java");
            
            try (var stream = Files.walk(servicePath)) {
                List<Path> javaFiles = stream
                    .filter(javaMatcher::matches)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
                
                logger.debug("      Checking {} Java files", javaFiles.size());
                
                for (Path javaFile : javaFiles) {
                    String content = Files.readString(javaFile);
                    
                    // Check if this file contains REST client code
                    boolean isRestClientFile = content.contains("@FeignClient") || 
                                             content.contains("RestTemplate") ||
                                             content.contains("WebClient") ||
                                             content.contains("@GetMapping") ||
                                             content.contains("@PostMapping") ||
                                             content.contains("@PutMapping") ||
                                             content.contains("@DeleteMapping");
                    
                    if (isRestClientFile && content.contains("\"" + endpoint + "\"")) {
                        logger.info("   🎯 Found endpoint '{}' in {}", endpoint, javaFile.getFileName());
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Error searching for endpoint in {}: {}", servicePath, e.getMessage());
        }
        
        logger.debug("      Endpoint '{}' not found in {}", endpoint, servicePath.getFileName());
        return false;
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
                        // Get the parent interface/class to extract method mappings
                        com.github.javaparser.ast.Node parent = annotation.getParentNode().orElse(null);
                        ServiceDependency dependency = extractFeignDependency(annotation, parent, javaFile, servicePath, allServices);
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
    
    private ServiceDependency extractFeignDependency(AnnotationExpr annotation, com.github.javaparser.ast.Node parent, Path javaFile, Path servicePath, List<ServiceInfo> allServices) {
        try {
            String annotationStr = annotation.toString();
            String sourceServiceName = servicePath.getFileName().toString();
            
            logger.info("🔍 Analyzing Feign client in {}: {}", sourceServiceName, annotationStr);
            
            // Extract BOTH name and url from @FeignClient annotation
            // Supports patterns like:
            // @FeignClient(name = "service-name")
            // @FeignClient(value = "service-name")
            // @FeignClient("service-name")
            // @FeignClient(name = "service-name", url = "${service.url}")
            String targetServiceName = null;
            String targetServiceUrl = null;
            
            // Parse annotation to extract both name and url
            String[] parts = annotationStr.split("[\"\']");
            for (int i = 0; i < parts.length - 1; i++) {
                String part = parts[i];
                if (part.contains("name") || part.contains("value") || part.contains("FeignClient(")) {
                    if (targetServiceName == null) {
                        targetServiceName = parts[i + 1];
                        logger.debug("   📝 Extracted raw name: '{}'", targetServiceName);
                    }
                }
                if (part.contains("url")) {
                    targetServiceUrl = parts[i + 1];
                    logger.debug("   🌐 Extracted raw url: '{}'", targetServiceUrl);
                }
            }
            
            // Extract endpoint paths from Feign client methods
            List<String> endpointPaths = new ArrayList<>();
            if (parent instanceof com.github.javaparser.ast.body.ClassOrInterfaceDeclaration) {
                com.github.javaparser.ast.body.ClassOrInterfaceDeclaration classDecl = 
                    (com.github.javaparser.ast.body.ClassOrInterfaceDeclaration) parent;
                
                classDecl.getMethods().forEach(method -> {
                    method.getAnnotations().forEach(methodAnnotation -> {
                        String methodAnnName = methodAnnotation.getNameAsString();
                        if (methodAnnName.contains("Mapping")) { // GetMapping, PostMapping, PutMapping, etc.
                            String methodAnnStr = methodAnnotation.toString();
                            String[] methodParts = methodAnnStr.split("[\"\']");
                            if (methodParts.length > 1) {
                                String endpoint = methodParts[1];
                                if (endpoint.startsWith("/")) {
                                    endpointPaths.add(endpoint);
                                    logger.debug("   📍 Found method endpoint: {}", endpoint);
                                }
                            }
                        }
                    });
                });
            }
            
            // STRATEGY 1: Try URL-based endpoint matching FIRST (Most Accurate!)
            String matchedServiceName = null;
            
            if (targetServiceUrl != null && !targetServiceUrl.trim().isEmpty()) {
                targetServiceUrl = targetServiceUrl.trim();
                
                // Resolve property placeholders in URL like ${feign.ccg.url}
                if (targetServiceUrl.startsWith("${") && targetServiceUrl.endsWith("}")) {
                    String propertyKey = targetServiceUrl.substring(2, targetServiceUrl.length() - 1);
                    logger.debug("   🔑 Resolving URL property: {}", propertyKey);
                    
                    String resolvedValue = resolveProperty(propertyKey);
                    if (resolvedValue != null) {
                        logger.info("   ✅ Resolved {} = '{}' -> '{}'", propertyKey, targetServiceUrl, resolvedValue);
                        targetServiceUrl = resolvedValue;
                    } else {
                        logger.warn("   ⚠️  URL Property '{}' not found in config files!", propertyKey);
                        targetServiceUrl = null;
                    }
                }
                
                // Build full endpoint paths by combining URL base path with method paths
                if (targetServiceUrl != null && !endpointPaths.isEmpty()) {
                    String basePath = extractPathFromUrl(targetServiceUrl);
                    logger.debug("   🔗 Base path from URL: '{}'", basePath);
                    
                    for (String methodPath : endpointPaths) {
                        String fullPath = basePath.isEmpty() ? methodPath : basePath + methodPath;
                        logger.info("   🎯 Combined endpoint: {} + {} = {}", basePath, methodPath, fullPath);
                        
                        // Try to match this combined endpoint to a service
                        matchedServiceName = extractServiceNameFromUrl(targetServiceUrl, fullPath, allServices);
                        if (matchedServiceName != null) {
                            logger.info("   ✅ Matched via endpoint lookup: {} -> {}", fullPath, matchedServiceName);
                            break;
                        }
                    }
                }
                
                // If no method paths or no match yet, try just the URL
                if (matchedServiceName == null && targetServiceUrl != null) {
                    logger.debug("   🎯 Using URL for endpoint-first matching: '{}'", targetServiceUrl);
                    matchedServiceName = extractServiceNameFromUrl(targetServiceUrl, null, allServices);
                    if (matchedServiceName != null) {
                        logger.info("   ✅ Matched via URL endpoint lookup: {} -> {}", targetServiceUrl, matchedServiceName);
                    }
                }
            }
            
            // STRATEGY 2: Fall back to name-based fuzzy matching
            if (matchedServiceName == null && targetServiceName != null && !targetServiceName.trim().isEmpty()) {
                targetServiceName = targetServiceName.trim();
                
                // Resolve property placeholders like ${feign.taskservice.name}
                if (targetServiceName.startsWith("${") && targetServiceName.endsWith("}")) {
                    String propertyKey = targetServiceName.substring(2, targetServiceName.length() - 1);
                    logger.debug("   🔑 Resolving name property: {}", propertyKey);
                    logger.debug("   📦 Available properties: {}", serviceProperties.keySet());
                    
                    String resolvedValue = resolveProperty(propertyKey);
                    if (resolvedValue != null) {
                        logger.info("   ✅ Resolved {} = '{}' -> '{}'", propertyKey, targetServiceName, resolvedValue);
                        targetServiceName = resolvedValue;
                    } else {
                        logger.warn("   ⚠️  Name Property '{}' not found in config files!", propertyKey);
                        logger.warn("   💡 Available properties: {}", 
                            serviceProperties.isEmpty() ? "NONE - config files not loaded?" : 
                            String.join(", ", serviceProperties.keySet()));
                        return null; // Can't resolve, skip this dependency
                    }
                }
                
                logger.debug("   🎯 Using name for fuzzy matching: '{}'", targetServiceName);
                
                // Try to match with actual service names using fuzzy matching
                matchedServiceName = findMatchingServiceName(targetServiceName, allServices);
                if (matchedServiceName == null) {
                    logger.warn("   ❌ No matching service found for '{}'", targetServiceName);
                    matchedServiceName = targetServiceName; // Keep original if no match found
                } else {
                    logger.info("   ✅ Matched via fuzzy name matching: {} -> {}", targetServiceName, matchedServiceName);
                }
            }
            
            if (matchedServiceName != null) {
                String relativeFile = servicePath.relativize(javaFile).toString();
                ServiceDependency dependency = new ServiceDependency(
                    sourceServiceName,        // fromService
                    matchedServiceName,       // targetService (matched to actual service)
                    "feign-client"           // dependencyType
                );
                dependency.setDescription("Feign client call to " + matchedServiceName);
                dependency.setSourceFile(relativeFile);
                dependency.setLineNumber(annotation.getBegin().map(pos -> pos.line).orElse(null));
                
                logger.info("✅ Found Feign dependency: {} -> {}", sourceServiceName, matchedServiceName);
                
                return dependency;
            } else {
                logger.warn("   ❌ Could not determine target service from Feign client annotation");
                return null;
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
     * - ccg-core-service -> task management service (word matching)
     */
    private String findMatchingServiceName(String feignClientName, List<ServiceInfo> allServices) {
        logger.info("🔍 Fuzzy matching '{}' against {} services", feignClientName, allServices.size());
        logger.debug("   Available services: {}", allServices.stream().map(ServiceInfo::getName).collect(java.util.stream.Collectors.joining(", ")));
        
        // Direct match first
        for (ServiceInfo service : allServices) {
            if (service.getName().equals(feignClientName)) {
                logger.info("   ✓ Direct match: '{}' == '{}'", feignClientName, service.getName());
                return service.getName();
            }
        }
        
        // Normalize the feign client name for comparison
        String normalizedFeignName = normalizeServiceName(feignClientName);
        logger.debug("   Normalized '{}' → '{}'", feignClientName, normalizedFeignName);
        
        // Try normalized matching
        for (ServiceInfo service : allServices) {
            String normalizedServiceName = normalizeServiceName(service.getName());
            if (normalizedServiceName.equals(normalizedFeignName)) {
                logger.info("   ✓ Normalized match: '{}' ('{}') == '{}' ('{}')", 
                    feignClientName, normalizedFeignName, service.getName(), normalizedServiceName);
                return service.getName();
            }
        }
        
        // Try partial matching (contains) - handles ccg-service -> ccg-core-service
        for (ServiceInfo service : allServices) {
            String normalizedServiceName = normalizeServiceName(service.getName());
            
            // Check if service name contains feign name (ccg core contains ccg)
            if (normalizedServiceName.contains(normalizedFeignName) && normalizedFeignName.length() > 2) {
                logger.info("   ✓ Partial match (service contains feign): '{}' ('{}') contains '{}' ('{}')", 
                    service.getName(), normalizedServiceName, feignClientName, normalizedFeignName);
                return service.getName();
            }
            
            // Check if feign name contains service name  
            if (normalizedFeignName.contains(normalizedServiceName) && normalizedServiceName.length() > 2) {
                logger.info("   ✓ Partial match (feign contains service): '{}' ('{}') contains '{}' ('{}')", 
                    feignClientName, normalizedFeignName, service.getName(), normalizedServiceName);
                return service.getName();
            }
        }
        
        // Try prefix/suffix matching - handles ccg-kafka-consumer -> ccg-kafka-consumer-service
        for (ServiceInfo service : allServices) {
            String serviceName = service.getName().toLowerCase();
            String feignName = feignClientName.toLowerCase();
            
            // Remove common suffixes for comparison
            String serviceBase = serviceName.replaceAll("[-_]service$", "");
            String feignBase = feignName.replaceAll("[-_]service$", "");
            
            if (serviceBase.equals(feignBase)) {
                logger.debug("✓ Base match: {} ({}) == {} ({})", 
                    feignClientName, feignBase, service.getName(), serviceBase);
                return service.getName();
            }
            
            // Check if one is prefix of other after removing -service suffix
            if (serviceBase.startsWith(feignBase) || feignBase.startsWith(serviceBase)) {
                logger.debug("✓ Prefix match: {} ({}) ~ {} ({})", 
                    feignClientName, feignBase, service.getName(), serviceBase);
                return service.getName();
            }
        }
        
        // Try word-based matching (for cases like "task" matching "task management service")
        String[] feignWords = normalizedFeignName.split("\\s+");
        for (ServiceInfo service : allServices) {
            String normalizedServiceName = normalizeServiceName(service.getName());
            String[] serviceWords = normalizedServiceName.split("\\s+");
            
            // Count matching words
            int matchCount = 0;
            for (String feignWord : feignWords) {
                if (feignWord.length() > 2) { // Only consider meaningful words
                    for (String serviceWord : serviceWords) {
                        if (serviceWord.equals(feignWord)) {
                            matchCount++;
                            break;
                        }
                    }
                }
            }
            
            // If majority of words match, consider it a match
            if (matchCount > 0 && matchCount >= feignWords.length / 2) {
                logger.debug("✓ Word-based match: {} ~ {} ({}/{} words matched)", 
                    feignClientName, service.getName(), matchCount, feignWords.length);
                return service.getName();
            }
        }
        
        // No match found
        logger.warn("❌ Could not find matching service for Feign client: '{}' (normalized: '{}'). Available services: {}", 
            feignClientName, normalizedFeignName, 
            allServices.stream().map(s -> s.getName() + " (" + normalizeServiceName(s.getName()) + ")").collect(java.util.stream.Collectors.joining(", ")));
        return null;
    }
    
    /**
     * Normalize service name for matching
     * Converts: TaskManagementService, task-management-service, task_management_service, task-lib
     * To: task management (removes common suffixes like "service", "lib", "api", "app")
     */
    private String normalizeServiceName(String serviceName) {
        return serviceName
            .toLowerCase()
            .replaceAll("[-_]", " ")  // Convert hyphens and underscores to spaces
            .replaceAll("\\b(service|lib|library|api|app|client|server)\\b", "")  // Remove common suffixes
            .replaceAll("\\s+", " ")  // Normalize multiple spaces to single space
            .trim();
    }
    
    /**
     * Load service properties from application-prd.yml, application-dev.yml, application.yml, etc.
     * Priority: prd > prod > dev > default
     */
    private void loadServiceProperties(Path servicePath) {
        serviceProperties.clear();
        
        String serviceName = servicePath.getFileName().toString();
        logger.debug("📂 Loading properties for service: {}", serviceName);
        
        // Priority order: prd -> prod -> dev -> default
        String[] configFiles = {
            "application-prd.yml",
            "application-prd.yaml",
            "application-prod.yml",
            "application-prod.yaml",
            "application-dev.yml",
            "application-dev.yaml",
            "application.yml",
            "application.yaml",
            "application-prd.properties",
            "application-prod.properties",
            "application-dev.properties",
            "application.properties"
        };
        
        boolean foundAny = false;
        for (String configFile : configFiles) {
            Path configPath = servicePath.resolve("src/main/resources/" + configFile);
            if (Files.exists(configPath)) {
                try {
                    if (configFile.endsWith(".yml") || configFile.endsWith(".yaml")) {
                        loadYamlProperties(configPath);
                    } else {
                        loadPropertiesFile(configPath);
                    }
                    logger.info("   ✓ Loaded properties from: {}", configFile);
                    logger.debug("   📋 Properties loaded: {}", serviceProperties.keySet());
                    foundAny = true;
                } catch (Exception e) {
                    logger.debug("   ⚠️  Error loading properties from {}: {}", configFile, e.getMessage());
                }
            }
        }
        
        if (!foundAny) {
            logger.warn("   ⚠️  No config files found in {}/src/main/resources/", serviceName);
        }
        
        // Also scan for any HTTP URLs directly in the config files (not as properties)
        extractHttpUrlsFromConfig(servicePath);
    }
    
    /**
     * Extract all HTTP/HTTPS URLs directly from config files
     * Handles cases where URLs are in any property, not just standard patterns
     */
    private void extractHttpUrlsFromConfig(Path servicePath) {
        String[] configFiles = {
            "application-prd.yml", "application-dev.yml", "application.yml",
            "application-prd.properties", "application-dev.properties", "application.properties"
        };
        
        for (String configFile : configFiles) {
            Path configPath = servicePath.resolve("src/main/resources/" + configFile);
            if (Files.exists(configPath)) {
                try {
                    String content = Files.readString(configPath);
                    // Extract all http/https URLs from the content
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                        "(https?://[a-zA-Z0-9.-]+(?::[0-9]+)?(?:/[^\\s\"']*)?)"
                    );
                    java.util.regex.Matcher matcher = pattern.matcher(content);
                    while (matcher.find()) {
                        String url = matcher.group(1);
                        // Store URL without property key (will be used for matching)
                        serviceProperties.put("_url_" + url, url);
                    }
                } catch (Exception e) {
                    logger.debug("Error extracting URLs from {}: {}", configFile, e.getMessage());
                }
            }
        }
    }
    
    /**
     * Load properties from YAML file (nested structure like feign.taskservice.name)
     */
    private void loadYamlProperties(Path yamlPath) throws Exception {
        Map<String, Object> yaml = yamlMapper.readValue(yamlPath.toFile(), Map.class);
        flattenYamlProperties("", yaml, serviceProperties);
    }
    
    /**
     * Flatten nested YAML structure to dot notation
     * Example: {feign: {taskservice: {name: "task-service"}}} -> feign.taskservice.name=task-service
     */
    private void flattenYamlProperties(String prefix, Map<String, Object> map, Map<String, String> result) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Map) {
                flattenYamlProperties(key, (Map<String, Object>) value, result);
            } else if (value != null) {
                result.put(key, value.toString());
            }
        }
    }
    
    /**
     * Load properties from .properties file
     */
    private void loadPropertiesFile(Path propertiesPath) throws Exception {
        Properties props = new Properties();
        try (FileReader reader = new FileReader(propertiesPath.toFile())) {
            props.load(reader);
            for (String key : props.stringPropertyNames()) {
                serviceProperties.put(key, props.getProperty(key));
            }
        }
    }
    
    /**
     * Resolve property placeholder like ${feign.taskservice.name}
     */
    private String resolveProperty(String propertyKey) {
        // Try exact match first
        String value = serviceProperties.get(propertyKey);
        if (value != null) {
            return value;
        }
        
        // Try without prefix (e.g., feign.consumer.name -> consumer.name)
        // Handles cases where YAML has "consumer.name" but Feign uses "${feign.consumer.name}"
        if (propertyKey.contains(".")) {
            String[] parts = propertyKey.split("\\.", 2);
            if (parts.length == 2) {
                String withoutPrefix = parts[1];
                value = serviceProperties.get(withoutPrefix);
                if (value != null) {
                    logger.debug("   💡 Found property without prefix: {} -> {}", withoutPrefix, value);
                    return value;
                }
            }
        }
        
        // Try adding common prefixes if not found
        // Handles cases where YAML has "feign.consumer.name" but placeholder uses "${consumer.name}"
        if (!propertyKey.startsWith("feign.")) {
            value = serviceProperties.get("feign." + propertyKey);
            if (value != null) {
                logger.debug("   💡 Found property with feign prefix: feign.{} -> {}", propertyKey, value);
                return value;
            }
        }
        
        return null;
    }
    
    private ServiceDependency extractRestTemplateDependency(MethodDeclaration method, Path javaFile, Path servicePath, List<ServiceInfo> allServices) {
        try {
            String methodBody = method.toString();
            
            // Look for URL patterns - can be hardcoded or property placeholders
            AtomicReference<String> foundUrl = new AtomicReference<>();
            AtomicReference<String> propertyPlaceholder = new AtomicReference<>();
            
            String[] lines = methodBody.split("\n");
            
            // Look for HTTP URLs (hardcoded) or property placeholders
            for (String line : lines) {
                String trimmed = line.trim();
                
                // Pattern 1: Hardcoded HTTP URLs
                if (trimmed.contains(AnalyzerConstants.HTTP_PREFIX) || trimmed.contains(AnalyzerConstants.HTTPS_PREFIX)) {
                    String url = extractUrlFromLine(trimmed);
                    if (url != null) {
                        foundUrl.set(url);
                        break;
                    }
                }
                
                // Pattern 2: Property placeholders like @Value("${task.service.url}")
                if (trimmed.contains("@Value") && trimmed.contains("${")) {
                    String placeholder = extractPropertyPlaceholder(trimmed);
                    if (placeholder != null) {
                        propertyPlaceholder.set(placeholder);
                    }
                }
                
                // Pattern 3: env.getProperty("task.service.url") or similar
                if (trimmed.contains("getProperty") && trimmed.contains("\"")) {
                    String propKey = extractQuotedString(trimmed);
                    if (propKey != null) {
                        String resolvedUrl = resolveProperty(propKey);
                        if (resolvedUrl != null && (resolvedUrl.startsWith("http://") || resolvedUrl.startsWith("https://"))) {
                            foundUrl.set(resolvedUrl);
                            break;
                        }
                    }
                }
            }
            
            // Resolve property placeholder if found
            if (propertyPlaceholder.get() != null && foundUrl.get() == null) {
                String resolved = resolveProperty(propertyPlaceholder.get());
                if (resolved != null) {
                    foundUrl.set(resolved);
                }
            }
            
            // Also check if any property in config contains a URL and is referenced in this method
            if (foundUrl.get() == null) {
                foundUrl.set(findUrlFromProperties(methodBody));
            }
            
            // Check if this method uses any HTTP client (RestTemplate, WebClient, HttpClient, Feign)
            if (foundUrl.get() != null) {
                boolean hasHttpClient = false;
                String clientType = "http-client";
                
                for (String line : lines) {
                    if (line.contains("restTemplate") || line.contains("RestTemplate")) {
                        hasHttpClient = true;
                        clientType = "rest-template";
                        break;
                    } else if (line.contains("webClient") || line.contains("WebClient")) {
                        hasHttpClient = true;
                        clientType = "web-client";
                        break;
                    } else if (line.contains("httpClient") || line.contains("HttpClient")) {
                        hasHttpClient = true;
                        clientType = "http-client";
                        break;
                    } else if (line.contains(".get(") || line.contains(".post(") || 
                               line.contains(".put(") || line.contains(".delete(")) {
                        hasHttpClient = true;
                        break;
                    }
                }
                
                if (hasHttpClient) {
                    String targetServiceName = extractServiceNameFromUrl(foundUrl.get(), null, allServices);
                    if (targetServiceName != null) {
                        String relativeFile = servicePath.relativize(javaFile).toString();
                        String sourceServiceName = servicePath.getFileName().toString();
                        
                        ServiceDependency dependency = new ServiceDependency(
                            sourceServiceName,
                            targetServiceName,
                            clientType
                        );
                        dependency.setDescription("HTTP call to " + targetServiceName);
                        dependency.setSourceFile(relativeFile);
                        dependency.setEndpoint(foundUrl.get());
                        return dependency;
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error extracting HTTP client dependency: {}", e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Extract property placeholder from @Value("${property.name}")
     */
    private String extractPropertyPlaceholder(String line) {
        int start = line.indexOf("${");
        int end = line.indexOf("}", start);
        if (start >= 0 && end > start) {
            return line.substring(start + 2, end);
        }
        return null;
    }
    
    /**
     * Extract quoted string from a line
     */
    private String extractQuotedString(String line) {
        int start = line.indexOf("\"");
        int end = line.indexOf("\"", start + 1);
        if (start >= 0 && end > start) {
            return line.substring(start + 1, end);
        }
        return null;
    }
    
    /**
     * Find URL from loaded properties that might be referenced in the method
     */
    private String findUrlFromProperties(String methodBody) {
        for (Map.Entry<String, String> entry : serviceProperties.entrySet()) {
            String value = entry.getValue();
            if ((value.startsWith("http://") || value.startsWith("https://")) && 
                methodBody.contains(entry.getKey())) {
                return value;
            }
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
    
    /**
     * Extract service name from URL using ENDPOINT-FIRST MATCHING (more accurate!)
     * 
     * Strategy:
     * 1. Extract endpoint path from URL
     * 2. Look up which service owns that endpoint (PRIMARY method)
     * 3. Fall back to service name/port matching if endpoint lookup fails
     * 
     * This is more reliable than fuzzy name matching because it validates against actual API endpoints.
     */
    private String extractServiceNameFromUrl(String url, String explicitEndpoint, List<ServiceInfo> allServices) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        // Use explicit endpoint if provided, otherwise extract from URL
        String endpointPath = explicitEndpoint != null ? explicitEndpoint : extractEndpointFromUrl(url);
        
        // STRATEGY 1: ENDPOINT-FIRST MATCHING (Most Accurate!)
        // Look up which service owns this endpoint
        if (endpointPath != null && !serviceEndpointsMap.isEmpty()) {
            String matchedService = findServiceByEndpoint(endpointPath);
            if (matchedService != null) {
                logger.debug("🎯 Matched URL {} to service {} via endpoint lookup: {}", url, matchedService, endpointPath);
                return matchedService;
            }
        }
        
        // Extract service name from URL hostname for fallback matching
        String urlServiceName = null;
        if (url.contains("://")) {
            String[] parts = url.split("://");
            if (parts.length > 1) {
                String hostPart = parts[1].split("/")[0];
                urlServiceName = hostPart.split(":")[0];
            }
        }
        
        // STRATEGY 2: Try name-based matching with endpoint validation
        for (ServiceInfo service : allServices) {
            String serviceName = service.getName();
            
            // Direct name match
            if (url.contains(serviceName)) {
                // Validate endpoint if we have endpoint map
                if (endpointPath != null && !serviceEndpointsMap.isEmpty()) {
                    if (serviceHasEndpoint(serviceName, endpointPath)) {
                        logger.debug("✓ Matched URL {} to service {} via name + endpoint validation", url, serviceName);
                        return serviceName;
                    } else {
                        logger.debug("⚠️  URL contains '{}' but endpoint {} not found in that service", serviceName, endpointPath);
                        continue; // Try other services
                    }
                } else {
                    return serviceName;
                }
            }
            
            // Port match
            if (service.getPort() != null && url.contains(":" + service.getPort())) {
                if (endpointPath == null || serviceHasEndpoint(serviceName, endpointPath)) {
                    logger.debug("✓ Matched URL {} to service {} via port: {}", url, serviceName, service.getPort());
                    return serviceName;
                }
            }
            
            // Fuzzy match with extracted service name from URL
            if (urlServiceName != null) {
                String matchedName = findMatchingServiceName(urlServiceName, allServices);
                if (matchedName != null && matchedName.equals(serviceName)) {
                    if (endpointPath == null || serviceHasEndpoint(serviceName, endpointPath)) {
                        logger.debug("✓ Matched URL {} to service {} via fuzzy name match", url, serviceName);
                        return serviceName;
                    }
                }
            }
        }
        
        // If we got here, it might be an external service (not in our service list)
        logger.debug("❌ URL {} does not match any internal service - likely external dependency", url);
        return null;
    }
    
    /**
     * Extract the path component from a URL.
     * Examples:
     * - "http://localhost:9095/ccgcore" -> "/ccgcore"
     * - "http://localhost:8080" -> ""
     * - "http://service:8080/api/v1" -> "/api/v1"
     */
    private String extractPathFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        
        try {
            // Remove protocol if present
            String path = url;
            if (path.contains("://")) {
                path = path.substring(path.indexOf("://") + 3);
            }
            
            // Remove host:port
            int slashIndex = path.indexOf('/');
            if (slashIndex >= 0) {
                path = path.substring(slashIndex);
            } else {
                return "";
            }
            
            return path;
        } catch (Exception e) {
            logger.warn("Failed to extract path from URL: {}", url, e);
            return "";
        }
    }
    
    /**
     * Find which service owns a specific endpoint (ENDPOINT-FIRST MATCHING)
     * This is the PRIMARY detection mechanism - more accurate than name matching!
     * 
     * @param endpointPath The endpoint path to lookup (e.g., "/api/excel/generate")
     * @return The service name that owns this endpoint, or null if not found
     */
    private String findServiceByEndpoint(String endpointPath) {
        if (endpointPath == null || endpointPath.isEmpty()) {
            return null;
        }
        
        // Direct lookup: exact match
        for (Map.Entry<String, List<String>> entry : serviceEndpointsMap.entrySet()) {
            String serviceName = entry.getKey();
            List<String> endpoints = entry.getValue();
            
            if (endpoints.contains(endpointPath)) {
                logger.debug("   → Endpoint {} found in service {} (exact match)", endpointPath, serviceName);
                return serviceName;
            }
        }
        
        // Partial match: endpoint starts with registered endpoint or vice versa
        // Handles cases like /api/users/123 matching /api/users
        for (Map.Entry<String, List<String>> entry : serviceEndpointsMap.entrySet()) {
            String serviceName = entry.getKey();
            List<String> endpoints = entry.getValue();
            
            for (String registeredEndpoint : endpoints) {
                // Check if the called endpoint starts with a registered endpoint
                // E.g., /api/users/123 matches /api/users
                if (endpointPath.startsWith(registeredEndpoint)) {
                    logger.debug("   → Endpoint {} matched to service {} (starts with {})", 
                        endpointPath, serviceName, registeredEndpoint);
                    return serviceName;
                }
                
                // Check if registered endpoint starts with called endpoint
                // E.g., /api matches /api/users
                if (registeredEndpoint.startsWith(endpointPath)) {
                    logger.debug("   → Endpoint {} matched to service {} (prefix of {})", 
                        endpointPath, serviceName, registeredEndpoint);
                    return serviceName;
                }
            }
        }
        
        logger.debug("   → Endpoint {} not found in any service's endpoint map", endpointPath);
        return null;
    }
    
    /**
     * Extract endpoint path from full URL
     * Example: http://excel-service:8080/api/excel/generate -> /api/excel/generate
     */
    private String extractEndpointFromUrl(String url) {
        if (!url.contains("://")) {
            return null;
        }
        
        String[] parts = url.split("://");
        if (parts.length < 2) {
            return null;
        }
        
        String afterProtocol = parts[1];
        int firstSlash = afterProtocol.indexOf('/');
        if (firstSlash < 0) {
            return null;
        }
        
        return afterProtocol.substring(firstSlash);
    }
    
    /**
     * Check if a service has a specific endpoint (with partial matching)
     * Handles base paths in Feign URLs like:
     *   URL: http://localhost:9095/ccgcore  + Endpoint: /v1/rawMessage
     *   Full path: /ccgcore/v1/rawMessage
     *   Controller only has: /v1/rawMessage
     */
    private boolean serviceHasEndpoint(String serviceName, String endpoint) {
        List<String> endpoints = serviceEndpointsMap.get(serviceName);
        if (endpoints == null || endpoints.isEmpty()) {
            return true; // If we don't have endpoint info, assume it's valid
        }
        
        // Exact match
        if (endpoints.contains(endpoint)) {
            logger.debug("   ✓ Exact endpoint match: {}", endpoint);
            return true;
        }
        
        // Partial match (endpoint starts with any registered endpoint)
        for (String registeredEndpoint : endpoints) {
            if (endpoint.startsWith(registeredEndpoint) || registeredEndpoint.startsWith(endpoint)) {
                logger.debug("   ✓ Partial endpoint match: {} ~ {}", endpoint, registeredEndpoint);
                return true;
            }
        }
        
        // Special case: endpoint might have a base path prefix that should be ignored
        // Example: /ccgcore/v1/rawMessage should match /v1/rawMessage
        for (String registeredEndpoint : endpoints) {
            if (endpoint.endsWith(registeredEndpoint)) {
                logger.debug("   ✓ Suffix endpoint match: {} ends with {}", endpoint, registeredEndpoint);
                return true;
            }
            // Or the registered endpoint might be part of the URL path
            if (endpoint.contains(registeredEndpoint)) {
                logger.debug("   ✓ Contains endpoint match: {} contains {}", endpoint, registeredEndpoint);
                return true;
            }
        }
        
        logger.debug("   ✗ Endpoint {} not found in service {} (has: {})", endpoint, serviceName, endpoints);
        return false;
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