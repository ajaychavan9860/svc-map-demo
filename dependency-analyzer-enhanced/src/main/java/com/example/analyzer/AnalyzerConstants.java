package com.example.analyzer;

/**
 * Constants used throughout the microservices dependency analyzer.
 */
public final class AnalyzerConstants {

    // Service Types
    public static final String GATEWAY_TYPE = "gateway";
    public static final String BUSINESS_TYPE = "business";
    public static final String CONFIG_TYPE = "config";

    // Dependency Types
    public static final String REST_TEMPLATE_TYPE = "rest-template";
    public static final String WEBCLIENT_TYPE = "webclient";
    public static final String ASYNC_TYPE = "async";
    public static final String MESSAGING_TYPE = "messaging";
    public static final String GATEWAY_DEPENDENCY_TYPE = "gateway";

    // File Extensions and Patterns
    public static final String JAVA_EXTENSION = ".java";
    public static final String POM_FILE = "pom.xml";
    public static final String BUILD_GRADLE = "build.gradle";
    public static final String PACKAGE_JSON = "package.json";

    // Directory Names
    public static final String SRC_MAIN_JAVA = "src/main/java";
    public static final String TARGET_DIR = "target";
    public static final String BUILD_DIR = "build";
    public static final String NODE_MODULES = "node_modules";
    public static final String GIT_DIR = ".git";
    public static final String IDEA_DIR = ".idea";

    // HTTP Protocols
    public static final String HTTP_PREFIX = "http://";
    public static final String HTTPS_PREFIX = "https://";

    // Common Patterns
    public static final String REST_CONTROLLER = "RestController";
    public static final String FEIGN_CLIENT = "FeignClient";

    // Output Directory
    public static final String DEPENDENCY_ANALYSIS_DIR = "dependency-analysis";

    // Output Files
    public static final String SVG_DIAGRAM_FILE = "dependency-diagram-graphviz-java.svg";
    public static final String HTML_REPORT_FILE = "dependency-report.html";
    public static final String JSON_REPORT_FILE = "analysis-result.json";
    public static final String CSV_MATRIX_FILE = "dependency-matrix.csv";
    public static final String IMPACT_ANALYSIS_FILE = "impact-analysis.md";

    // Default Ports (for reference)
    public static final int DEFAULT_GATEWAY_PORT = 8080;
    public static final int DEFAULT_USER_SERVICE_PORT = 8081;
    public static final int DEFAULT_PRODUCT_SERVICE_PORT = 8082;
    public static final int DEFAULT_ORDER_SERVICE_PORT = 8083;
    public static final int DEFAULT_PAYMENT_SERVICE_PORT = 8084;
    public static final int DEFAULT_INVENTORY_SERVICE_PORT = 8085;
    public static final int DEFAULT_NOTIFICATION_SERVICE_PORT = 8086;
    public static final int DEFAULT_EMAIL_SERVICE_PORT = 8087;
    public static final int DEFAULT_LOGGING_SERVICE_PORT = 8088;
    public static final int DEFAULT_REPORTING_SERVICE_PORT = 8089;
    public static final int DEFAULT_ANALYTICS_SERVICE_PORT = 8090;
    public static final int DEFAULT_CONFIG_SERVICE_PORT = 8761;

    // Private constructor to prevent instantiation
    private AnalyzerConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}