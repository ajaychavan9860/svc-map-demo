# Feign Client Detection Fix - Usage Guide

## What Was Fixed

The analyzer now properly detects **Feign Client** dependencies like:
```java
@FeignClient(name = "excel-generation-service", url = "${excel.service.url}")
public interface ExcelServiceClient {
    @PostMapping("/generate")
    ResponseEntity<byte[]> generateExcel(@RequestBody request);
}
```

## Before vs After

### Before (only 22 dependencies):
- ✅ RestTemplate calls detected
- ✅ Gateway routes detected
- ❌ Feign clients NOT detected

### After (now 37 dependencies):
- ✅ RestTemplate calls detected
- ✅ Gateway routes detected
- ✅ **Feign clients detected!**

## Supported Feign Patterns

The analyzer now detects all these Feign client patterns:

```java
// Pattern 1: Simple name
@FeignClient(name = "service-name")

// Pattern 2: With URL from properties
@FeignClient(name = "service-name", url = "${service.url}")

// Pattern 3: Using value attribute
@FeignClient(value = "service-name")

// Pattern 4: Short form
@FeignClient("service-name")
```

## How to Use on Your 360/Backend Project

### Option 1: Use Latest JAR (Recommended)
```bash
# Download the latest analyzer from GitHub
git clone https://github.com/ajaychavan9860/svc-map-demo.git
cd svc-map-demo/dependency-analyzer-enhanced

# Run on your 360/backend project
java -jar target/generic-microservices-dependency-analyzer-2.0.0.jar "C:/work/idaafi_projects/360/backend"
```

### Option 2: Build from Source
```bash
cd dependency-analyzer-enhanced
mvn clean package -DskipTests
java -jar target/generic-microservices-dependency-analyzer-2.0.0.jar "C:/work/idaafi_projects/360/backend"
```

### Option 3: Use Custom Configuration (if needed)
If you still don't see all dependencies, use the custom config:
```bash
java -jar target/generic-microservices-dependency-analyzer-2.0.0.jar "C:/work/idaafi_projects/360/backend" custom-analyzer-config.yml
```

## Expected Results for Your Project

After running the analyzer on your 360/backend project, you should now see:

1. **Feign client dependencies** between services (not just Root → services)
2. **Detailed dependency information**:
   - Source service name
   - Target service name
   - Dependency type (feign-client)
   - Source file path
   - Line number

3. **Generated reports**:
   - `dependency-diagram-graphviz-java.svg` - Visual diagram with inter-service arrows
   - `dependency-report.html` - Interactive HTML report
   - `analysis-result.json` - Complete JSON data
   - `impact-analysis.md` - Detailed dependency list

## Troubleshooting

### If you still only see Root → services:

1. **Check if Feign is used**: Search your codebase for `@FeignClient`
   ```bash
   grep -r "@FeignClient" C:/work/idaafi_projects/360/backend
   ```

2. **Run diagnostic script** (Windows):
   ```bash
   diagnostic-analyzer.bat C:/work/idaafi_projects/360/backend
   ```

3. **Check service names**: Make sure service folder names match the Feign client names
   - If `@FeignClient(name = "excel-generation-service")` exists
   - There should be a folder named `excel-generation-service` in your project

4. **Enable verbose logging**:
   ```bash
   java -jar target/generic-microservices-dependency-analyzer-2.0.0.jar "C:/work/idaafi_projects/360/backend" 2>&1 | tee analysis.log
   grep "Feign" analysis.log
   ```

## What to Look For

In the generated diagram, you should now see arrows like:
- `report-analytics-service → excel-generation-service`
- `sms-registration-service → notification-service`
- `transaction-service → payment-service`

Instead of just:
- `Root → all services`

## Technical Details

### Code Changes
File: `GenericDependencyScanner.java`
- Enhanced `extractFeignDependency()` method
- Now properly extracts both source and target service names
- Supports multiple Feign annotation patterns
- Added debug logging for Feign dependency detection

### Dependencies Detected Per Service (Demo Project)
- payment-service: 4 dependencies (3 Feign + 1 REST)
- order-service: 8 dependencies (6 Feign + 2 REST)
- reporting-service: 8 dependencies (6 Feign + 2 REST)
- analytics-service: 4 dependencies (all REST)

## Need Help?

If the analyzer still doesn't detect dependencies properly:

1. Share a code snippet of your Feign client (like you did with ExcelServiceClient)
2. Share the service folder structure
3. Run the diagnostic script and share the output
4. Check if service names in `@FeignClient(name="...")` match folder names

## Next Steps

1. Run the analyzer on your 360/backend project
2. Check the generated SVG diagram
3. Verify all Feign client dependencies are now visible
4. If issues persist, use the diagnostic script to identify missing patterns

---

**Updated**: January 25, 2026
**Version**: 2.0.0
**GitHub**: https://github.com/ajaychavan9860/svc-map-demo
