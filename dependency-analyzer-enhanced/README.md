# 🔍 Generic Microservices Dependency Analyzer

**Universal tool to analyze microservices dependencies with SVG visualization - Solving the regression testing problem with data-driven insights.**

## 🎯 What Problem Does This Solve?

### ❌ **BEFORE: The Regression Testing Problem**

- **Change ANY service** → **Test ALL services**
- **Complete Regression Required** every deployment
- **High Cost** and **Long Test Cycles**
- **No Evidence** to justify selective testing

### ✅ **AFTER: Data-Driven Testing Strategy**

- **Precise Testing Scope** based on actual dependencies
- **60-80% Reduction** in regression testing
- **Visual Evidence** for business stakeholders
- **Clear Risk Assessment** for each change

---

## 🚀 Features

### 🔍 **Universal Service Discovery**

- **Java**: Maven (pom.xml), Gradle (build.gradle)
- **JavaScript/Node.js**: NPM (package.json)
- **Python**: Requirements.txt, setup.py
- **Go**: go.mod
- **Rust**: Cargo.toml
- **Docker**: Dockerfile, docker-compose.yml

### 🔗 **Dependency Detection**

- **Feign Clients**: `@FeignClient` annotations
- **REST Calls**: RestTemplate, WebClient, HttpClient, axios, requests
- **Gateway Routes**: Spring Cloud Gateway, Zuul, NGINX, Traefik
- **Messaging**: Kafka, RabbitMQ, SQS, SNS, EventBridge
- **Databases**: JPA, MongoDB, Redis connections
- **Configuration**: Property files, environment variables

### 📊 **Rich Visualizations**

- **Interactive SVG**: Vector graphics with zoom/pan
- **HTML Reports**: Modern, responsive interface
- **PNG/DOT**: Traditional graph formats
- **CSV Export**: Spreadsheet analysis for management
- **JSON API**: Machine-readable for CI/CD integration

### ⚙️ **Highly Configurable**

- **Custom Patterns**: Define your own dependency detection rules
- **Multiple Architectures**: Spring Boot, Express.js, Flask, FastAPI
- **Framework Support**: Kubernetes, Docker, Service Mesh
- **Output Control**: Choose which formats to generate

---

## ✨ Recent Improvements & Bug Fixes

### 🔧 **v2.0.0 - Gateway Routes & Library Dependencies (Latest)**

**Major Fixes:**

- ✅ **Gateway Route Extraction** - Fixed URI parsing for Spring Cloud Gateway routes (lb://service-name format)
- ✅ **Gateway Dependencies** - Now correctly creates dependencies for API Gateway routes
- ✅ **Maven Library Scanning** - Enabled scanning of shared library dependencies when using --include-all
- ✅ **ServiceDependency Constructor** - Fixed parameter order to (fromService, toService, dependencyType)

**What This Means:**

- Gateway routes now appear in dependency diagrams (with --include-all)
- Shared library usage is now discoverable (e.g., common-lib dependencies)
- Complete picture of both service-to-service AND gateway routing

**Testing Impact:**

- Default mode: Focuses on direct service communication (10 dependencies in typical setup)
- --include-all mode: Includes gateway routes and library dependencies (18 dependencies in typical setup)
- Enables gateway coverage analysis (can now see which services are NOT routable)

**Technical Details:**

- Fixed `extractUri()` method to use `indexOf()` instead of `split(":")`
- Changed dependency type from "gateway-route" to "gateway" for consistency
- Re-enabled Maven scanning with conditional flag checking

---

### ⚙️ **Highly Configurable**

- **Custom Patterns**: Define your own dependency detection rules
- **Multiple Architectures**: Spring Boot, Express.js, Flask, FastAPI
- **Framework Support**: Kubernetes, Docker, Service Mesh
- **Output Control**: Choose which formats to generate
- **Analysis Modes**: Default (focused) or --include-all (comprehensive)

---

## 📦 Installation & Usage

### 📋 **Prerequisites**

#### **Java 17+ (Required)**

- **macOS**: `brew install openjdk@17`
- **Ubuntu/Debian**: `sudo apt install openjdk-17-jdk`
- **Windows**:
  - Download from [Adoptium](https://adoptium.net/) or [Oracle JDK](https://www.oracle.com/java/)
  - Or use Chocolatey: `choco install openjdk17`
  - Verify: `java -version`

#### **Maven 3.6+ (Required for building)**

- **macOS**: `brew install maven`
- **Ubuntu/Debian**: `sudo apt install maven`
- **Windows**:
  - Download from [Apache Maven](https://maven.apache.org/download.cgi)
  - Or use Chocolatey: `choco install maven`
  - Add to PATH: `C:\ProgramData\chocolatey\lib\maven\apache-maven-3.x.x\bin`
  - Verify: `mvn -version`

#### **Graphviz (Optional, for enhanced diagrams)**

- **macOS**: `brew install graphviz`
- **Ubuntu/Debian**: `sudo apt-get install graphviz`
- **Windows**:
  - Use Chocolatey: `choco install graphviz`
  - Or download from [Graphviz.org](https://graphviz.org/download/)
  - Verify: `dot -V`

### 🚀 **Windows Quick Start**

If you're on Windows, here's the fastest way to get started:

```batch
# 1. Install prerequisites (run in PowerShell as Administrator)
choco install openjdk17 maven graphviz

# 2. Clone and build
git clone <your-repo>
cd dependency-analyzer-enhanced
mvn clean package -DskipTests

# 3. Run analysis
java -jar target\generic-microservices-dependency-analyzer-2.0.0.jar C:\path\to\your\microservices

# 4. View results in dependency-analysis\ folder
```

**Expected Output:**

```
🚀 Starting Generic Microservices Dependency Analysis...
📂 Project Path: C:\path\to\your\microservices
📋 Found X services...
📊 Found X dependency relationships
✅ Analysis completed successfully!
```

### 🏗️ **Build the Tool**

#### **Clone and Build**

```bash
# Clone the repository
git clone <your-repo>
cd dependency-analyzer-enhanced

# Build the analyzer
mvn clean package -DskipTests

# Verify build success
ls -la target/generic-microservices-dependency-analyzer-*.jar
```

#### **Windows Build Script**

```batch
# Use the provided Windows batch file
demo-enhanced-analyzer.bat
```

### 🔧 **Basic Usage**

#### **Command Line**

```bash
# Analyze with default settings (excludes gateway routes and maven dependencies)
java -jar target/generic-microservices-dependency-analyzer-2.0.0.jar /path/to/your/microservices

# Use custom configuration
java -jar target/generic-microservices-dependency-analyzer-2.0.0.jar /path/to/your/microservices /path/to/analyzer-config.yml

# Include all dependency types (gateway routes, maven dependencies, etc.)
java -jar target/generic-microservices-dependency-analyzer-2.0.0.jar /path/to/your/microservices --include-all
```

#### **Windows Command Prompt**

```batch
# Basic analysis
java -jar target\generic-microservices-dependency-analyzer-2.0.0.jar C:\path\to\your\microservices

# With custom config
java -jar target\generic-microservices-dependency-analyzer-2.0.0.jar C:\path\to\your\microservices C:\path\to\analyzer-config.yml

# Include all dependency types
java -jar target\generic-microservices-dependency-analyzer-2.0.0.jar C:\path\to\your\microservices --include-all
```

#### **Windows PowerShell**

```powershell
# Basic analysis
java -jar "target\generic-microservices-dependency-analyzer-2.0.0.jar" "C:\path\to\your\microservices"

# With custom config
java -jar "target\generic-microservices-dependency-analyzer-2.0.0.jar" "C:\path\to\your\microservices" "C:\path\to\analyzer-config.yml"

# Include all dependency types
java -jar "target\generic-microservices-dependency-analyzer-2.0.0.jar" "C:\path\to\your\microservices" "--include-all"
```

#### **Quick Reference: Command Options**

| Flag                     | Purpose                                     | Usage                                                      |
| ------------------------ | ------------------------------------------- | ---------------------------------------------------------- |
| _(none)_                 | Default analysis mode                       | `java -jar analyzer.jar /project`                          |
| `--include-all`          | Include gateway routes & maven dependencies | `java -jar analyzer.jar /project --include-all`            |
| config.yml               | Custom configuration file                   | `java -jar analyzer.jar /project config.yml`               |
| `--include-all` + config | Both custom config and all dependencies     | `java -jar analyzer.jar /project config.yml --include-all` |

---

### 📊 **Analysis Modes: Default vs --include-all**

The analyzer supports two distinct analysis modes that provide different levels of detail:

#### **🔵 Default Mode (Recommended for Most Use Cases)**

When run without the `--include-all` flag, the analyzer focuses on direct service-to-service communication:

```bash
# Default mode - excludes gateway routes and maven library dependencies
java -jar target/generic-microservices-dependency-analyzer-2.0.0.jar /path/to/your/microservices
```

**What's Included:**

- ✅ Feign client calls (@FeignClient)
- ✅ REST template calls (RestTemplate, WebClient, axios, requests)
- ✅ Message queue connections (Kafka, RabbitMQ, etc.)
- ✅ Database connections (JPA, MongoDB, Redis)
- ❌ Gateway routes (excluded)
- ❌ Maven library dependencies (excluded)

**Best For:**

- Standard microservices regression testing
- Understanding direct service-to-service dependencies
- Day-to-day testing decisions
- Core business flow analysis

**Example Output:**

```
Total Services: 14
Total Dependencies: 10
```

---

#### **🟢 Comprehensive Mode (--include-all Flag)**

When run with the `--include-all` flag, the analyzer discovers the complete dependency graph:

```bash
# Comprehensive mode - includes gateway routes and all dependencies
java -jar target/generic-microservices-dependency-analyzer-2.0.0.jar /path/to/your/microservices --include-all
```

**What's Included (Default Mode + All of):**

- ✅ Gateway routes from Spring Cloud Gateway (application.yml, api.yml)
- ✅ Maven library dependencies (pom.xml, build.gradle)
- ✅ Shared library usage patterns
- ✅ All framework-level dependencies

**Best For:**

- Comprehensive architecture visualization
- Library adoption analysis (which services use common-lib?)
- Complete architecture documentation
- Gateway routing coverage assessment
- Code reuse metrics
- Architecture reviews and planning

**Example Output:**

```
Total Services: 14
Total Dependencies: 18 (vs 10 in default mode)

Additional Dependencies Found:
  - 6 Gateway routes
  - 2 Maven library dependencies
```

---

#### **Detailed Comparison: Default vs --include-all**

| Feature                          | Default Mode | --include-all |
| -------------------------------- | ------------ | ------------- |
| **Feign Clients**                | ✅ Yes       | ✅ Yes        |
| **REST Calls**                   | ✅ Yes       | ✅ Yes        |
| **Database Connections**         | ✅ Yes       | ✅ Yes        |
| **Messaging Queues**             | ✅ Yes       | ✅ Yes        |
| **Gateway Routes**               | ❌ No        | ✅ Yes        |
| **Maven Dependencies**           | ❌ No        | ✅ Yes        |
| **Shared Libraries**             | ❌ No        | ✅ Yes        |
| **Total Dependencies (Typical)** | 10           | 18            |
| **SVG File Size**                | ~17 KB       | ~21 KB        |
| **HTML Report**                  | ✅ Yes       | ✅ Yes        |
| **CSV Matrix**                   | ✅ Yes       | ✅ Yes        |

---

#### **What Does --include-all Actually Discover?**

**Gateway Routes (Spring Cloud Gateway)**

These are extracted from `application.yml`:

```yaml
spring.cloud.gateway.routes:
  - id: order-service
    uri: lb://order-service
    predicates:
      - Path=/api/orders/**
```

Shows up in analysis as:

- `gateway-service → order-service` (type: gateway)

**Maven Library Dependencies**

These are extracted from `pom.xml`:

```xml
<dependency>
  <groupId>com.example</groupId>
  <artifactId>common-lib</artifactId>
  <version>1.0.0</version>
</dependency>
```

Shows up in analysis as:

- `order-service → common-lib` (type: maven-dependency)

---

#### **Decision Guide: Which Mode to Use?**

```
Question: What are you trying to understand?

├─ "What tests do I need to run?" → Use DEFAULT MODE
│  └─ Directly answers: "If service X changes, test services Y, Z"
│
├─ "Is every service routable through the gateway?" → Use --include-all
│  └─ Shows gateway coverage and routing configuration
│
├─ "How much code reuse do we have?" → Use --include-all
│  └─ Reveals library adoption patterns (common-lib usage)
│
├─ "What's the complete system architecture?" → Use --include-all
│  └─ Full dependency graph for documentation and planning
│
└─ "Daily development and testing?" → Use DEFAULT MODE
   └─ Focused on what actually impacts your service
```

---

#### **Real-World Example: svc-map-demo Project**

**Default Mode Output (Current Setup):**

- Services: 14
- Dependencies: 10
- Shows: Feign calls, REST templates, database connections
- Hidden: Gateway routing, library usage

**--include-all Mode Output:**

- Services: 14
- Dependencies: 18 (+8 additional)
- Added:
  - 6 Gateway routes (gateway-service → 6 services)
  - 2 Library dependencies (order-service, user-service → common-lib)
- Benefits:
  - Reveals that 4 services are not routable via gateway (email, logging, reporting, analytics)
  - Shows that only 2/10 services use common-lib (20% adoption, opportunity for improvement)
  - Provides complete architectural picture

### 🪟 **Windows-Specific Setup**

#### **1. Environment Variables**

Ensure Java and Maven are in your PATH:

```batch
# Check current PATH
echo %PATH%

# Add Java to PATH (adjust path as needed)
setx PATH "%PATH%;C:\Program Files\Java\jdk-17\bin"

# Add Maven to PATH
setx PATH "%PATH%;C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.5\bin"

# Restart command prompt or run: refreshenv
```

#### **2. Common Windows Issues**

**Java Not Found:**

```batch
# Install Java via Chocolatey
choco install openjdk17

# Or download manually and set JAVA_HOME
setx JAVA_HOME "C:\Program Files\Java\jdk-17"
```

**Maven Not Found:**

```batch
# Install Maven via Chocolatey
choco install maven

# Or download and extract to C:\maven, then add to PATH
```

**Permission Issues:**

```batch
# Run as Administrator or check folder permissions
# Ensure write access to output directory
```

**Long Path Names:**

```batch
# Use short paths or enable long paths in Windows
# Windows 10+: reg add HKLM\SYSTEM\CurrentControlSet\Control\FileSystem /v LongPathsEnabled /t REG_DWORD /d 1 /f
```

### 🔍 **Troubleshooting Gateway Routes & Dependencies**

#### **Gateway Routes Not Appearing**

**Problem:** When using `--include-all`, gateway routes are not showing in the diagram.

**Solutions:**

1. **Verify Spring Cloud Gateway Configuration:**

   ```bash
   # Check if application.yml or api.yml contains routes
   grep -r "spring.cloud.gateway.routes" application.yml
   grep -r "uri: lb://" application.yml
   ```

2. **Ensure Routes Use lb:// Format:**

   ```yaml
   # ✅ CORRECT - This will be detected
   spring.cloud.gateway.routes:
     - id: order-service
       uri: lb://order-service
       predicates:
         - Path=/api/orders/**

   # ❌ WRONG - This won't be detected
   spring.cloud.gateway.routes:
     - id: order-service
       uri: http://localhost:8083
       predicates:
         - Path=/api/orders/**
   ```

3. **Check Analyzer Output:**

   ```bash
   # Run with --include-all and check for gateway extraction logs
   java -jar analyzer.jar /project --include-all 2>&1 | grep -i gateway
   ```

4. **Verify Configuration File Location:**
   - Routes must be in: `src/main/resources/application.yml`
   - Or in: `src/main/resources/application-{env}.yml`
   - Not in Java code (only static YAML files are scanned)

---

#### **Maven Dependencies Not Appearing**

**Problem:** Library dependencies (like common-lib) not showing when using `--include-all`.

**Solutions:**

1. **Verify pom.xml Contains Dependencies:**

   ```bash
   # Check if your services have pom.xml with dependencies
   grep -r "<dependency>" */pom.xml | grep common-lib
   ```

2. **Ensure Correct Dependency Format:**

   ```xml
   <!-- ✅ CORRECT - This will be detected -->
   <dependency>
       <groupId>com.example</groupId>
       <artifactId>common-lib</artifactId>
       <version>1.0.0</version>
   </dependency>

   <!-- ❌ WRONG - Version required -->
   <dependency>
       <groupId>com.example</groupId>
       <artifactId>common-lib</artifactId>
   </dependency>
   ```

3. **Check Project Language Detection:**

   ```bash
   # Analyzer only scans Java projects with pom.xml
   # Ensure your service has pom.xml, not package.json or build.gradle
   ls -la order-service/pom.xml
   ```

4. **Verify Maven Can Find Dependencies:**

   ```bash
   # Run Maven to ensure dependencies are resolved
   cd order-service
   mvn dependency:tree | grep common-lib
   ```

5. **Rebuild and Re-analyze:**
   ```bash
   # Sometimes caching issues occur
   mvn clean
   java -jar analyzer.jar /project --include-all
   ```

---

#### **Inconsistent Results Between Runs**

**Problem:** Different dependency counts when running multiple times.

**Possible Causes:**

- Stale compiled classes in target/ directories
- Caching in Maven repository
- Changes to application.yml not detected

**Solution:**

```bash
# Clean all build artifacts and re-analyze
find . -name "target" -type d -exec rm -rf {} +
mvn clean
java -jar analyzer.jar /project --include-all
```

---

#### **Gateway Routes with Complex Patterns**

**Problem:** Routes with complex predicates or filters not being detected.

**Note:** The analyzer extracts routes using a simple pattern:

```
Extract any line with: uri: lb://service-name
```

**Supported:**

```yaml
spring.cloud.gateway.routes:
  - id: order-service
    uri: lb://order-service
    predicates:
      - Path=/api/orders/**
    filters:
      - StripPrefix=1
```

**Not Directly Supported (but route will still be found):**

```yaml
spring.cloud.gateway.routes:
  - id: order-service
    predicates:
      - Path=/api/orders/**
    filters:
      - name: CircuitBreaker
        args:
          name: myCircuitBreaker
```

---

#### **Library Dependency Filtering**

**Problem:** Certain Maven dependencies are showing that you don't want to see.

**Solution - Use Custom Configuration:**

Create `analyzer-config.yml`:

```yaml
dependency_patterns:
  exclude_dependencies:
    - "org.springframework.*" # Exclude Spring Framework
    - "org.apache.*" # Exclude Apache projects
    - "junit*" # Exclude test dependencies
```

---

**Long Path Names:**

```batch
# Use short paths or enable long paths in Windows
# Windows 10+: reg add HKLM\SYSTEM\CurrentControlSet\Control\FileSystem /v LongPathsEnabled /t REG_DWORD /d 1 /f
```

#### **3. Windows Demo Script**

The repository includes `demo-enhanced-analyzer.bat` for easy testing:

```batch
# Run the demo
demo-enhanced-analyzer.bat

# This will:
# - Check prerequisites
# - Build the analyzer
# - Run analysis on current directory
# - Display results
```

### 📊 **Analysis Output Examples**

#### **Linux/macOS Output:**

```bash
🚀 Starting Generic Microservices Dependency Analysis...
📂 Project Path: /Users/you/my-microservices
⚙️ Using default configuration
🔍 Discovering services...
📋 Found 8 services:
   - gateway-service (gateway) at gateway-service
   - user-service (business) at user-service
   - product-service (business) at product-service
   - order-service (business) at order-service
   - payment-service (business) at payment-service
🔗 Analyzing dependencies...
📊 Found 14 dependency relationships
📈 Generating reports...
📂 Reports generated:
   ✅ dependency-report.html
   ✅ analysis-result.json
   ✅ dependency-matrix.csv
   ✅ impact-analysis.md
   ✅ dependency-graph.dot
   ✅ dependency-graph.svg
✅ Analysis completed successfully!
```

#### **Windows Output:**

```batch
🚀 Starting Generic Microservices Dependency Analysis...
📂 Project Path: C:\Users\you\my-microservices
⚙️ Using default configuration
🔍 Discovering services...
📋 Found 8 services:
   - gateway-service (gateway) at gateway-service
   - user-service (business) at user-service
   - product-service (business) at product-service
   - order-service (business) at order-service
   - payment-service (business) at payment-service
🔗 Analyzing dependencies...
📊 Found 14 dependency relationships
📈 Generating reports...
📂 Reports generated:
   ✅ dependency-report.html
   ✅ analysis-result.json
   ✅ dependency-matrix.csv
   ✅ impact-analysis.md
   ✅ dependency-graph.dot
   ✅ dependency-graph.svg
✅ Analysis completed successfully!
```

### 🔧 **Troubleshooting**

#### **Common Issues**

**"Java not found" Error:**

```bash
# Check Java installation
java -version

# On Windows, ensure JAVA_HOME is set
echo %JAVA_HOME%

# On Windows, check PATH includes Java bin directory
where java
```

**"Maven not found" Error:**

```bash
# Check Maven installation
mvn -version

# On Windows, ensure MAVEN_HOME is set
echo %MAVEN_HOME%
```

**Build Failures:**

```bash
# Clean and rebuild
mvn clean
mvn package -DskipTests

# Check Java version compatibility
java -version  # Should be Java 17+
```

**Permission Errors (Windows):**

```batch
# Run Command Prompt as Administrator
# Or check folder permissions on the project directory
# Ensure write access to the output directory
```

**Long Path Issues (Windows):**

```batch
# Enable long paths in Windows 10+
# Run PowerShell as Administrator:
reg add HKLM\SYSTEM\CurrentControlSet\Control\FileSystem /v LongPathsEnabled /t REG_DWORD /d 1 /f
```

**Memory Issues:**

```bash
# Increase Java heap size if analyzing large projects
java -Xmx2g -jar target/generic-microservices-dependency-analyzer-2.0.0.jar /path/to/project
```

**Graphviz Missing (Optional):**

```bash
# SVG generation will be skipped, but analysis continues
# Install Graphviz to enable diagram generation
```

---

## 📁 Generated Reports

### 🌐 **dependency-report.html**

Interactive web report with:

- **Service Overview**: Cards showing each service's type, framework, port
- **Visual Dependency Graph**: Embedded SVG with color-coded relationships
- **Impact Analysis**: "If service X changes, test services Y, Z"
- **Responsive Design**: Works on desktop, tablet, mobile

### 📊 **dependency-graph.svg**

Vector graphics dependency diagram:

- **Color-coded Services**: Blue=Gateway, Green=Business, Yellow=Config
- **Relationship Types**: Different colors for Feign, REST, Gateway routes
- **Scalable**: Zoom without quality loss
- **Embeddable**: Use in presentations, documentation

### 📈 **dependency-matrix.csv**

Business-friendly spreadsheet:

```csv
Source Service,Target Service,Dependency Type,Description,Source File,Line Number
order-service,user-service,feign-client,Feign client call to user-service,src/main/java/UserClient.java,15
gateway-service,order-service,gateway-route,Gateway route to order-service,src/main/resources/application.yml,23
```

### 📝 **impact-analysis.md**

Testing strategy documentation:

- **Change Impact Matrix**: Which services to test when others change
- **Risk Assessment**: Low/Medium/High impact changes
- **Business Justification**: Evidence for reduced regression testing

### 📋 **analysis-result.json**

Complete data in machine-readable format:

```json
{
  "analysis_date": "2026-01-24T10:30:00",
  "total_services": 8,
  "total_dependencies": 14,
  "services": [
    {
      "name": "order-service",
      "type": "business",
      "framework": "spring-boot",
      "port": 8083,
      "dependencies": [...]
    }
  ]
}
```

---

## ⚙️ Configuration

### 🔧 **Custom Configuration File**

Create `analyzer-config.yml` to customize detection patterns:

```yaml
# Service detection patterns
service_detection:
  build_files:
    - "**/pom.xml" # Maven
    - "**/package.json" # Node.js
    - "**/requirements.txt" # Python
    - "**/go.mod" # Go

  exclude_directories:
    - "target"
    - "node_modules"
    - ".git"

# Dependency patterns
dependency_patterns:
  feign_clients:
    - "@FeignClient"
    - "@Service"

  rest_templates:
    - "RestTemplate"
    - "axios" # JavaScript
    - "requests" # Python

# Output control
output_formats:
  html: true
  svg: true
  png: false
  json: true
  csv: true
  markdown: true
```

### 🎨 **Visualization Options**

```yaml
visualization:
  show_service_types: true # Display service types in diagrams
  show_dependency_types: true # Show edge labels (feign, rest, etc.)
  color_by_service_type: true # Color-code services by type
  include_config_services: true # Include config servers
  include_gateway_services: true # Include API gateways
```

---

## 🏗️ Architecture Support

### ☕ **Java Ecosystem**

- **Spring Boot**: @FeignClient, @RestController, @Service
- **Spring Cloud**: Gateway routes, Eureka discovery, Config server
- **Maven/Gradle**: Dependency management, multi-module projects
- **Microprofile**: JAX-RS, CDI patterns

### 🟨 **JavaScript/Node.js**

- **Express.js**: app.get(), app.post(), middleware
- **Next.js**: API routes, server components
- **Axios/Fetch**: HTTP client calls
- **NPM**: package.json dependencies

### 🐍 **Python**

- **Flask/FastAPI**: @app.route(), dependency injection
- **Requests**: HTTP client library
- **Django**: MVT patterns, REST framework
- **Requirements.txt**: Dependency management

### 🐹 **Go**

- **Gin/Echo**: HTTP routers and middleware
- **gRPC**: Service-to-service communication
- **go.mod**: Module dependencies

### 🚢 **Container Orchestration**

- **Kubernetes**: Services, Ingress, ConfigMaps
- **Docker Compose**: Multi-service definitions
- **Service Mesh**: Istio, Linkerd communication

---

## 📊 Business Value

### 💰 **ROI Calculation Example**

```
Traditional Approach:
- Full regression: 8 services × 4 hours = 32 hours
- QA cost: 3 people × 32 hours × $50/hour = $4,800 per release

Data-Driven Approach:
- Targeted testing: 2-3 services × 4 hours = 8-12 hours
- QA cost: 3 people × 10 hours × $50/hour = $1,500 per release

Monthly Savings: $3,300 (69% reduction)
```

### 📈 **Key Benefits**

- **60-80% Testing Time Reduction**: Target only affected services
- **Faster Release Cycles**: 3-day testing → 1-day testing
- **Evidence-Based Decisions**: Visual proof for stakeholders
- **Risk Mitigation**: Clear understanding of change impact
- **Developer Confidence**: Know exactly what to test

---

## 🔄 CI/CD Integration

### 🏗️ **Jenkins Pipeline**

```groovy
pipeline {
    stages {
        stage('Dependency Analysis') {
            steps {
                sh 'java -jar dependency-analyzer.jar ${WORKSPACE}'
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'dependency-analysis',
                    reportFiles: 'dependency-report.html',
                    reportName: 'Dependency Analysis Report'
                ])
            }
        }

        stage('Impact-Based Testing') {
            steps {
                script {
                    def analysis = readJSON file: 'dependency-analysis/analysis-result.json'
                    def changedServices = getChangedServices() // Your implementation
                    def servicesToTest = calculateImpact(changedServices, analysis)

                    echo "Changed services: ${changedServices}"
                    echo "Services to test: ${servicesToTest}"

                    // Run targeted tests only
                    runTargetedTests(servicesToTest)
                }
            }
        }
    }
}
```

### 🐙 **GitHub Actions**

```yaml
name: Smart Testing
on: [push, pull_request]

jobs:
  analyze-dependencies:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: "17"

      - name: Install Graphviz
        run: sudo apt-get install -y graphviz

      - name: Run Dependency Analysis
        run: |
          java -jar dependency-analyzer.jar ${{ github.workspace }}

      - name: Upload Analysis Report
        uses: actions/upload-artifact@v3
        with:
          name: dependency-analysis
          path: dependency-analysis/

      - name: Comment PR with Impact
        if: github.event_name == 'pull_request'
        run: |
          # Parse analysis and comment on PR with testing recommendations
          echo "Based on dependency analysis, test these services: ..."
```

---

## 🔧 Advanced Usage

### 🎯 **Custom Service Patterns**

```yaml
# Add support for your custom framework
dependency_patterns:
  custom_rpc:
    - "@GrpcClient"
    - "ServiceProxy"
    - "RpcInvoke"

  custom_messaging:
    - "@MessageConsumer"
    - "@EventPublisher"
    - "CustomQueue"
```

### 🏢 **Multi-Tenant Analysis**

```bash
# Analyze multiple environments
java -jar analyzer.jar /project/dev dev-config.yml
java -jar analyzer.jar /project/staging staging-config.yml
java -jar analyzer.jar /project/prod prod-config.yml
```

### 📊 **Continuous Monitoring**

```bash
# Schedule periodic analysis
crontab -e
0 2 * * 1 java -jar analyzer.jar /project >> /logs/dependency-analysis.log
```

---

## 🤝 Contributing

### 🔍 **Adding New Language Support**

1. Create scanner in `src/main/java/scanner/`
2. Add patterns to `AnalyzerConfiguration.java`
3. Update `GenericServiceDiscovery.java`
4. Add tests and documentation

### 🎨 **Custom Visualization**

1. Modify `EnhancedReportGenerator.java`
2. Update CSS styles in `getEnhancedCssStyles()`
3. Add new output formats as needed

### 🐛 **Bug Reports & Feature Requests**

Open issues with:

- Project type and structure
- Configuration file used
- Expected vs actual behavior
- Sample project (if possible)

---

## 📜 License

MIT License - see LICENSE file for details.

---

## 🎯 Examples

### 🏢 **Enterprise Spring Boot**

```bash
# Typical enterprise setup
java -jar analyzer.jar /enterprise/microservices enterprise-config.yml

# Generates:
# - 15+ services analysis
# - Gateway routing analysis
# - Database dependency mapping
# - Message queue relationships
```

### 🚀 **Kubernetes Native**

```bash
# Cloud-native microservices
java -jar analyzer.jar /k8s/services k8s-config.yml

# Analyzes:
# - Service mesh communication
# - Ingress controller routing
# - ConfigMap dependencies
# - Inter-pod networking
```

### 🔄 **Event-Driven Architecture**

```bash
# Event-driven microservices
java -jar analyzer.jar /event-driven event-config.yml

# Maps:
# - Event publishers/consumers
# - Message queue topology
# - Saga pattern flows
# - Event sourcing chains
```

---

## 🎯 Next Steps

### **Quick Start (5 minutes)**

1. **Build the Tool**: `mvn clean package`
2. **Analyze Your Project**: `java -jar analyzer.jar /your/project`
3. **Open HTML Report**: View `dependency-analysis/dependency-report.html`
4. **Explore Dependencies**: Check which services depend on yours

### **Comprehensive Analysis (10 minutes)**

1. **Build with Full Features**: `mvn clean package`
2. **Run Comprehensive Analysis**: `java -jar analyzer.jar /your/project --include-all`
3. **View Complete Architecture**: Check `dependency-analysis/dependency-report.html`
4. **Analyze Gateway Coverage**: See which services are routable through the API Gateway
5. **Check Library Adoption**: Identify which services use shared libraries (e.g., common-lib)

### **CI/CD Integration (30 minutes)**

1. **Choose Your Mode**: Default (daily testing) or --include-all (architecture reviews)
2. **Configure Your Pipeline**: Jenkins, GitHub Actions, or GitLab CI
3. **Integrate Impact Analysis**: Automatically determine which services to test
4. **Archive Reports**: Store dependency-analysis folder as build artifact
5. **Track Metrics**: Monitor dependency drift over time

### **Architecture Review (1 hour)**

1. **Run --include-all Mode**: Get complete architecture picture
2. **Export CSV**: Share `dependency-matrix.csv` with team
3. **Document Gateway Routes**: Review which services are routable
4. **Evaluate Library Usage**: Assess code reuse patterns
5. **Plan Improvements**:
   - Add missing gateway routes
   - Increase shared library adoption
   - Reduce unnecessary dependencies

---

## 📚 Documentation Structure

### **For Daily Development**

- Use **Default Mode** for regression testing decisions
- Answers: "If I change X, what do I need to test?"
- Output: 10-15 dependencies (direct service communication)

### **For Architecture Analysis**

- Use **--include-all Mode** for comprehensive view
- Answers: "How is the entire system connected?"
- Output: 18-25+ dependencies (includes all layers)

### **For Platform Teams**

- Monitor both modes over time
- Track when new gateway routes are added
- Measure library adoption growth
- Plan infrastructure improvements

---

## ✨ Key Takeaways

| Feature                   | Benefit                   | Mode          |
| ------------------------- | ------------------------- | ------------- |
| **Direct Dependencies**   | Know what to test daily   | Default       |
| **Gateway Routes**        | Understand API routing    | --include-all |
| **Library Usage**         | Measure code reuse        | --include-all |
| **Complete Architecture** | Plan system changes       | --include-all |
| **Fast Analysis**         | Quick feedback loops      | Default       |
| **Visual Reports**        | Stakeholder communication | Both          |

---

**Transform from "test everything always" to "test what actually matters based on evidence"** 🚀

**With the new features, you can also:**

- **See your API Gateway structure** with --include-all
- **Identify library adoption patterns** to improve code reuse
- **Plan architectural improvements** with complete dependency visibility
