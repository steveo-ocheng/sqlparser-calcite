# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Java application that parses SQL SELECT statements using Apache Calcite and generates:
- Detailed analysis of tables, columns, conditions, joins, etc.
- Human-readable English descriptions of what the query does

## Build and Development Commands

### Building
```bash
# Build the project (creates fat JAR with all dependencies)
./gradlew build

# On Windows
gradlew.bat build

# Note: Requires Java 17+ for Gradle 9.x build tool
# The project itself targets Java 11
```

The build produces `build/libs/sqlparser-calcite-all-1.0.0.jar` (fat JAR with Apache Calcite and all dependencies).

### Running

```bash
# Using convenience script
./sqlparser.sh examples/simple-query.sql

# Or run JAR directly
java -jar build/libs/sqlparser-calcite-all-1.0.0.jar examples/simple-query.sql

# Run with Gradle
./gradlew run
```

### Testing

```bash
# Run all tests (JUnit 5)
./gradlew test

# Run a specific test class
./gradlew test --tests SqlAnalyzerTest

# Run a specific test method
./gradlew test --tests SqlAnalyzerTest.testSimpleSelectSingleColumn

# Run tests with verbose output
./gradlew test --info
```

Test files are located in `src/test/java/com/sqlparser/`:
- `SqlAnalyzerTest.java` - Core parsing functionality tests
- `SqlAnalysisResultTest.java` - Result formatting tests
- `WindowFunctionTest.java` - Window function parsing tests

### CLI Options

- `--help, -h` - Show help message
- `--version, -v` - Show version
- `--examples` - Run built-in SQL examples
- `--verbose` - Show SQL query + full analysis
- `--quiet, -q` - Only show English description

## Architecture

### Core Components

**SqlAnalyzer** (`src/main/java/com/sqlparser/SqlAnalyzer.java`)
- Main parsing engine using Apache Calcite library
- Processes SELECT statements via `analyze(String sql)` method
- Traverses Calcite's SQL AST (SqlNode tree) to extract:
  - Tables and aliases
  - Columns organized by table
  - SELECT items, WHERE conditions, JOIN conditions
  - GROUP BY, HAVING, ORDER BY, LIMIT/FETCH clauses
- Generates natural language descriptions via `generateDescription()`
- State is maintained in instance variables and reset before each analysis

**SqlAnalysisResult** (`src/main/java/com/sqlparser/SqlAnalysisResult.java`)
- Immutable data class holding analysis results
- Contains getters for all extracted SQL components
- Provides `toFormattedString()` for pretty-printed output
- All fields are final and set via constructor

**SqlParserCli** (`src/main/java/com/sqlparser/SqlParserCli.java`)
- Command-line interface entry point (main class configured in build.gradle)
- Handles file reading, argument parsing, output formatting
- Supports various CLI modes (verbose, quiet, examples)
- Contains hardcoded example SQL queries in `runExamples()`

**SqlParserApp** (`src/main/java/com/sqlparser/SqlParserApp.java`)
- Alternative example application demonstrating programmatic API usage
- Shows how to use SqlAnalyzer in Java code
- Not the default main class (SqlParserCli is used for the JAR)

### Key Design Patterns

- **Stateful Analysis**: SqlAnalyzer maintains mutable state during parsing (reset before each use)
- **Immutable Results**: SqlAnalysisResult is immutable once created
- **Recursive Traversal**: Methods recursively traverse Calcite's SqlNode tree

### Apache Calcite Integration

The project uses Apache Calcite 1.37.0 to parse SQL. Key classes from Calcite:
- `SqlParser.create()` - Entry point for creating parser
- `SqlParser.parseQuery()` - Parses SQL string into SqlNode tree
- `SqlSelect` - Represents a SELECT statement
- `SqlNode`, `SqlIdentifier`, `SqlBasicCall`, `SqlJoin` - AST node types
- `SqlOrderBy` - Wrapper node for queries with ORDER BY/LIMIT
- Methods like `extractColumnsFromNode()` recursively traverse the AST

### Data Flow

1. SQL string → `SqlAnalyzer.analyze()`
2. Create SqlParser with configuration (case-insensitive, lenient conformance)
3. Parse to SqlNode AST via `parser.parseQuery()`
4. Extract components via `processSelect()` and helper methods
5. Build `SqlAnalysisResult` with extracted data + generated description
6. Format output via `SqlAnalysisResult.toFormattedString()`

### Why Apache Calcite?

Apache Calcite provides:
- **Robust parsing**: Battle-tested parser used by Apache Hive, Drill, Flink
- **SQL validation**: Can validate queries against schemas (not currently used)
- **Type inference**: Understands SQL types and can perform type checking
- **Extensibility**: Can be extended with custom SQL dialects
- **Industry adoption**: Used by major database systems and query engines

## Example SQL Files

Located in `examples/` directory - useful for testing changes to the parser.

**Basic Examples:**
- `simple-query.sql` - Basic SELECT with WHERE and ORDER BY
- `join-query.sql` - INNER JOIN with multiple conditions
- `aggregation-query.sql` - GROUP BY with aggregate functions
- `multi-join-query.sql` - Multiple table joins
- `subquery.sql` - Query with subquery in WHERE clause

**Advanced CTE Examples:**
- `complex-cte-query.sql` - Multi-CTE query with analytics
- `recursive-cte-query.sql` - Recursive CTE for hierarchies
- `multiple-cte-analytics.sql` - User segmentation and revenue analytics

**Window Function Examples (20 files):**
The `examples/` directory contains 20 window function examples (`window-01-*.sql` through `window-20-*.sql`) covering:
- ROW_NUMBER, RANK, DENSE_RANK partitioning
- Running totals and moving averages
- LAG/LEAD functions for period comparisons
- FIRST_VALUE/LAST_VALUE for session analysis
- NTILE for quartile segmentation
- Gap/island detection and streak analysis
- Percentile calculations, cohort analysis, funnel analysis, and more
