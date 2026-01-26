package com.example.analyzer.scanner;

import com.example.analyzer.config.AnalyzerConfiguration;
import com.example.analyzer.model.ServiceInfo;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.FileReader;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class GenericServiceDiscovery {
    
    private final AnalyzerConfiguration config;
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    
    public GenericServiceDiscovery(AnalyzerConfiguration config) {
        this.config = config;
    }
    
    public List<ServiceInfo> discoverServices(Path projectPath) {
        List<ServiceInfo> services = new ArrayList<>();
        
        try {
            // Find all potential service directories
            List<Path> servicePaths = findServiceDirectories(projectPath);
            
            for (Path servicePath : servicePaths) {
                ServiceInfo service = analyzeServiceDirectory(servicePath, projectPath);
                if (service != null && !"parent-pom".equals(service.getType())) {
                    // Exclude parent/aggregator POMs from the service list
                    services.add(service);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error discovering services: " + e.getMessage());
        }
        
        return services;
    }
    
    private List<Path> findServiceDirectories(Path projectPath) throws Exception {
        List<Path> servicePaths = new ArrayList<>();
        
        // Look for Maven projects (pom.xml)
        PathMatcher pomMatcher = FileSystems.getDefault().getPathMatcher("glob:**/pom.xml");
        
        try (var stream = Files.walk(projectPath)) {
            List<Path> pomFiles = stream
                .filter(pomMatcher::matches)
                .filter(path -> isValidServiceDirectory(path.getParent()))
                .collect(Collectors.toList());
            
            for (Path pomFile : pomFiles) {
                servicePaths.add(pomFile.getParent());
            }
        }
        
        // Look for Gradle projects
        PathMatcher gradleMatcher = FileSystems.getDefault().getPathMatcher("glob:**/build.gradle*");
        
        try (var stream = Files.walk(projectPath)) {
            List<Path> gradleFiles = stream
                .filter(gradleMatcher::matches)
                .filter(path -> isValidServiceDirectory(path.getParent()))
                .collect(Collectors.toList());
            
            for (Path gradleFile : gradleFiles) {
                Path serviceDir = gradleFile.getParent();
                if (servicePaths.stream().noneMatch(existing -> existing.equals(serviceDir))) {
                    servicePaths.add(serviceDir);
                }
            }
        }
        
        // Look for Node.js projects
        PathMatcher packageMatcher = FileSystems.getDefault().getPathMatcher("glob:**/package.json");
        
        try (var stream = Files.walk(projectPath)) {
            List<Path> packageFiles = stream
                .filter(packageMatcher::matches)
                .filter(path -> isValidServiceDirectory(path.getParent()))
                .collect(Collectors.toList());
            
            for (Path packageFile : packageFiles) {
                Path serviceDir = packageFile.getParent();
                if (servicePaths.stream().noneMatch(existing -> existing.equals(serviceDir))) {
                    servicePaths.add(serviceDir);
                }
            }
        }
        
        return servicePaths;
    }
    
    private boolean isValidServiceDirectory(Path path) {
        String dirName = path.getFileName().toString();
        
        // Exclude common build/output directories
        for (String excludeDir : config.getServiceDetection().getExcludeDirectories()) {
            if (dirName.equals(excludeDir)) {
                return false;
            }
        }
        
        // Exclude parent project directories
        if (isParentProject(dirName)) {
            return false;
        }
        
        return true;
    }
    
    // Check if directory represents parent project
    private boolean isParentProject(String dirName) {
        return dirName != null && 
               (dirName.contains("demo") || 
                dirName.contains("workspace") ||
                dirName.contains("parent") ||
                dirName.equals(".") ||
                dirName.toLowerCase().endsWith("-demo"));
    }
    
    private ServiceInfo analyzeServiceDirectory(Path servicePath, Path projectRoot) {
        try {
            String serviceName = servicePath.getFileName().toString();
            String relativePath = projectRoot.relativize(servicePath).toString();
            
            ServiceInfo service = new ServiceInfo(serviceName, "unknown", relativePath);
            
            // Analyze Maven project
            Path pomFile = servicePath.resolve("pom.xml");
            if (Files.exists(pomFile)) {
                analyzeMavenProject(service, pomFile);
            }
            
            // Analyze Gradle project
            Path gradleFile = servicePath.resolve("build.gradle");
            if (!Files.exists(gradleFile)) {
                gradleFile = servicePath.resolve("build.gradle.kts");
            }
            if (Files.exists(gradleFile)) {
                analyzeGradleProject(service, gradleFile);
            }
            
            // Analyze Node.js project
            Path packageFile = servicePath.resolve("package.json");
            if (Files.exists(packageFile)) {
                analyzeNodeProject(service, packageFile);
            }
            
            // Analyze configuration files
            analyzeConfigurationFiles(service, servicePath);
            
            // Determine service type based on analysis
            determineServiceType(service);
            
            return service;
            
        } catch (Exception e) {
            System.err.println("Error analyzing service directory " + servicePath + ": " + e.getMessage());
            return null;
        }
    }
    
    private void analyzeMavenProject(ServiceInfo service, Path pomFile) {
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(pomFile.toFile()));
            
            // Skip parent/aggregator POMs (they are not actual services)
            // Parent POMs have <packaging>pom</packaging> and typically have <modules>
            if ("pom".equalsIgnoreCase(model.getPackaging())) {
                if (model.getModules() != null && !model.getModules().isEmpty()) {
                    // This is a parent/aggregator POM, mark it to be excluded
                    service.setType("parent-pom");
                    return;
                }
            }
            
            service.setBuildTool("maven");
            service.setLanguage("java");
            
            // Look for Spring Boot
            if (model.getDependencies().stream()
                .anyMatch(dep -> dep.getGroupId().contains("spring-boot"))) {
                service.setFramework("spring-boot");
            }
            
            // Look for Spring Cloud Gateway
            if (model.getDependencies().stream()
                .anyMatch(dep -> dep.getArtifactId().contains("spring-cloud-starter-gateway"))) {
                service.setType("gateway");
            }
            
            // Look for Eureka
            if (model.getDependencies().stream()
                .anyMatch(dep -> dep.getArtifactId().contains("eureka"))) {
                if (model.getDependencies().stream()
                    .anyMatch(dep -> dep.getArtifactId().contains("eureka-server"))) {
                    service.setType("discovery");
                } else {
                    service.setType("business");
                }
            }
            
            // Look for Config Server
            if (model.getDependencies().stream()
                .anyMatch(dep -> dep.getArtifactId().contains("spring-cloud-config-server"))) {
                service.setType("config");
            }
            
        } catch (Exception e) {
            System.err.println("Error reading POM file: " + e.getMessage());
        }
    }
    
    private void analyzeGradleProject(ServiceInfo service, Path gradleFile) {
        try {
            service.setBuildTool("gradle");
            service.setLanguage("java");
            
            String content = Files.readString(gradleFile);
            
            if (content.contains("spring-boot")) {
                service.setFramework("spring-boot");
            }
            
            if (content.contains("spring-cloud-starter-gateway")) {
                service.setType("gateway");
            } else if (content.contains("eureka-server")) {
                service.setType("discovery");
            } else if (content.contains("spring-cloud-config-server")) {
                service.setType("config");
            } else if (content.contains("spring-boot-starter-web")) {
                service.setType("business");
            }
            
        } catch (Exception e) {
            System.err.println("Error reading Gradle file: " + e.getMessage());
        }
    }
    
    private void analyzeNodeProject(ServiceInfo service, Path packageFile) {
        try {
            service.setBuildTool("npm");
            service.setLanguage("javascript");
            
            Map<String, Object> packageJson = yamlMapper.readValue(packageFile.toFile(), Map.class);
            
            Object dependencies = packageJson.get("dependencies");
            if (dependencies instanceof Map) {
                Map<String, Object> deps = (Map<String, Object>) dependencies;
                
                if (deps.containsKey("express")) {
                    service.setFramework("express");
                    service.setType("business");
                }
                
                if (deps.containsKey("next")) {
                    service.setFramework("next.js");
                    service.setType("frontend");
                }
                
                if (deps.containsKey("react")) {
                    service.setFramework("react");
                    service.setType("frontend");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error reading package.json: " + e.getMessage());
        }
    }
    
    private void analyzeConfigurationFiles(ServiceInfo service, Path servicePath) {
        List<String> configFiles = new ArrayList<>();
        
        // Look for application configuration files
        String[] configFileNames = {
            "application.yml", "application.yaml", "application.properties",
            "bootstrap.yml", "bootstrap.yaml", "bootstrap.properties"
        };
        
        for (String fileName : configFileNames) {
            Path configFile = servicePath.resolve("src/main/resources/" + fileName);
            if (Files.exists(configFile)) {
                configFiles.add(fileName);
                analyzeConfigFile(service, configFile);
            }
        }
        
        service.setConfigFiles(configFiles);
    }
    
    private void analyzeConfigFile(ServiceInfo service, Path configFile) {
        try {
            String content = Files.readString(configFile);
            
            // Extract port number
            if (configFile.getFileName().toString().endsWith(".properties")) {
                for (String line : content.split("\n")) {
                    if (line.startsWith("server.port")) {
                        String port = line.split("=")[1].trim();
                        service.setPort(Integer.parseInt(port));
                    }
                }
            } else {
                // YAML format
                try {
                    Map<String, Object> yamlContent = yamlMapper.readValue(configFile.toFile(), Map.class);
                    Object server = yamlContent.get("server");
                    if (server instanceof Map) {
                        Object port = ((Map<String, Object>) server).get("port");
                        if (port instanceof Integer) {
                            service.setPort((Integer) port);
                        }
                    }
                } catch (Exception e) {
                    // Ignore YAML parsing errors
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error reading config file: " + e.getMessage());
        }
    }
    
    private void determineServiceType(ServiceInfo service) {
        if ("unknown".equals(service.getType())) {
            String name = service.getName().toLowerCase();
            
            if (name.contains("gateway") || name.contains("proxy")) {
                service.setType("gateway");
            } else if (name.contains("config")) {
                service.setType("config");
            } else if (name.contains("discovery") || name.contains("eureka") || name.contains("registry")) {
                service.setType("discovery");
            } else if (name.contains("auth") || name.contains("security")) {
                service.setType("auth");
            } else if (service.getLanguage() != null) {
                service.setType("business");
            }
        }
    }
}