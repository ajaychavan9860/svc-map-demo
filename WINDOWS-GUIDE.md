# Running Diagnostic Scripts on Windows

## Option 1: Use Batch Files (Recommended for Windows)

Windows batch file versions are provided with `.bat` extension:

### Quick Start

```cmd
REM Clone the repository
git clone https://github.com/ajaychavan9860/svc-map-demo.git
cd svc-map-demo

REM Build the analyzer
cd dependency-analyzer-enhanced
mvn clean package -DskipTests
cd ..

REM Run diagnostic on your project
diagnose-ccg-dependency.bat C:\path\to\your\360\backend

REM Or analyze the entire project
analyze-360-backend.bat C:\path\to\your\360\backend

REM Check bidirectional test results
show-bidirectional-test.bat
```

### Available Batch Scripts

- `diagnose-ccg-dependency.bat` - Detailed CCG service diagnostic
- `analyze-360-backend.bat` - Full project analysis
- `show-bidirectional-test.bat` - Verify bidirectional dependencies

---

## Option 2: Use Git Bash (Better JSON support)

Git Bash comes with Git for Windows and can run the `.sh` scripts:

### Install Git Bash

1. Download Git for Windows: https://git-scm.com/download/win
2. Install with default options (includes Git Bash)

### Run Scripts

```bash
# Open Git Bash terminal
cd /c/path/to/svc-map-demo

# Make scripts executable
chmod +x *.sh

# Run any script
./diagnose-ccg-dependency.sh /c/path/to/360/backend
./analyze-360-backend.sh /c/path/to/360/backend
./show-bidirectional-test.sh
```

**Advantages:**

- Full JSON parsing with `jq` (included in Git Bash)
- Better output formatting
- All script features work

---

## Option 3: Use WSL (Windows Subsystem for Linux)

### Install WSL

```powershell
# Run in PowerShell as Administrator
wsl --install
```

### Run Scripts

```bash
# Open WSL terminal
cd /mnt/c/path/to/svc-map-demo

# Make scripts executable
chmod +x *.sh

# Run any script
./diagnose-ccg-dependency.sh /mnt/c/path/to/360/backend
```

---

## Running Analyzer Directly

You can always run the analyzer JAR directly on Windows:

```cmd
cd dependency-analyzer-enhanced
mvn clean package -DskipTests

java -jar target\generic-microservices-dependency-analyzer-2.0.0.jar C:\path\to\your\project
```

### View Results

- **HTML Report:** `C:\path\to\your\project\dependency-analysis\dependency-report.html`
- **SVG Diagram:** `C:\path\to\your\project\dependency-analysis\dependency-diagram-graphviz-java.svg`
- **JSON Data:** `C:\path\to\your\project\dependency-analysis\analysis-result.json`

---

## Troubleshooting

### Maven not found

```cmd
REM Download Maven from https://maven.apache.org/download.cgi
REM Add to PATH: C:\path\to\apache-maven-3.x.x\bin
```

### Java not found

```cmd
REM Download JDK from https://adoptium.net/
REM Add to PATH: C:\Program Files\Java\jdk-17\bin
```

### Unicode characters showing as boxes

The batch files avoid Unicode emojis. For better output, use Git Bash or WSL.

### JSON parsing limitations

Batch files have basic JSON support. For advanced JSON queries:

- Install `jq` for Windows: https://stedolan.github.io/jq/download/
- Or use Git Bash (includes jq)
- Or use WSL

---

## Path Formats

### Command Prompt / Batch

```cmd
C:\Users\username\project\360\backend
```

### Git Bash

```bash
/c/Users/username/project/360/backend
```

### WSL

```bash
/mnt/c/Users/username/project/360/backend
```

---

## Example: Full Workflow on Windows

```cmd
REM 1. Clone repository
git clone https://github.com/ajaychavan9860/svc-map-demo.git
cd svc-map-demo

REM 2. Build analyzer
cd dependency-analyzer-enhanced
mvn clean package -DskipTests
cd ..

REM 3. Run diagnostic
diagnose-ccg-dependency.bat C:\code\360\backend

REM 4. Open diagram
start C:\code\360\backend\dependency-analysis\dependency-diagram-graphviz-java.svg

REM 5. Open HTML report
start C:\code\360\backend\dependency-analysis\dependency-report.html
```

---

## Need Help?

If you encounter issues:

1. Check Java version: `java -version` (should be 17+)
2. Check Maven version: `mvn -version` (should be 3.6+)
3. Verify project path exists
4. Check build logs for errors

For best experience, use **Git Bash** - it's free, easy to install, and works perfectly with all scripts!
