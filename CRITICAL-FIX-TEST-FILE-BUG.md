# CRITICAL FIX: Test File Exclusion Bug

## Problem Found

The analyzer was excluding **any file** with "test" in its name or path, not just files in `/test/` directories.

This caused files like:

- `TestEndpointController.java`
- `TestClient.java`
- `CcgTestService.java`
- Any file with "test" anywhere in the path

to be **completely ignored** during:

1. Endpoint extraction
2. Endpoint string searching
3. Feign client detection

## Root Cause

Three filters in GenericDependencyScanner.java were using:

```java
.filter(path -> !path.toString().toLowerCase().contains("test"))
```

This is too broad! It excludes files like `/src/main/java/TestController.java` when it should only exclude `/src/test/java/Controller.java`.

## Fix Applied

Changed all 3 occurrences to:

```java
.filter(path -> !path.toString().contains("/test/") && !path.toString().contains("\\test\\"))
```

Now it only excludes actual test directories (src/test/java, test/resources, etc.).

## Locations Fixed

1. Line ~88: `extractServiceEndpoints()` - endpoint extraction from controllers
2. Line ~524: `searchForEndpointInService()` - searching for endpoint strings
3. Line ~556: `scanJavaFiles()` - general Java file scanning

## Impact on Your 360/Backend Project

### Why ccg-kafka-consumer → ccg-core-service might be missing:

**Scenario 1: File name issue**
If CcgCoreServiceProxy.java or any controller has "test" in the path, it was being ignored.

**Scenario 2: Feign client detection**  
If CcgCoreServiceProxy.java has:

```java
@FeignClient(name = "ccg-core-service")
public interface CcgCoreServiceProxy {
    @PostMapping("/v1/rawMessage")
    ...
}
```

It should now be detected!

**Scenario 3: Endpoint string search**
If the string `"/v1/rawMessage"` appears anywhere in ccg-kafka-consumer code (even in a Test file), it will now be found.

### Why ccg-core-service → report-generation might be missing:

Same issue! Check if:

1. Report-generation-service has endpoints in controllers
2. Ccg-core-service has strings matching those endpoints
3. Any files involved have "test" in their path

## Testing Performed

Created test case in demo project:

- ✅ TestEndpointController.java with `/v1/rawMessage` endpoint
- ✅ ProductTestClient.java Feign client calling that endpoint
- ✅ TestClient.java with endpoint string

All now detected correctly!

## Next Steps

1. Copy the updated JAR to your 360/backend project
2. Run the analyzer again
3. Check the logs for:
   - "EXTRACTING ENDPOINTS FROM: ccg-core-service"
   - "Found X Java files to scan" (should include all controllers, even with "test" in name)
   - "Analyzing Feign client in ccg-kafka-consumer"
   - "Found Feign dependency: ccg-kafka-consumer -> ccg-core-service"

If still missing, the logs will now show exactly which files are being scanned and which endpoints are being found.

## Updated JAR Location

```
/Users/ajay/svc-map-demo/dependency-analyzer-enhanced/target/generic-microservices-dependency-analyzer-2.0.0.jar
```

Size: 21M
Build time: Jan 26 15:45
