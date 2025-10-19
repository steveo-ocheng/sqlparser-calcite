package com.sqlparser;

import org.apache.calcite.sql.parser.SqlParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Window Function Tests")
class WindowFunctionTest {

    private SqlAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new SqlAnalyzer();
    }

    @Test
    @DisplayName("Should parse ROW_NUMBER with PARTITION BY")
    void testRowNumberPartition() throws SqlParseException {
        String sql = "SELECT employee_id, department, " +
                     "ROW_NUMBER() OVER (PARTITION BY department ORDER BY salary DESC) AS rank " +
                     "FROM employees";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(1, result.getTables().size());
        assertEquals(3, result.getSelectedColumns().size());
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("ROW_NUMBER()")));
    }

    @Test
    @DisplayName("Should parse RANK and DENSE_RANK functions")
    void testRankDenseRank() throws SqlParseException {
        String sql = "SELECT product_name, " +
                     "RANK() OVER (ORDER BY sales DESC) AS rank, " +
                     "DENSE_RANK() OVER (ORDER BY sales DESC) AS dense_rank " +
                     "FROM products";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(3, result.getSelectedColumns().size());
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("RANK()")));
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("DENSE_RANK()")));
    }

    @Test
    @DisplayName("Should parse PERCENT_RANK and CUME_DIST")
    void testPercentRankCumeDist() throws SqlParseException {
        String sql = "SELECT product_id, " +
                     "PERCENT_RANK() OVER (ORDER BY price) AS percentile, " +
                     "CUME_DIST() OVER (ORDER BY price) AS cumulative " +
                     "FROM products";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(3, result.getSelectedColumns().size());
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("PERCENT_RANK()")));
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("CUME_DIST()")));
    }

    @Test
    @DisplayName("Should parse running totals with ROWS frame")
    void testRunningTotals() throws SqlParseException {
        String sql = "SELECT order_date, amount, " +
                     "SUM(amount) OVER (ORDER BY order_date ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS running_total " +
                     "FROM orders";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(3, result.getSelectedColumns().size());
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("SUM(amount)")));
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("OVER")));
    }

    @Test
    @DisplayName("Should parse moving averages with window frame")
    void testMovingAverages() throws SqlParseException {
        String sql = "SELECT date, price, " +
                     "AVG(price) OVER (ORDER BY date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW) AS ma_7day " +
                     "FROM stock_prices";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(3, result.getSelectedColumns().size());
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("AVG(price)")));
    }

    @Test
    @DisplayName("Should parse LAG function")
    void testLagFunction() throws SqlParseException {
        String sql = "SELECT date, value, " +
                     "LAG(value, 1) OVER (ORDER BY date) AS previous_value " +
                     "FROM metrics";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(3, result.getSelectedColumns().size());
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("LAG(value")));
    }

    @Test
    @DisplayName("Should parse LEAD function")
    void testLeadFunction() throws SqlParseException {
        String sql = "SELECT date, value, " +
                     "LEAD(value, 1) OVER (ORDER BY date) AS next_value " +
                     "FROM metrics";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(3, result.getSelectedColumns().size());
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("LEAD(value")));
    }

    @Test
    @DisplayName("Should parse LAG with default value")
    void testLagWithDefault() throws SqlParseException {
        String sql = "SELECT id, amount, " +
                     "LAG(amount, 1, 0) OVER (ORDER BY id) AS prev_amount " +
                     "FROM transactions";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("LAG(amount")));
    }

    @Test
    @DisplayName("Should parse FIRST_VALUE function")
    void testFirstValue() throws SqlParseException {
        String sql = "SELECT session_id, page, " +
                     "FIRST_VALUE(page) OVER (PARTITION BY session_id ORDER BY timestamp) AS entry_page " +
                     "FROM page_views";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("FIRST_VALUE(page)")));
    }

    @Test
    @DisplayName("Should parse LAST_VALUE function")
    void testLastValue() throws SqlParseException {
        String sql = "SELECT session_id, page, " +
                     "LAST_VALUE(page) OVER (PARTITION BY session_id ORDER BY timestamp ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS exit_page " +
                     "FROM page_views";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("LAST_VALUE(page)")));
    }

    @Test
    @DisplayName("Should parse NTILE function")
    void testNtile() throws SqlParseException {
        String sql = "SELECT customer_id, revenue, " +
                     "NTILE(4) OVER (ORDER BY revenue DESC) AS quartile " +
                     "FROM customers";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(3, result.getSelectedColumns().size());
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("NTILE(4)")));
    }

    @Test
    @DisplayName("Should parse multiple NTILE with different buckets")
    void testMultipleNtile() throws SqlParseException {
        String sql = "SELECT id, value, " +
                     "NTILE(4) OVER (ORDER BY value) AS quartile, " +
                     "NTILE(10) OVER (ORDER BY value) AS decile, " +
                     "NTILE(100) OVER (ORDER BY value) AS percentile " +
                     "FROM data";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(5, result.getSelectedColumns().size());
        long ntileCount = result.getSelectedColumns().stream()
                .filter(col -> col.contains("NTILE")).count();
        assertEquals(3, ntileCount);
    }

    @Test
    @DisplayName("Should parse RANGE BETWEEN frame")
    void testRangeBetween() throws SqlParseException {
        String sql = "SELECT date, amount, " +
                     "SUM(amount) OVER (ORDER BY date RANGE BETWEEN INTERVAL '7 days' PRECEDING AND CURRENT ROW) AS sum_7days " +
                     "FROM orders";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("SUM(amount)")));
    }

    @Test
    @DisplayName("Should parse multiple window functions with different partitions")
    void testMultiplePartitions() throws SqlParseException {
        String sql = "SELECT region, store, sales, " +
                     "SUM(sales) OVER (PARTITION BY region) AS region_total, " +
                     "SUM(sales) OVER (PARTITION BY store) AS store_total, " +
                     "SUM(sales) OVER () AS grand_total " +
                     "FROM sales_data";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(6, result.getSelectedColumns().size());
        long sumCount = result.getSelectedColumns().stream()
                .filter(col -> col.contains("SUM(sales)")).count();
        assertEquals(3, sumCount);
    }

    @Test
    @DisplayName("Should parse named window with WINDOW clause")
    void testNamedWindow() throws SqlParseException {
        String sql = "SELECT emp_id, salary, " +
                     "ROW_NUMBER() OVER w AS rank, " +
                     "AVG(salary) OVER w AS avg_sal " +
                     "FROM employees " +
                     "WINDOW w AS (PARTITION BY department ORDER BY salary DESC)";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(4, result.getSelectedColumns().size());
    }

    @Test
    @DisplayName("Should parse window function with complex ORDER BY")
    void testComplexOrderBy() throws SqlParseException {
        String sql = "SELECT name, sales, returns, " +
                     "RANK() OVER (ORDER BY sales DESC, returns ASC) AS rank " +
                     "FROM products";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("RANK()")));
    }

    @Test
    @DisplayName("Should parse FILTER clause with window function")
    void testFilterClause() throws SqlParseException {
        String sql = "SELECT date, " +
                     "SUM(amount) FILTER (WHERE type = 'sale') OVER (ORDER BY date) AS sales_total " +
                     "FROM transactions";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("SUM(amount)")));
    }

    @Test
    @DisplayName("Should parse STDDEV window function")
    void testStddevWindow() throws SqlParseException {
        String sql = "SELECT date, price, " +
                     "STDDEV(price) OVER (ORDER BY date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW) AS volatility " +
                     "FROM prices";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("STDDEV(price)")));
    }

    @Test
    @DisplayName("Should parse window function in expression")
    void testWindowInExpression() throws SqlParseException {
        String sql = "SELECT date, value, " +
                     "value - LAG(value) OVER (ORDER BY date) AS change " +
                     "FROM metrics";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(3, result.getSelectedColumns().size());
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("value - LAG(value)")));
    }

    @Test
    @DisplayName("Should parse nested window functions in CASE")
    void testWindowInCase() throws SqlParseException {
        String sql = "SELECT id, value, " +
                     "CASE WHEN value > AVG(value) OVER () THEN 'Above' ELSE 'Below' END AS category " +
                     "FROM data";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(3, result.getSelectedColumns().size());
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("CASE")));
    }

    @Test
    @DisplayName("Should parse PERCENTILE_CONT window function")
    void testPercentileCont() throws SqlParseException {
        String sql = "SELECT category, price, " +
                     "PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY price) OVER (PARTITION BY category) AS median " +
                     "FROM products";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("PERCENTILE_CONT")));
    }

    @Test
    @DisplayName("Should parse PERCENTILE_DISC window function")
    void testPercentileDisc() throws SqlParseException {
        String sql = "SELECT category, price, " +
                     "PERCENTILE_DISC(0.5) WITHIN GROUP (ORDER BY price) OVER (PARTITION BY category) AS median " +
                     "FROM products";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("PERCENTILE_DISC")));
    }

    @Test
    @DisplayName("Should parse complex multi-window query")
    void testComplexMultiWindow() throws SqlParseException {
        String sql = "SELECT date, region, sales, " +
                     "SUM(sales) OVER (PARTITION BY region ORDER BY date) AS running_total, " +
                     "AVG(sales) OVER (PARTITION BY region ORDER BY date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW) AS ma_7, " +
                     "RANK() OVER (PARTITION BY region ORDER BY sales DESC) AS rank, " +
                     "LAG(sales, 1) OVER (PARTITION BY region ORDER BY date) AS prev_sales " +
                     "FROM daily_sales";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(7, result.getSelectedColumns().size());
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("SUM(sales)")));
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("AVG(sales)")));
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("RANK()")));
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("LAG(sales")));
    }

    @Test
    @DisplayName("Should parse window function with ROWS BETWEEN")
    void testRowsBetween() throws SqlParseException {
        String sql = "SELECT date, value, " +
                     "AVG(value) OVER (ORDER BY date ROWS BETWEEN 2 PRECEDING AND 2 FOLLOWING) AS centered_avg " +
                     "FROM metrics";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("AVG(value)")));
    }

    @Test
    @DisplayName("Should parse window function with empty OVER clause")
    void testEmptyOver() throws SqlParseException {
        String sql = "SELECT id, value, " +
                     "SUM(value) OVER () AS total, " +
                     "AVG(value) OVER () AS average " +
                     "FROM data";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(4, result.getSelectedColumns().size());
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("SUM(value)")));
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("AVG(value)")));
    }

    @Test
    @DisplayName("Should parse COUNT window function")
    void testCountWindow() throws SqlParseException {
        String sql = "SELECT date, event, " +
                     "COUNT(*) OVER (PARTITION BY user_id ORDER BY date) AS event_count " +
                     "FROM events";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("COUNT(*)")));
    }

    @Test
    @DisplayName("Should parse MAX and MIN window functions")
    void testMaxMinWindow() throws SqlParseException {
        String sql = "SELECT date, price, " +
                     "MAX(price) OVER (ORDER BY date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW) AS max_30d, " +
                     "MIN(price) OVER (ORDER BY date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW) AS min_30d " +
                     "FROM prices";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("MAX(price)")));
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("MIN(price)")));
    }

    @Test
    @DisplayName("Should handle window functions in WHERE clause subquery")
    void testWindowInSubquery() throws SqlParseException {
        String sql = "SELECT name, salary FROM employees " +
                     "WHERE salary > (SELECT AVG(salary) OVER () FROM employees LIMIT 1)";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(2, result.getSelectedColumns().size());
        assertEquals(1, result.getWhereConditions().size());
    }

    @Test
    @DisplayName("Should generate description for query with window functions")
    void testDescriptionWithWindows() throws SqlParseException {
        String sql = "SELECT employee_id, salary, " +
                     "RANK() OVER (PARTITION BY department ORDER BY salary DESC) AS rank " +
                     "FROM employees " +
                     "ORDER BY department, rank";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertNotNull(result.getDescription());
        assertTrue(result.getDescription().contains("retrieves 3 columns"));
        assertTrue(result.getDescription().contains("sorted"));
    }
}
