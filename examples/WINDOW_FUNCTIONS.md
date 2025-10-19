# Window Function Examples

This directory contains 20 comprehensive examples demonstrating advanced SQL window functions for various analytics use cases.

## Overview

Window functions perform calculations across a set of table rows that are related to the current row. Unlike aggregate functions with GROUP BY, window functions retain individual rows while providing aggregated insights.

## Example Categories

### Ranking Functions (Examples 1-2, 7)

**window-01-row-number-partitioning.sql** - ROW_NUMBER with partitioning
- Demonstrates ROW_NUMBER() with multiple partition strategies
- Ranks employees by salary within departments
- Tracks overall salary rankings and seniority rankings
- **Use case**: Employee performance dashboards, compensation analysis

**window-02-rank-dense-rank.sql** - RANK vs DENSE_RANK comparison
- Compares RANK() (with gaps) vs DENSE_RANK() (no gaps)
- Includes PERCENT_RANK and CUME_DIST for distribution analysis
- **Use case**: Product sales rankings, handling ties in competitions

**window-07-ntile-quartiles.sql** - NTILE for bucketing
- Divides customers into quartiles, deciles, and percentiles
- Segments data within customer segments
- **Use case**: Customer segmentation, A/B testing group assignment

### Aggregate Windows (Examples 3-4, 8-9)

**window-03-running-totals.sql** - Cumulative aggregates
- Running totals, averages, counts with ROWS BETWEEN
- Tracks max/min values to date
- **Use case**: Financial reporting, cumulative metrics tracking

**window-04-moving-averages.sql** - Time-based moving windows
- 7, 30, 90, and 200-day moving averages
- Rolling volume and volatility calculations
- **Use case**: Stock price analysis, trend identification

**window-08-range-between.sql** - Value-based windows (RANGE)
- RANGE BETWEEN for date/value-based windows instead of row counts
- Trailing 7, 30, 90-day calculations
- **Use case**: Time-based aggregations, calendar-aware analytics

**window-09-multiple-partitions.sql** - Complex multi-partition analysis
- Multiple window functions with different partitions in single query
- Regional, store, category, and combined analytics
- **Use case**: Multi-dimensional business intelligence reporting

### Navigation Functions (Examples 5-6, 11)

**window-05-lead-lag-functions.sql** - LEAD and LAG
- Access previous and next row values
- Calculate period-over-period changes
- Measure time gaps between events
- **Use case**: Sequential analysis, change detection

**window-06-first-last-value.sql** - Session boundaries
- FIRST_VALUE and LAST_VALUE with proper frame specifications
- Identify entry/exit pages in user sessions
- Calculate total session time
- **Use case**: Web analytics, session analysis

**window-11-gap-island-detection.sql** - Streak and gap detection
- Identify consecutive login streaks
- Detect gaps in activity
- **Use case**: User engagement tracking, data quality monitoring

### Distribution Functions (Examples 12, 14)

**window-12-percentile-calculations.sql** - Advanced percentiles
- PERCENTILE_CONT and PERCENTILE_DISC
- Calculate P25, median, P75, P90
- Price tier classification based on percentiles
- **Use case**: Pricing strategy, outlier detection

**window-14-year-over-year-comparison.sql** - Period comparisons
- YoY, QoQ, MoM growth calculations
- Trailing 12-month totals
- **Use case**: Financial reporting, trend analysis

### Named Windows (Example 10)

**window-10-named-windows.sql** - WINDOW clause
- Define reusable window specifications with WINDOW clause
- Multiple functions sharing same window definition
- **Use case**: Cleaner SQL, performance optimization

### Conditional Aggregates (Example 13)

**window-13-conditional-aggregates.sql** - FILTER and CASE
- Filtered window aggregates (FILTER WHERE)
- Conditional sums using CASE expressions
- Net revenue calculations
- **Use case**: Transaction analysis, revenue reporting

### Advanced Analytics (Examples 15-20)

**window-15-top-n-per-group.sql** - Top N per category
- Find top performers in each group
- Percentage of category sales
- Performance tier classification
- **Use case**: Product rankings, sales leaderboards

**window-16-session-analytics.sql** - User session metrics
- Event sequencing and timing
- Session boundaries and duration
- Click-through analysis
- **Use case**: Web analytics, user behavior analysis

**window-17-cohort-analysis.sql** - Cohort retention
- User cohorts by registration month
- Retention rate calculations
- Cumulative cohort metrics
- **Use case**: SaaS metrics, customer retention analysis

**window-18-inventory-running-balance.sql** - Inventory tracking
- Running inventory balance with stock movements
- Min/max stock levels over time periods
- Restock frequency tracking
- **Use case**: Supply chain management, inventory optimization

**window-19-funnel-conversion-analysis.sql** - Conversion funnels
- Multi-step funnel progression
- Drop-off detection at each step
- Time-to-conversion metrics
- **Use case**: E-commerce optimization, conversion rate optimization

**window-20-time-series-forecasting.sql** - Time series analysis
- Trend detection (uptrend/downtrend/stable)
- Anomaly detection using z-scores
- Volatility and standard deviation
- Seasonality comparison (YoY)
- **Use case**: Demand forecasting, anomaly detection

## Window Function Reference

### Ranking Functions
- `ROW_NUMBER()` - Unique sequential number for each row
- `RANK()` - Rank with gaps for ties
- `DENSE_RANK()` - Rank without gaps
- `PERCENT_RANK()` - Relative rank (0 to 1)
- `CUME_DIST()` - Cumulative distribution (0 to 1)
- `NTILE(n)` - Divide rows into n equal buckets

### Aggregate Functions (as Windows)
- `SUM()` - Running/moving sum
- `AVG()` - Running/moving average
- `COUNT()` - Running/moving count
- `MAX()` / `MIN()` - Running/moving extremes
- `STDDEV()` - Standard deviation

### Navigation Functions
- `LAG(col, offset, default)` - Value from previous row
- `LEAD(col, offset, default)` - Value from next row
- `FIRST_VALUE(col)` - First value in window
- `LAST_VALUE(col)` - Last value in window

### Distribution Functions
- `PERCENTILE_CONT(pct)` - Continuous percentile (interpolated)
- `PERCENTILE_DISC(pct)` - Discrete percentile (actual value)

## Window Frame Specifications

### ROWS vs RANGE
- `ROWS` - Physical row offset (count-based)
- `RANGE` - Logical value offset (value-based, e.g., dates)

### Frame Bounds
```sql
ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW  -- All rows up to current
ROWS BETWEEN 7 PRECEDING AND CURRENT ROW           -- Last 7 rows
ROWS BETWEEN 3 PRECEDING AND 3 FOLLOWING           -- Centered 7-row window
RANGE BETWEEN INTERVAL '30 days' PRECEDING AND CURRENT ROW  -- Last 30 days
```

### Default Frames
- With ORDER BY: `RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW`
- Without ORDER BY: All rows in partition

## Common Patterns

### Running Total
```sql
SUM(amount) OVER (PARTITION BY customer_id ORDER BY date
                  ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)
```

### Moving Average (7-day)
```sql
AVG(value) OVER (ORDER BY date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW)
```

### Previous Value
```sql
LAG(value, 1, 0) OVER (ORDER BY date)
```

### Rank Within Group
```sql
RANK() OVER (PARTITION BY category ORDER BY sales DESC)
```

### Percentile
```sql
NTILE(4) OVER (ORDER BY revenue DESC)  -- Quartiles
PERCENT_RANK() OVER (ORDER BY score)   -- Exact percentile
```

## Running the Examples

### Individual Examples
```bash
# Basic output
java -jar build/libs/sqlparser-all-1.0.0.jar examples/window-01-row-number-partitioning.sql

# Quiet mode (description only)
java -jar build/libs/sqlparser-all-1.0.0.jar examples/window-04-moving-averages.sql --quiet

# Verbose mode (SQL + analysis)
java -jar build/libs/sqlparser-all-1.0.0.jar examples/window-15-top-n-per-group.sql --verbose
```

### Test All Window Examples
```bash
for file in examples/window-*.sql; do
    echo "Testing: $file"
    java -jar build/libs/sqlparser-all-1.0.0.jar "$file" --quiet
done
```

## Unit Tests

Window function parsing is extensively tested in `WindowFunctionTest.java` with 30 test cases covering:
- All ranking functions (ROW_NUMBER, RANK, DENSE_RANK, NTILE, PERCENT_RANK)
- All navigation functions (LAG, LEAD, FIRST_VALUE, LAST_VALUE)
- Window aggregates (SUM, AVG, COUNT, MAX, MIN, STDDEV)
- Distribution functions (PERCENTILE_CONT, PERCENTILE_DISC)
- Frame specifications (ROWS BETWEEN, RANGE BETWEEN)
- Named windows (WINDOW clause)
- Conditional aggregates (FILTER clause)
- Complex multi-window queries

Run tests:
```bash
./gradlew test --tests WindowFunctionTest
```

## Performance Considerations

1. **Partitioning**: Use PARTITION BY to limit window scope
2. **Ordering**: Window ORDER BY can be expensive on large datasets
3. **Frame specification**: ROWS is typically faster than RANGE
4. **Multiple windows**: Consider if WINDOW clause can improve performance
5. **Indexes**: Create indexes on partition and order columns

## Parser Capabilities

The SQL Parser successfully extracts from window function queries:
- ✓ Window function names and arguments
- ✓ PARTITION BY columns
- ✓ ORDER BY clauses within windows
- ✓ Frame specifications (ROWS/RANGE BETWEEN)
- ✓ Named windows (WINDOW clause)
- ✓ Multiple window functions in single query
- ✓ Window functions in expressions and CASE statements
- ✓ FILTER clauses with window functions
- ✓ All aggregate, ranking, and navigation functions

## Real-World Applications

1. **Financial Analysis**: Moving averages, running totals, YoY comparisons
2. **Sales Analytics**: Product rankings, top N per category, growth trends
3. **Web Analytics**: Session analysis, funnel conversion, user journeys
4. **Customer Analytics**: Cohort retention, RFM analysis, customer lifetime value
5. **Inventory Management**: Stock levels, reorder points, turnover rates
6. **Time Series**: Trend detection, anomaly detection, forecasting
7. **HR Analytics**: Salary benchmarking, employee rankings, tenure analysis

## Further Reading

- PostgreSQL Window Functions Documentation
- SQL Standard Window Functions (ISO/IEC 9075-2)
- "SQL Window Functions" by Markus Winand
- Database-specific optimizations for window functions
