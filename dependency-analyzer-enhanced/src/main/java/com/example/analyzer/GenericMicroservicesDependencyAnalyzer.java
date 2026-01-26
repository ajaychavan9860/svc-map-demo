package com.example.analyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class GenericMicroservicesDependencyAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(GenericMicroservicesDependencyAnalyzer.class);

    public static void main(String[] args) {
        SpringApplication.run(GenericMicroservicesDependencyAnalyzer.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(MicroserviceAnalyzer analyzer) {
        return args -> {
            if (args.length < 1) {
                logger.error("Usage: java -jar generic-dependency-analyzer.jar <project-path> [config-file]");
                logger.error("       java -jar generic-dependency-analyzer.jar /path/to/microservices");
                logger.error("       java -jar generic-dependency-analyzer.jar /path/to/microservices /path/to/config.yml");
                System.exit(1);
            }

            Path projectPath = Paths.get(args[0]);
            Path configPath = args.length > 1 ? Paths.get(args[1]) : null;

            logger.info("🚀 Starting Generic Microservices Dependency Analysis...");
            logger.info("📂 Project Path: {}", projectPath.toAbsolutePath());

            if (configPath != null) {
                logger.info("⚙️ Config File: {}", configPath.toAbsolutePath());
            } else {
                logger.info("⚙️ Using default configuration");
            }

            try {
                analyzer.analyzeProject(projectPath, configPath);
                logger.info("[OK] Analysis completed successfully!");
                logger.info("[STATS] Reports generated in: {}", projectPath.resolve("dependency-analysis"));
            } catch (Exception e) {
                logger.error("[FAIL] Analysis failed: {}", e.getMessage(), e);
                System.exit(1);
            }
        };
    }
}