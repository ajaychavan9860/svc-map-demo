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
                logger.error("Usage: java -jar generic-dependency-analyzer.jar <project-path> [config-file] [--include-all]");
                logger.error("       java -jar generic-dependency-analyzer.jar /path/to/microservices");
                logger.error("       java -jar generic-dependency-analyzer.jar /path/to/microservices /path/to/config.yml");
                logger.error("       java -jar generic-dependency-analyzer.jar /path/to/microservices . --include-all");
                logger.error("");
                logger.error("Options:");
                logger.error("  --include-all    Include gateway services and libraries in analysis (default: excluded)");
                System.exit(1);
            }

            // Parse arguments
            boolean includeAll = false;
            String projectPathArg = args[0];
            String configPathArg = null;
            
            for (int i = 1; i < args.length; i++) {
                if ("--include-all".equals(args[i])) {
                    includeAll = true;
                    logger.info("[CONFIG] --include-all flag detected: will include gateway services and libraries");
                } else if (configPathArg == null && !args[i].startsWith("--")) {
                    configPathArg = args[i];
                }
            }

            Path projectPath = Paths.get(projectPathArg);
            Path configPath = configPathArg != null ? Paths.get(configPathArg) : null;

            logger.info("[START] Starting Generic Microservices Dependency Analysis...");
            logger.info("Project Path: {}", projectPath.toAbsolutePath());

            if (configPath != null) {
                logger.info("[CONFIG] Config File: {}", configPath.toAbsolutePath());
            } else {
                logger.info("[CONFIG] Using default configuration");
            }

            try {
                analyzer.analyzeProject(projectPath, configPath, includeAll);
                logger.info("[OK] Analysis completed successfully!");
                logger.info("[STATS] Reports generated in: {}", projectPath.resolve("dependency-analysis"));
            } catch (Exception e) {
                logger.error("[FAIL] Analysis failed: {}", e.getMessage(), e);
                System.exit(1);
            }
        };
    }
}