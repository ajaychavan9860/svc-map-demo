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

## 📦 Installation & Usage

### 📋 **Prerequisites**

- **Java 17+** (for running the analyzer)
- **Graphviz** (optional, for SVG/PNG generation)

  ```bash
  # macOS
  brew install graphviz

  # Ubuntu/Debian
  sudo apt-get install graphviz

  # Windows
  choco install graphviz
  ```

### 🏗️ **Build the Tool**

```bash
git clone <your-repo>
cd dependency-analyzer-enhanced

# Build the analyzer
mvn clean package -DskipTests

# The JAR will be created as:
# target/generic-microservices-dependency-analyzer-2.0.0.jar
```

### 🔧 **Basic Usage**

```bash
# Analyze with default settings
java -jar target/generic-microservices-dependency-analyzer-2.0.0.jar /path/to/your/microservices

# Use custom configuration
java -jar target/generic-microservices-dependency-analyzer-2.0.0.jar /path/to/your/microservices /path/to/analyzer-config.yml
```

### 📊 **Example Output**

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

1. **Build the Tool**: `mvn clean package`
2. **Analyze Your Project**: `java -jar analyzer.jar /your/project`
3. **Open HTML Report**: View `dependency-analysis/dependency-report.html`
4. **Present to Stakeholders**: Use visual evidence to justify targeted testing
5. **Integrate into CI/CD**: Automate dependency-aware testing

**Transform from "test everything always" to "test what actually matters based on evidence"** 🚀
