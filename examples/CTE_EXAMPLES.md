# CTE (Common Table Expression) Examples

This directory contains advanced SQL examples using CTEs to demonstrate the parser's capability to handle complex queries.

## Examples Overview

### 1. complex-cte-query.sql
**Purpose:** Customer and product analytics with multiple CTEs

**Features demonstrated:**
- Multiple CTEs (4 CTEs: monthly_sales, customer_ltv, product_metrics, ranked_customers)
- Window functions (ROW_NUMBER, PERCENT_RANK)
- Aggregate functions across CTEs
- Complex JOIN operations between CTEs
- Subquery in WHERE clause
- CASE expressions for customer tiering
- Multiple ORDER BY columns with mixed ASC/DESC

**Business scenario:**
Analyzes customer lifetime value, product performance, and creates customer segments with rankings.

**Sample output:**
```
This query retrieves 11 columns (rc.customer_name, rc.customer_segment,
rc.lifetime_value, and 8 more) from 3 tables (ranked_customers, monthly_sales,
product_metrics), joining tables using 2 join conditions. The results are filtered
where rc.segment_rank <= 5 AND rc.percentile_rank <= 0.1 AND
pm.total_product_revenue > (SELECT AVG(total_product_revenue) * 1.5
FROM product_metrics). Results are sorted by rc.customer_segment ASC,
rc.lifetime_value DESC, pm.total_product_revenue DESC, limited to 50 rows.
```

### 2. recursive-cte-query.sql
**Purpose:** Employee hierarchy analysis with recursive CTE

**Features demonstrated:**
- RECURSIVE CTE for organizational hierarchy
- String concatenation in recursion (reporting chain)
- Depth limiting in recursive queries
- Multiple non-recursive CTEs combined with recursive CTE
- Complex WHERE clause with correlated subquery
- Window functions (RANK, DENSE_RANK)
- EXTRACT and date functions
- Nested CASE expressions

**Business scenario:**
Builds an employee organizational chart recursively, then analyzes department
statistics and identifies high performers with tenure categories.

**Key metrics:**
- 14 columns selected
- 2 main tables in final query (high_performers, department_stats)
- 100 row limit
- Multiple complex calculations and aggregations

### 3. multiple-cte-analytics.sql
**Purpose:** User engagement and segmentation analytics

**Features demonstrated:**
- 5 CTEs working together (daily_activity, engagement_scores, user_revenue, user_segments, segment_performance)
- Advanced segmentation logic with multiple CASE expressions
- Window aggregate functions (PERCENTILE_CONT, WITHIN GROUP)
- LEFT JOIN with COALESCE for handling nulls
- Complex WHERE clause with OR conditions and IN operator
- Sophisticated ORDER BY with CASE for custom sorting
- Date interval calculations

**Business scenario:**
Tracks user activity, calculates engagement scores, attributes revenue, segments
users into categories (Champion, Loyal, etc.), and provides benchmarking against
segment averages.

**Key metrics:**
- 18 columns in final output
- 200 row limit
- Multi-dimensional user analysis combining engagement, revenue, and tenure

## Running the Examples

### Basic execution:
```bash
# Run with default output
java -jar build/libs/sqlparser-all-1.0.0.jar examples/complex-cte-query.sql

# Get only the description
java -jar build/libs/sqlparser-all-1.0.0.jar examples/recursive-cte-query.sql --quiet

# See the full SQL + analysis
java -jar build/libs/sqlparser-all-1.0.0.jar examples/multiple-cte-analytics.sql --verbose
```

### Using the convenience script:
```bash
./sqlparser.sh examples/complex-cte-query.sql
./sqlparser.sh examples/recursive-cte-query.sql --quiet
./sqlparser.sh examples/multiple-cte-analytics.sql --verbose
```

## What the Parser Extracts

For all CTE queries, the parser successfully extracts:

1. **Tables**: Identifies the final query tables (CTEs are treated as tables)
2. **Columns**: Maps columns to their table/CTE aliases
3. **Selected Columns**: All columns in the SELECT clause, including:
   - Simple column references
   - Expressions and calculations
   - Window functions
   - CASE expressions
   - Aggregate functions with aliases

4. **WHERE Conditions**: Complete WHERE clause including:
   - Simple comparisons
   - Complex AND/OR logic
   - IN operators
   - Subqueries

5. **JOIN Conditions**: All JOIN types and their ON clauses
6. **GROUP BY**: Grouping columns
7. **HAVING**: Aggregate filtering conditions
8. **ORDER BY**: Sorting columns with direction (ASC/DESC)
9. **LIMIT**: Row limits
10. **Description**: Natural language summary of the query

## Notes on CTE Handling

The JSqlParser library (used by this project) treats CTEs as virtual tables in the
final SELECT statement. This means:

- The parser focuses on analyzing the final SELECT that uses the CTEs
- CTE definitions are recognized but the final query analysis shows the CTE names as tables
- This is semantically correct as CTEs act as temporary named result sets

For example, in `complex-cte-query.sql`:
- The parser identifies `ranked_customers`, `monthly_sales`, and `product_metrics` as the tables
- These are actually CTEs defined earlier in the WITH clause
- The analysis correctly shows how these CTEs are joined and queried in the final SELECT
