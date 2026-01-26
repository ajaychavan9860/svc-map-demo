package com.example.analyzer;

import com.example.analyzer.model.ServiceInfo;
import com.example.analyzer.model.ServiceDependency;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static guru.nidi.graphviz.model.Factory.*;

/**
 * GraphViz Java-based SVG generator - ORIGINAL GraphViz quality with pure Java!
 * Uses guru.nidi:graphviz-java with bundled GraphViz engines - NO system installation required!
 */
@Component
public class GraphVizJavaSvgGenerator {
    
    public void generateSvgDiagram(List<ServiceInfo> services, 
                                   List<ServiceDependency> dependencies, 
                                   Path outputPath) throws IOException {
        
        // Create the main graph with professional styling
        MutableGraph graph = mutGraph("microservices")
            .setDirected(true)
            .graphAttrs().add("rankdir", "TB")  // Top to Bottom layout
            .graphAttrs().add("bgcolor", "white")
            .graphAttrs().add("fontname", "Arial")
            .graphAttrs().add("fontsize", "12")
            .graphAttrs().add("labelloc", "t")
            .graphAttrs().add("label", "Microservices Dependency Graph\\n(Generated with GraphViz Java - Pure Maven)")
            .graphAttrs().add("compound", "true")  // Allow edges to/from clusters
            .nodeAttrs().add("shape", "box")
            .nodeAttrs().add("style", "rounded,filled")
            .nodeAttrs().add("fontname", "Arial")
            .nodeAttrs().add("fontsize", "10");
        
        // Create nodes for each service with proper colors and grouping
        Map<String, MutableNode> nodeMap = new HashMap<>();
        
        for (ServiceInfo service : services) {
            String nodeColor = getServiceColor(service.getName());
            String nodeLabel = String.format("%s\\nPort: %s", service.getName(), service.getPort());
            
            MutableNode node = mutNode(service.getName())
                .add("label", nodeLabel)
                .add("fillcolor", nodeColor)
                .add("color", "black");
                
            nodeMap.put(service.getName(), node);
            graph.add(node);
        }
        
        
        // Add compact legend at the top using simple text approach
        graph.add(mutNode("legend_info")
            .add("label", "Service Types: Gateway=Green | Config=Yellow | Business=Blue | Support=Cyan | Other=Gray\\nConnections:  -> Gateway(Green)  -> REST/Feign(Blue)  -> Messaging(Purple)")
            .add("shape", "plaintext")
            .add("fontsize", "10")
            .add("pos", "0,0!")  // Force position at top
            .add("fontname", "Arial"));
        
        // Create a rank constraint to put legend at top
        graph.graphAttrs().add("newrank", "true");
        graph.graphAttrs().add("ranksep", "1.0");
        
        // Add dependencies as edges
        for (ServiceDependency dependency : dependencies) {
            String fromService = dependency.getFromService();
            String toService = dependency.getToService();
            
            MutableNode fromNode = nodeMap.get(fromService);
            MutableNode toNode = nodeMap.get(toService);
            
            if (fromNode != null && toNode != null && !fromService.equals(toService)) {
                String edgeLabel = dependency.getType();
                String edgeColor = getEdgeColor(dependency.getType());
                
                fromNode.addLink(
                    to(toNode)
                        .with("label", edgeLabel)
                        .with("color", edgeColor)
                        .with("fontcolor", edgeColor)
                        .with("fontname", "Arial")
                        .with("fontsize", "9")
                        .with("arrowhead", "vee")
                );
            }
        }
        
        // Generate SVG using GraphViz Java with bundled engine
        try {
            Graphviz.fromGraph(graph)
                .width(1200)
                .height(800)
                .render(Format.SVG)
                .toFile(outputPath.toFile());
                
            System.out.println("[OK] GraphViz Java SVG diagram generated: " + outputPath);
            System.out.println("   🏆 Original GraphViz quality with ZERO system dependencies!");
            System.out.println("   📦 Pure Maven solution - " + services.size() + " services, " + dependencies.size() + " dependencies");
        } catch (Exception e) {
            System.err.println("[WARN]  GraphViz Java generation failed: " + e.getMessage());
            System.err.println("   💡 This is normal - some engines need additional setup");
            
            // For now, throw the exception - we'll use PlantUML as backup
            throw new IOException("GraphViz Java generation failed: " + e.getMessage(), e);
        }
    }
    
    private String getServiceColor(String serviceName) {
        String name = serviceName.toLowerCase();
        
        if (name.contains("gateway")) {
            return "lightgreen";  // Gateway services
        } else if (name.contains("config") || name.contains("discovery") || name.contains("eureka")) {
            return "lightyellow"; // Infrastructure services
        } else if (name.contains("user") || name.contains("product") || name.contains("order")) {
            return "lightblue";   // Core business services
        } else if (name.contains("payment") || name.contains("inventory") || name.contains("notification")) {
            return "lightcyan";   // Supporting services
        } else {
            return "lightgray";   // Other services
        }
    }
    
    private String getEdgeColor(String dependencyType) {
        String type = dependencyType.toLowerCase();
        
        if (type.contains("gateway") || type.contains("route")) {
            return "darkgreen";
        } else if (type.contains("feign") || type.contains("rest")) {
            return "blue";
        } else if (type.contains("async") || type.contains("message")) {
            return "purple"; 
        } else if (type.contains("database") || type.contains("data")) {
            return "orange";
        } else {
            return "black";
        }
    }
}