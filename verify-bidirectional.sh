#!/bin/bash
# Verify bidirectional dependencies detection

echo "========================================="
echo "Bidirectional Dependency Verification"
echo "========================================="
echo ""

# Check if analysis JSON exists
if [ ! -f "dependency-analysis/analysis-result.json" ]; then
    echo "❌ No analysis results found. Run the analyzer first."
    exit 1
fi

echo "📊 Analyzing dependencies for bidirectional patterns..."
echo ""

# Extract all dependencies and group by service pairs
cat dependency-analysis/analysis-result.json | \
    jq -r '.dependencies[] | "\(.fromService) -> \(.toService) [\(.dependencyType)]"' | \
    sort > /tmp/all_deps.txt

# Find potential bidirectional pairs
echo "🔍 Checking for bidirectional dependencies..."
echo ""

while IFS= read -r line; do
    from=$(echo "$line" | cut -d' ' -f1)
    to=$(echo "$line" | cut -d' ' -f3)
    type=$(echo "$line" | cut -d'[' -f2 | tr -d ']')
    
    # Check if reverse dependency exists
    reverse=$(grep -E "^$to -> $from" /tmp/all_deps.txt || true)
    
    if [ ! -z "$reverse" ]; then
        reverse_type=$(echo "$reverse" | cut -d'[' -f2 | tr -d ']')
        echo "✓ BIDIRECTIONAL FOUND:"
        echo "  Forward:  $from -> $to [$type]"
        echo "  Backward: $to -> $from [$reverse_type]"
        echo ""
    fi
done < /tmp/all_deps.txt | sort -u

echo ""
echo "========================================="
echo "📋 All Dependencies (sorted by service):"
echo "========================================="
cat /tmp/all_deps.txt

echo ""
echo "========================================="
echo "📊 Statistics:"
echo "========================================="
total=$(wc -l < /tmp/all_deps.txt)
echo "Total dependencies: $total"

# Count unique service pairs (ignoring direction)
cat /tmp/all_deps.txt | \
    awk '{
        if ($1 < $3) 
            print $1, $3
        else 
            print $3, $1
    }' | sort -u > /tmp/pairs.txt

pairs=$(wc -l < /tmp/pairs.txt)
echo "Unique service pairs: $pairs"

if [ $total -gt $pairs ]; then
    bidirectional=$((total - pairs))
    echo "Bidirectional arrows: $bidirectional (estimated)"
fi

rm -f /tmp/all_deps.txt /tmp/pairs.txt
