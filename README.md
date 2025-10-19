# SQL Parser

A Java application that parses SQL SELECT statements using Apache Calcite and provides detailed analysis including:
- All tables and their columns used in the query
- Selected columns
- WHERE conditions, JOINs, GROUP BY, HAVING, ORDER BY, and LIMIT clauses
- A human-readable English description of what the SQL query does

## Features

- Parses all types of SQL SELECT statements
- Handles simple and complex queries including:
  - Simple SELECT with WHERE clauses
  - JOINs (INNER, LEFT, RIGHT, FULL)
  - Subqueries
  - Aggregate functions (COUNT, SUM, AVG, etc.)
  - GROUP BY and HAVING clauses
  - ORDER BY and LIMIT clauses
  - Complex expressions and functions
- Extracts all tables and columns used in the query
- Generates natural language descriptions of SQL queries

## Requirements

- Java 11 or higher (for building from source)
- No requirements for running the fat JAR (just Java Runtime)

## Quick Start - Using the Pre-built JAR

The easiest way to use SQL Parser is with the fat JAR (all dependencies included):

### 1. Build the fat JAR

```bash
cd sqlparser

# Set JAVA_HOME to Java 17+ (required for Gradle 9.x)
export JAVA_HOME=/opt/homebrew/opt/openjdk@17  # macOS with Homebrew
export PATH="$JAVA_HOME/bin:$PATH"

gradle build
```

On Windows:
```bash
set JAVA_HOME=C:\Program Files\Java\jdk-17
gradlew.bat build
```

This creates `build/libs/sqlparser-calcite-all-1.0.0.jar` (includes all dependencies including Apache Calcite)

### 2. Run with a SQL file

Using the convenience script (automatically finds Java):
```bash
./sqlparser.sh examples/simple-query.sql
```

Or run the JAR directly:
```bash
java -jar build/libs/sqlparser-calcite-all-1.0.0.jar examples/simple-query.sql
```

### 3. Command-line options

```bash
# Show help
java -jar build/libs/sqlparser-calcite-all-1.0.0.jar --help

# Run built-in examples
java -jar build/libs/sqlparser-calcite-all-1.0.0.jar --examples

# Verbose output (shows SQL query + full analysis)
java -jar build/libs/sqlparser-calcite-all-1.0.0.jar query.sql --verbose

# Quiet mode (only English description)
java -jar build/libs/sqlparser-calcite-all-1.0.0.jar query.sql --quiet

# Show version
java -jar build/libs/sqlparser-calcite-all-1.0.0.jar --version
```

## Example SQL Files

The project includes several example SQL files in the `examples/` directory:

**Basic Examples:**
- `simple-query.sql` - Basic SELECT with WHERE and ORDER BY
- `join-query.sql` - INNER JOIN with multiple conditions
- `aggregation-query.sql` - GROUP BY with aggregate functions
- `multi-join-query.sql` - Multiple table joins
- `subquery.sql` - Query with subquery in WHERE clause

**Advanced CTE Examples:**
- `complex-cte-query.sql` - Complex multi-CTE query with customer analytics, product metrics, and ranking functions
- `recursive-cte-query.sql` - Recursive CTE for employee hierarchy with department statistics and performance analysis
- `multiple-cte-analytics.sql` - Advanced analytics with user segmentation, engagement scoring, and revenue attribution

**Window Function Examples (20 files):**
- `window-01-row-number-partitioning.sql` - ROW_NUMBER with multiple partition strategies
- `window-02-rank-dense-rank.sql` - RANK vs DENSE_RANK comparison with distribution functions
- `window-03-running-totals.sql` - Cumulative aggregates with ROWS BETWEEN
- `window-04-moving-averages.sql` - 7, 30, 90, 200-day moving averages for stock analysis
- `window-05-lead-lag-functions.sql` - LAG/LEAD for period-over-period comparisons
- `window-06-first-last-value.sql` - Session entry/exit page analysis
- `window-07-ntile-quartiles.sql` - Customer segmentation into quartiles/deciles
- `window-08-range-between.sql` - RANGE BETWEEN for date-based windows
- `window-09-multiple-partitions.sql` - Multi-dimensional analytics with different partitions
- `window-10-named-windows.sql` - WINDOW clause for reusable window definitions
- `window-11-gap-island-detection.sql` - Streak detection and gap analysis
- `window-12-percentile-calculations.sql` - PERCENTILE_CONT/DISC for distribution analysis
- `window-13-conditional-aggregates.sql` - FILTER clause and conditional windows
- `window-14-year-over-year-comparison.sql` - YoY, QoQ, MoM growth calculations
- `window-15-top-n-per-group.sql` - Top performers per category
- `window-16-session-analytics.sql` - User session event sequencing
- `window-17-cohort-analysis.sql` - Cohort retention and lifetime value
- `window-18-inventory-running-balance.sql` - Inventory tracking with stock movements
- `window-19-funnel-conversion-analysis.sql` - Multi-step conversion funnel analysis
- `window-20-time-series-forecasting.sql` - Trend detection and anomaly identification

Try them out:
```bash
./sqlparser.sh examples/join-query.sql
# or
java -jar build/libs/sqlparser-calcite-all-1.0.0.jar examples/join-query.sql

# Try a complex CTE example
java -jar build/libs/sqlparser-calcite-all-1.0.0.jar examples/complex-cte-query.sql --verbose

# Try window functions
java -jar build/libs/sqlparser-calcite-all-1.0.0.jar examples/window-04-moving-averages.sql --quiet
```

## Building and Running from Source

### Build the project

```bash
cd sqlparser
./gradlew build
```

On Windows:
```bash
gradlew.bat build
```

### Run the example application

```bash
./gradlew run
```

On Windows:
```bash
gradlew.bat run
```

## Usage in Your Code

```java
import com.sqlparser.SqlAnalyzer;
import com.sqlparser.SqlAnalysisResult;

public class Example {
    public static void main(String[] args) {
        SqlAnalyzer analyzer = new SqlAnalyzer();

        String sql = "SELECT u.name, o.total FROM users u " +
                     "INNER JOIN orders o ON u.id = o.user_id " +
                     "WHERE o.total > 100";

        try {
            SqlAnalysisResult result = analyzer.analyze(sql);

            // Get tables
            System.out.println("Tables: " + result.getTables());

            // Get table-column mapping
            System.out.println("Columns by table: " + result.getTableColumns());

            // Get English description
            System.out.println("Description: " + result.getDescription());

            // Print formatted output
            System.out.println(result.toFormattedString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## Example Output

For the query:
```sql
SELECT u.name, o.order_date, o.total
FROM users u
INNER JOIN orders o ON u.id = o.user_id
WHERE o.total > 100
ORDER BY o.order_date DESC
```

The analyzer produces:

```
=== SQL ANALYSIS ===

DESCRIPTION:
  This query retrieves 3 columns (u.name, o.order_date, o.total) from 2 tables (users (alias: u), orders (alias: o)), joining tables on condition: INNER JOIN orders o ON u.id = o.user_id. The results are filtered where o.total > 100. Results are sorted by o.order_date DESC.

TABLES:
  - users (alias: u)
  - orders (alias: o)

TABLE-COLUMNS:
  u:
    - name
    - id
  o:
    - order_date
    - total
    - user_id

SELECTED COLUMNS:
  - u.name
  - o.order_date
  - o.total

WHERE CONDITIONS:
  - o.total > 100

JOIN CONDITIONS:
  - INNER JOIN orders o ON u.id = o.user_id

ORDER BY:
  - o.order_date DESC
```

## Supported SQL Features

- SELECT with column lists or wildcard (*)
- FROM clause with table aliases
- INNER JOIN, LEFT JOIN, RIGHT JOIN, FULL JOIN
- WHERE clause with conditions
- GROUP BY clause
- HAVING clause
- ORDER BY clause (ASC/DESC)
- LIMIT clause
- Aggregate functions (COUNT, SUM, AVG, MIN, MAX, etc.)
- Subqueries in FROM and WHERE clauses
- Complex expressions and functions
- Column aliases (AS)

## Dependencies

This project uses:
- [Apache Calcite](https://calcite.apache.org/) v1.37.0 - SQL parsing and validation library
- SLF4J + Logback - Logging framework
- JUnit 5 - Testing framework

## Why Apache Calcite?

Apache Calcite is a powerful SQL parser and query optimizer used by many database systems including:
- Apache Hive, Drill, Flink, Storm, Samza
- Elasticsearch SQL
- Google BigQuery (via ZetaSQL)

Benefits over JSqlParser:
- More robust parser with better SQL dialect support
- Validation and type-checking capabilities
- Used in production by major database systems
- Active development by Apache Foundation
- Extensible for custom SQL dialects

## Project Structure

```
sqlparser/
├── build.gradle
├── settings.gradle
├── gradlew                         # Gradle wrapper (Unix/Mac)
├── README.md
├── examples/                       # Sample SQL files
│   ├── simple-query.sql
│   ├── join-query.sql
│   ├── aggregation-query.sql
│   ├── multi-join-query.sql
│   └── subquery.sql
└── src/
    └── main/
        └── java/
            └── com/
                └── sqlparser/
                    ├── SqlAnalyzer.java        # Core parsing logic
                    ├── SqlAnalysisResult.java  # Result data structure
                    ├── SqlParserApp.java       # Example application
                    └── SqlParserCli.java       # CLI for reading SQL files
```

## License

This project is provided as-is for educational and development purposes.
