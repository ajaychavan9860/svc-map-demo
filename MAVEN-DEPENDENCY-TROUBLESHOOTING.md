# Maven Dependency Detection Troubleshooting Guide

## Quick Diagnosis

If bidirectional dependencies aren't showing in your project, follow these steps:

### Step 1: Run the Diagnostic Script

```bash
./diagnose-bidirectional.sh /path/to/your/360/backend
```

This will show you:

- All services with pom.xml files
- Internal dependencies found in each pom.xml
- Dependency matrix
- Feign client dependencies

### Step 2: Enable Debug Logging

Run the analyzer with debug logging to see what's happening:

```bash
# Download latest version
git pull origin main
cd dependency-analyzer-enhanced
mvn clean package -DskipTests

# Run with output to file for analysis
java -jar target/generic-microservices-dependency-analyzer-2.0.0.jar /path/to/your/360/backend 2>&1 | tee analysis.log

# Search for Maven dependency logs
grep "📦 Scanning Maven" analysis.log
grep "✅ Found Maven dependency" analysis.log
grep "⚠️  Potential internal" analysis.log
```

### Step 3: Check Common Issues

#### Issue 1: Different GroupIds

**Symptom**: Services have different `groupId` in their pom.xml

**Check**:

```bash
# Find all groupIds in your project
find /path/to/360/backend -name "pom.xml" -not -path "*/target/*" | while read pom; do
    echo "=== $(dirname $pom) ==="
    xmllint --xpath "string(//*[local-name()='project']/*[local-name()='groupId'])" "$pom" 2>/dev/null || \
    xmllint --xpath "string(//*[local-name()='project']/*[local-name()='parent']/*[local-name()='groupId'])" "$pom" 2>/dev/null
done
```

**Solution**: All services in the same project should use the same `groupId` (e.g., `com.company.360`). If they're different, the analyzer won't detect them as internal dependencies.

#### Issue 2: ArtifactId Doesn't Match Folder Name

**Symptom**: The `artifactId` in pom.xml doesn't match the service folder name

**Example**:

```
Folder: ccg-core-service
ArtifactId in pom.xml: core-service
```

**Check**:

```bash
# Compare folder names with artifactIds
find /path/to/360/backend -name "pom.xml" -not -path "*/target/*" | while read pom; do
    FOLDER=$(basename $(dirname $pom))
    ARTIFACT=$(xmllint --xpath "string(//*[local-name()='project']/*[local-name()='artifactId'])" "$pom" 2>/dev/null)
    if [ "$FOLDER" != "$ARTIFACT" ]; then
        echo "⚠️  Mismatch: folder=$FOLDER, artifactId=$ARTIFACT"
    fi
done
```

**Solution**: The analyzer uses fuzzy matching, so `ccg-core-service` should match `core-service`. If it still doesn't work, check the debug logs for the normalized values.

#### Issue 3: Test/Provided Scope Dependencies

**Symptom**: Dependencies are defined with `<scope>test</scope>` or `<scope>provided</scope>`

**Check** in your pom.xml:

```xml
<dependency>
    <groupId>com.company.360</groupId>
    <artifactId>common-lib</artifactId>
    <scope>test</scope>  <!-- ❌ Will be skipped -->
</dependency>
```

**Solution**: Remove the scope or use `<scope>compile</scope>` for internal dependencies that should be detected.

#### Issue 4: Dependencies Only in DependencyManagement

**Symptom**: Dependencies are defined in `<dependencyManagement>` section only, not in `<dependencies>`

**Check**:

```xml
<dependencyManagement>
    <dependencies>
        <!-- These won't be detected -->
        <dependency>
            <groupId>com.company.360</groupId>
            <artifactId>common-lib</artifactId>
        </dependency>
    </dependencies>
</dependencyManagement>
```

**Solution**: Make sure dependencies are in the `<dependencies>` section, not just `<dependencyManagement>`.

#### Issue 5: Services Not Discovered

**Symptom**: The analyzer doesn't find some services at all

**Check** the analyzer output:

```bash
java -jar target/generic-microservices-dependency-analyzer-2.0.0.jar /path/to/360/backend 2>&1 | grep "Found .* services"
```

**Solution**: Make sure each service has a `pom.xml` file and is not excluded as a parent/aggregator POM.

### Step 4: Analyze Debug Output

Look for these patterns in the logs:

**Good - Dependency Found**:

```
📦 Scanning Maven dependencies for order-service (groupId: com.demo.microservices)
   Found 5 dependencies in pom.xml
   Checking dependency: com.demo.microservices:common-lib (scope: compile)
      ✓ Exact match: com.demo.microservices:common-lib == com.demo.microservices:common-lib
✅ Found Maven dependency: order-service -> common-lib (com.demo.microservices:common-lib)
```

**Problem - Dependency Not Matched**:

```
📦 Scanning Maven dependencies for ccg-core-service (groupId: com.company.360)
   Found 8 dependencies in pom.xml
   Checking dependency: com.company.360:task-management (scope: compile)
   ⚠️  Potential internal dependency not matched: com.company.360:task-management
      Available services: ccg-core-service, task-management-service, excel-service, ...
```

This shows the artifactId (`task-management`) doesn't match any service name (`task-management-service`). The fuzzy matcher should handle this, so check the normalized values.

### Step 5: Manual Verification

Check if dependencies exist in your pom.xml files:

```bash
# Find all internal dependencies in a specific service
cd /path/to/360/backend/ccg-core-service
grep -A 2 "<groupId>com.company.360</groupId>" pom.xml | grep "<artifactId>"
```

### Common Fixes

1. **Update to latest version**:

   ```bash
   cd /path/to/svc-map-demo
   git pull origin main
   cd dependency-analyzer-enhanced
   mvn clean package -DskipTests
   ```

2. **Verify groupId consistency**:

   ```bash
   # All services should have the same groupId
   # Edit pom.xml if needed
   ```

3. **Check dependency scopes**:

   ```bash
   # Make sure internal dependencies don't have test/provided scope
   ```

4. **Run with full logging**:

   ```bash
   java -jar target/generic-microservices-dependency-analyzer-2.0.0.jar /path/to/360/backend 2>&1 | tee full-analysis.log

   # Search for specific patterns
   grep "⚠️" full-analysis.log  # Warnings about unmatched dependencies
   grep "📦" full-analysis.log  # Maven scanning details
   ```

### Still Not Working?

If you've checked all the above and it's still not working:

1. **Share the logs**: Run with `2>&1 | tee analysis.log` and share the relevant sections
2. **Share pom.xml samples**: Share 2-3 pom.xml files from services that should have dependencies
3. **Check the diagnostic script output**: Run `./diagnose-bidirectional.sh /path/to/360/backend` and share the output

### Expected Behavior

When working correctly, you should see:

1. **In console output**:

   ```
   ✅ Found Maven dependency: service-a -> common-lib (com.company:common-lib)
   ✅ Found Maven dependency: service-b -> common-lib (com.company:common-lib)
   📊 Found 205 dependency relationships
   ```

2. **In impact-analysis.md**:

   ```markdown
   ### service-a → common-lib

   - **Type**: maven-dependency
   - **Source**: pom.xml
   ```

3. **In the SVG diagram**: Arrows from both service-a and service-b pointing to common-lib

4. **Bidirectional**: If service-a depends on service-b (Maven) and service-b calls service-a (Feign), you'll see arrows in BOTH directions.
