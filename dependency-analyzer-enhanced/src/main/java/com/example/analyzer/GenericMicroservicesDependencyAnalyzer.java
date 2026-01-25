package com.example.analyzer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class GenericMicroservicesDependencyAnalyzer {

    public static void main(String[] args) {
        SpringApplication.run(GenericMicroservicesDependencyAnalyzer.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(MicroserviceAnalyzer analyzer) {
        return args -> {
            if (args.length < 1) {
                System.err.println("Usage: java -jar generic-dependency-analyzer.jar <project-path> [config-file]");
                System.err.println("       java -jar generic-dependency-analyzer.jar /path/to/microservices");
                System.err.println("       java -jar generic-dependency-analyzer.jar /path/to/microservices /path/to/config.yml");
                System.exit(1);
            }

            Path projectPath = Paths.get(args[0]);
            Path configPath = args.length > 1 ? Paths.get(args[1]) : null;

            System.out.println("🚀 Starting Generic Microservices Dependency Analysis...");
            System.out.println("📂 Project Path: " + projectPath.toAbsolutePath());
            
            if (configPath != null) {
                System.out.println("⚙️ Config File: " + configPath.toAbsolutePath());
            } else {
                System.out.println("⚙️ Using default configuration");
            }

            try {
                analyzer.analyzeProject(projectPath, configPath);
                System.out.println("✅ Analysis completed successfully!");
                System.out.println("📊 Reports generated in: " + projectPath.resolve("dependency-analysis"));
            } catch (Exception e) {
                System.err.println("❌ Analysis failed: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        };
    }
}