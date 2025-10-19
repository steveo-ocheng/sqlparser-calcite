#!/bin/bash

# SQL Parser Runner Script
# This script sets up Java and runs the SQL parser

# Find Java (prefer Java 17 if available)
if [ -f "/opt/homebrew/opt/openjdk@17/bin/java" ]; then
    JAVA_CMD="/opt/homebrew/opt/openjdk@17/bin/java"
elif [ -f "/opt/homebrew/opt/openjdk@11/bin/java" ]; then
    JAVA_CMD="/opt/homebrew/opt/openjdk@11/bin/java"
elif command -v java &> /dev/null; then
    JAVA_CMD="java"
else
    echo "Error: Java not found. Please install Java 11 or higher."
    exit 1
fi

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAR_FILE="$SCRIPT_DIR/build/libs/sqlparser-all-1.0.0.jar"

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    echo "Please build the project first with: gradle build"
    exit 1
fi

# Run the SQL parser with all arguments passed through
$JAVA_CMD -jar "$JAR_FILE" "$@"
