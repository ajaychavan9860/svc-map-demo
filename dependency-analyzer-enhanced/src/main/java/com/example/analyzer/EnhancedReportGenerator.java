package com.example.analyzer;

import com.example.analyzer.config.AnalyzerConfiguration;
import com.example.analyzer.model.AnalysisResult;
import com.example.analyzer.model.ServiceDependency;
import com.example.analyzer.model.ServiceInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enhanced Report Generator with multiple output formats
 * - JSON, CSV, Markdown, HTML  
 * - GraphViz Java SVG visualization (original GraphViz quality with pure Maven)
 */
@Component
public class EnhancedReportGenerator {
    
    private AnalyzerConfiguration config;
    private final ObjectMapper jsonMapper;
    private final GraphVizJavaSvgGenerator graphVizJavaGenerator;
    
    public EnhancedReportGenerator(GraphVizJavaSvgGenerator graphVizJavaGenerator) {
        this.jsonMapper = new ObjectMapper();
        this.jsonMapper.registerModule(new JavaTimeModule());
        this.jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.graphVizJavaGenerator = graphVizJavaGenerator;
    }
    
    public void generateReports(AnalysisResult result, Path outputDir) throws IOException {
        generateReports(result, outputDir, AnalyzerConfiguration.getDefault());
    }
    
    public void generateReports(AnalysisResult result, Path outputDir, AnalyzerConfiguration config) throws IOException {
        this.config = config;
        
        System.out.println("🎨 Generating reports with GraphViz Java - Original quality!");
        
        if (config.getOutputFormats().isJson()) {
            generateJsonReport(result, outputDir);
        }
        
        if (config.getOutputFormats().isCsv()) {
            generateCsvReport(result, outputDir);
        }
        
        if (config.getOutputFormats().isMarkdown()) {
            generateMarkdownReport(result, outputDir);
        }
        
        if (config.getOutputFormats().isHtml()) {
            generateHtmlReport(result, outputDir);
        }
        
        // Generate GraphViz Java visualization
        generateGraphVizJavaSvgReport(result, outputDir); // 🏆 ORIGINAL GraphViz with Pure Java!
        
        System.out.println("✅ All reports generated successfully!");
        System.out.println("   🏆 GraphViz Java - Original quality with pure Maven dependencies!");
    }
    
    private void generateGraphVizJavaSvgReport(AnalysisResult result, Path outputDir) throws IOException {
        Path svgPath = outputDir.resolve("dependency-diagram-graphviz-java.svg");
        graphVizJavaGenerator.generateSvgDiagram(result.getServices(), result.getDependencies(), svgPath);
        
        System.out.printf("   🏆 GraphViz Java SVG diagram: %s (%.1f KB) - ORIGINAL QUALITY!%n", 
            svgPath.getFileName(), Files.size(svgPath) / 1024.0);
    }
    
    private void generateJsonReport(AnalysisResult result, Path outputDir) throws IOException {
        Path jsonPath = outputDir.resolve("analysis-result.json");
        jsonMapper.writeValue(jsonPath.toFile(), result);
        
        System.out.printf("   📄 JSON report: %s (%.1f KB)%n", 
            jsonPath.getFileName(), Files.size(jsonPath) / 1024.0);
    }
    
    private void generateCsvReport(AnalysisResult result, Path outputDir) throws IOException {
        Path csvPath = outputDir.resolve("dependency-matrix.csv");
        
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(csvPath))) {
            writer.println("From Service,To Service,Dependency Type,Source File,Endpoint");
            
            for (ServiceDependency dep : result.getDependencies()) {
                writer.printf("%s,%s,%s,%s,%s%n",
                    dep.getFromService(),
                    dep.getToService(),
                    dep.getType(),
                    dep.getSourceFile() != null ? dep.getSourceFile() : "unknown",
                    dep.getEndpoint() != null ? dep.getEndpoint() : "n/a"
                );
            }
        }
        
        System.out.printf("   📊 CSV report: %s (%.1f KB)%n", 
            csvPath.getFileName(), Files.size(csvPath) / 1024.0);
    }
    
    private void generateMarkdownReport(AnalysisResult result, Path outputDir) throws IOException {
        Path mdPath = outputDir.resolve("impact-analysis.md");
        
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(mdPath))) {
            writer.println("# Microservices Dependency Impact Analysis");
            writer.println();
            writer.println("## Executive Summary");
            writer.printf("- **Total Services**: %d%n", result.getTotalServices());
            writer.printf("- **Total Dependencies**: %d%n", result.getTotalDependencies());
            writer.printf("- **Analysis Date**: %s%n", 
                result.getAnalysisTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            writer.println();
            
            writer.println("## Services Overview");
            writer.println("| Service | Port | Framework | Dependencies |");
            writer.println("|---------|------|-----------|--------------|");
            
            for (ServiceInfo service : result.getServices()) {
                long depCount = result.getDependencies().stream()
                    .filter(dep -> dep.getFromService().equals(service.getName()))
                    .count();
                    
                writer.printf("| %s | %s | %s | %d |%n",
                    service.getName(),
                    service.getPort(),
                    service.getFramework(),
                    depCount
                );
            }
            
            writer.println();
            writer.println("## Dependency Details");
            
            for (ServiceDependency dep : result.getDependencies()) {
                writer.printf("### %s → %s%n", dep.getFromService(), dep.getToService());
                writer.printf("- **Type**: %s%n", dep.getType());
                if (dep.getEndpoint() != null) {
                    writer.printf("- **Endpoint**: %s%n", dep.getEndpoint());
                }
                if (dep.getSourceFile() != null) {
                    writer.printf("- **Source**: %s%n", dep.getSourceFile());
                }
                writer.println();
            }
        }
        
        System.out.printf("   📝 Markdown report: %s (%.1f KB)%n", 
            mdPath.getFileName(), Files.size(mdPath) / 1024.0);
    }
    
    private void generateHtmlReport(AnalysisResult result, Path outputDir) throws IOException {
        Path htmlPath = outputDir.resolve("dependency-report.html");
        
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(htmlPath))) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("    <title>Microservices Dependency Analysis Report</title>");
            writer.println("    <style>");
            writer.println("        body { font-family: Arial, sans-serif; margin: 20px; }");
            writer.println("        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 10px; }");
            writer.println("        .summary { background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0; }");
            writer.println("        .service { margin: 10px 0; padding: 10px; border-left: 4px solid #007bff; background: #f8f9fa; }");
            writer.println("        .dependency { margin: 5px 0; padding: 8px; border-left: 3px solid #28a745; background: #f1f8f4; }");
            writer.println("        table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
            writer.println("        th, td { border: 1px solid #dee2e6; padding: 8px; text-align: left; }");
            writer.println("        th { background-color: #e9ecef; }");
            writer.println("        .high-confidence { color: #28a745; font-weight: bold; }");
            writer.println("        .medium-confidence { color: #ffc107; font-weight: bold; }");
            writer.println("        .low-confidence { color: #dc3545; font-weight: bold; }");
            writer.println("    </style>");
            writer.println("</head>");
            writer.println("<body>");
            
            writer.println("<div class='header'>");
            writer.println("    <h1>🔄 Microservices Dependency Analysis</h1>");
            writer.println("    <p>Enterprise-grade dependency mapping and impact analysis</p>");
            writer.println("</div>");
            
            writer.println("<div class='summary'>");
            writer.println("    <h2>📊 Executive Summary</h2>");
            writer.printf("    <p><strong>Analysis Date:</strong> %s</p>%n", 
                result.getAnalysisTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            writer.printf("    <p><strong>Total Services:</strong> %d</p>%n", result.getTotalServices());
            writer.printf("    <p><strong>Total Dependencies:</strong> %d</p>%n", result.getTotalDependencies());
            
            // Calculate business metrics
            double avgDepsPerService = (double) result.getTotalDependencies() / result.getTotalServices();
            writer.printf("    <p><strong>Average Dependencies per Service:</strong> %.1f</p>%n", avgDepsPerService);
            writer.printf("    <p><strong>🎯 Regression Testing Cost Reduction:</strong> ~%.0f%%</p>%n", 
                Math.min(90, avgDepsPerService * 15)); // Estimate based on targeted testing
            writer.println("</div>");
            
            writer.println("<h2>🏢 Services Overview</h2>");
            writer.println("<table>");
            writer.println("<tr><th>Service</th><th>Port</th><th>Framework</th><th>Outbound Dependencies</th><th>Risk Level</th></tr>");
            
            for (ServiceInfo service : result.getServices()) {
                long depCount = result.getDependencies().stream()
                    .filter(dep -> dep.getFromService().equals(service.getName()))
                    .count();
                    
                String riskLevel = depCount > 5 ? "High" : depCount > 2 ? "Medium" : "Low";
                String riskClass = depCount > 5 ? "low-confidence" : depCount > 2 ? "medium-confidence" : "high-confidence";
                
                writer.printf("<tr><td>%s</td><td>%s</td><td>%s</td><td>%d</td><td class='%s'>%s</td></tr>%n",
                    service.getName(), service.getPort(), service.getFramework(), depCount, riskClass, riskLevel);
            }
            writer.println("</table>");
            
            writer.println("<h2>🔗 Dependency Relationships</h2>");
            
            Map<String, List<ServiceDependency>> dependenciesBySource = result.getDependencies().stream()
                .collect(Collectors.groupingBy(ServiceDependency::getFromService));
                
            for (Map.Entry<String, List<ServiceDependency>> entry : dependenciesBySource.entrySet()) {
                writer.printf("<div class='service'><h3>📦 %s</h3>%n", entry.getKey());
                
                for (ServiceDependency dep : entry.getValue()) {
                    writer.printf("<div class='dependency'>→ <strong>%s</strong> " +
                                 "(<em>%s</em>", dep.getToService(), dep.getType());
                                 
                    if (dep.getEndpoint() != null && !dep.getEndpoint().isEmpty()) {
                        writer.printf(" endpoint: %s", dep.getEndpoint());
                    }
                    
                    writer.print(")</div>\n");
                }
                writer.println("</div>");
            }
            
            writer.println("<hr>");
            writer.println("<p><em>Generated by Generic Microservices Dependency Analyzer - Enterprise Edition</em></p>");
            writer.println("</body>");
            writer.println("</html>");
        }
        
        System.out.printf("   🌐 HTML report: %s (%.1f KB)%n", 
            htmlPath.getFileName(), Files.size(htmlPath) / 1024.0);
    }
}