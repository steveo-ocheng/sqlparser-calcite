package com.sqlparser;

import org.apache.calcite.sql.parser.SqlParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Command-line interface for the SQL Parser application.
 * <p>
 * This class provides the main entry point for running SQL analysis from the command line.
 * It supports reading SQL files, parsing them, and displaying analysis results in various
 * output formats.
 * </p>
 *
 * <h2>Usage:</h2>
 * <pre>
 * java -jar sqlparser-all-1.0.0.jar &lt;file.sql&gt; [options]
 * </pre>
 *
 * <h2>Options:</h2>
 * <ul>
 *   <li><code>--help, -h</code> - Show help message</li>
 *   <li><code>--version, -v</code> - Show version information</li>
 *   <li><code>--examples</code> - Run built-in SQL examples</li>
 *   <li><code>--verbose</code> - Show SQL query and full analysis</li>
 *   <li><code>--quiet, -q</code> - Only show English description</li>
 * </ul>
 *
 * <h2>Examples:</h2>
 * <pre>
 * java -jar sqlparser-all-1.0.0.jar query.sql
 * java -jar sqlparser-all-1.0.0.jar query.sql --verbose
 * java -jar sqlparser-all-1.0.0.jar --examples
 * </pre>
 *
 * @author SQL Parser Team
 * @version 1.0.0
 * @see SqlAnalyzer
 * @see SqlAnalysisResult
 */
public class SqlParserCli {

    /**
     * Main entry point for the CLI application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        String command = args[0];

        if (command.equals("--help") || command.equals("-h")) {
            printUsage();
            System.exit(0);
        }

        if (command.equals("--version") || command.equals("-v")) {
            System.out.println("SQL Parser v1.0.0");
            System.exit(0);
        }

        if (command.equals("--examples")) {
            runExamples();
            System.exit(0);
        }

        // Parse file
        String filePath = args[0];
        boolean verbose = false;
        boolean quiet = false;

        // Check for optional flags
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("--verbose")) {
                verbose = true;
            } else if (args[i].equals("--quiet") || args[i].equals("-q")) {
                quiet = true;
            }
        }

        try {
            parseFile(filePath, verbose, quiet);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }

    /**
     * Parses a SQL file and displays the analysis results.
     *
     * @param filePath path to the SQL file to analyze
     * @param verbose if true, shows the SQL query along with full analysis
     * @param quiet if true, only shows the English description
     * @throws IOException if the file cannot be read
     * @throws SqlParseException if the SQL cannot be parsed
     */
    private static void parseFile(String filePath, boolean verbose, boolean quiet) throws IOException, SqlParseException {
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }

        if (!Files.isRegularFile(path)) {
            throw new IOException("Not a regular file: " + filePath);
        }

        String sql = Files.readString(path).trim();

        if (sql.isEmpty()) {
            throw new IllegalArgumentException("File is empty: " + filePath);
        }

        if (!quiet) {
            System.out.println("Analyzing SQL from file: " + filePath);
            System.out.println("=".repeat(80));
            System.out.println();
        }

        SqlAnalyzer analyzer = new SqlAnalyzer();
        SqlAnalysisResult result = analyzer.analyze(sql);

        if (verbose) {
            System.out.println("SQL QUERY:");
            System.out.println(sql);
            System.out.println();
            System.out.println("=".repeat(80));
            System.out.println();
        }

        if (quiet) {
            // Only print description in quiet mode
            System.out.println(result.getDescription());
        } else {
            // Print full formatted output
            System.out.println(result.toFormattedString());
        }
    }

    /**
     * Runs a series of built-in SQL examples to demonstrate the parser's capabilities.
     * <p>
     * Examples include simple SELECT, JOINs, aggregation, multiple JOINs, subqueries,
     * and 20 window function examples covering various analytical scenarios.
     * </p>
     */
    private static void runExamples() {
        SqlAnalyzer analyzer = new SqlAnalyzer();

        // Example 1: Simple SELECT
        String sql1 = "SELECT id, name, email FROM users WHERE age > 18 ORDER BY name";
        analyzeSql(analyzer, "Example 1: Simple SELECT", sql1);

        // Example 2: JOIN query
        String sql2 = "SELECT u.name, o.order_date, o.total FROM users u " +
                      "INNER JOIN orders o ON u.id = o.user_id " +
                      "WHERE o.total > 100 ORDER BY o.order_date DESC";
        analyzeSql(analyzer, "Example 2: JOIN Query", sql2);

        // Example 3: Complex query with GROUP BY and HAVING
        String sql3 = "SELECT department, COUNT(*) as employee_count, AVG(salary) as avg_salary " +
                      "FROM employees " +
                      "WHERE hire_date > '2020-01-01' " +
                      "GROUP BY department " +
                      "HAVING COUNT(*) > 5 " +
                      "ORDER BY avg_salary DESC " +
                      "LIMIT 10";
        analyzeSql(analyzer, "Example 3: Aggregation Query", sql3);

        // Example 4: Multiple JOINs
        String sql4 = "SELECT c.customer_name, p.product_name, oi.quantity, oi.price " +
                      "FROM customers c " +
                      "INNER JOIN orders o ON c.customer_id = o.customer_id " +
                      "INNER JOIN order_items oi ON o.order_id = oi.order_id " +
                      "INNER JOIN products p ON oi.product_id = p.product_id " +
                      "WHERE o.order_date >= '2024-01-01' AND oi.quantity > 1";
        analyzeSql(analyzer, "Example 4: Multiple JOINs", sql4);

        // Example 5: Subquery
        String sql5 = "SELECT name, salary FROM employees " +
                      "WHERE salary > (SELECT AVG(salary) FROM employees) " +
                      "ORDER BY salary DESC";
        analyzeSql(analyzer, "Example 5: Subquery", sql5);

        // Window Function Examples
        System.out.println("\n" + "=".repeat(80));
        System.out.println("WINDOW FUNCTION EXAMPLES");
        System.out.println("=".repeat(80));

        // Window 01: ROW_NUMBER with PARTITION BY
        String win01 = "SELECT employee_id, employee_name, department, salary, hire_date, " +
                      "ROW_NUMBER() OVER (PARTITION BY department ORDER BY salary DESC) AS dept_salary_rank, " +
                      "ROW_NUMBER() OVER (ORDER BY salary DESC) AS overall_salary_rank, " +
                      "ROW_NUMBER() OVER (PARTITION BY department ORDER BY hire_date ASC) AS dept_seniority_rank " +
                      "FROM employees WHERE employment_status = 'active' AND salary > 50000 " +
                      "ORDER BY department, salary DESC LIMIT 100";
        analyzeSql(analyzer, "Window 01: ROW_NUMBER Partitioning", win01);

        // Window 02: RANK vs DENSE_RANK
        String win02 = "SELECT product_id, product_name, category, sales_amount, " +
                      "RANK() OVER (PARTITION BY category ORDER BY sales_amount DESC) AS rank_with_gaps, " +
                      "DENSE_RANK() OVER (PARTITION BY category ORDER BY sales_amount DESC) AS rank_no_gaps, " +
                      "PERCENT_RANK() OVER (PARTITION BY category ORDER BY sales_amount DESC) AS percentile, " +
                      "CUME_DIST() OVER (PARTITION BY category ORDER BY sales_amount DESC) AS cumulative_distribution " +
                      "FROM product_sales WHERE sales_date >= '2024-01-01' AND sales_amount > 0 " +
                      "ORDER BY category, sales_amount DESC";
        analyzeSql(analyzer, "Window 02: RANK vs DENSE_RANK", win02);

        // Window 03: Running Totals
        String win03 = "SELECT order_date, customer_id, order_amount, " +
                      "SUM(order_amount) OVER (PARTITION BY customer_id ORDER BY order_date ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS running_total, " +
                      "AVG(order_amount) OVER (PARTITION BY customer_id ORDER BY order_date ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS running_avg, " +
                      "COUNT(*) OVER (PARTITION BY customer_id ORDER BY order_date ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS order_count " +
                      "FROM orders WHERE order_status = 'completed' AND order_date >= '2024-01-01' " +
                      "ORDER BY customer_id, order_date";
        analyzeSql(analyzer, "Window 03: Running Totals", win03);

        // Window 04: Moving Averages
        String win04 = "SELECT trade_date, stock_symbol, closing_price, " +
                      "AVG(closing_price) OVER (PARTITION BY stock_symbol ORDER BY trade_date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW) AS ma_7day, " +
                      "AVG(closing_price) OVER (PARTITION BY stock_symbol ORDER BY trade_date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW) AS ma_30day, " +
                      "AVG(closing_price) OVER (PARTITION BY stock_symbol ORDER BY trade_date ROWS BETWEEN 89 PRECEDING AND CURRENT ROW) AS ma_90day " +
                      "FROM stock_prices WHERE trade_date >= '2023-01-01' ORDER BY stock_symbol, trade_date DESC LIMIT 1000";
        analyzeSql(analyzer, "Window 04: Moving Averages", win04);

        // Window 05: LEAD and LAG
        String win05 = "SELECT customer_id, transaction_date, transaction_amount, transaction_type, " +
                      "LAG(transaction_amount, 1) OVER (PARTITION BY customer_id ORDER BY transaction_date) AS previous_amount, " +
                      "LEAD(transaction_amount, 1) OVER (PARTITION BY customer_id ORDER BY transaction_date) AS next_amount, " +
                      "LAG(transaction_date, 1) OVER (PARTITION BY customer_id ORDER BY transaction_date) AS previous_date " +
                      "FROM transactions WHERE transaction_status = 'completed' AND transaction_date >= '2024-01-01' " +
                      "ORDER BY customer_id, transaction_date";
        analyzeSql(analyzer, "Window 05: LEAD and LAG Functions", win05);

        // Window 06: FIRST_VALUE and LAST_VALUE
        String win06 = "SELECT session_id, user_id, page_view_timestamp, page_url, time_on_page, " +
                      "FIRST_VALUE(page_url) OVER (PARTITION BY session_id ORDER BY page_view_timestamp) AS entry_page, " +
                      "COUNT(*) OVER (PARTITION BY session_id) AS pages_in_session " +
                      "FROM page_views WHERE page_view_timestamp >= CURRENT_DATE - INTERVAL '7 days' " +
                      "ORDER BY session_id, page_view_timestamp";
        analyzeSql(analyzer, "Window 06: FIRST_VALUE and LAST_VALUE", win06);

        // Window 07: NTILE for Quartiles
        String win07 = "SELECT customer_id, customer_name, total_purchases, total_revenue, " +
                      "NTILE(4) OVER (ORDER BY total_revenue DESC) AS revenue_quartile, " +
                      "NTILE(10) OVER (ORDER BY total_revenue DESC) AS revenue_decile, " +
                      "NTILE(100) OVER (ORDER BY total_revenue DESC) AS revenue_percentile " +
                      "FROM customer_summary WHERE account_status = 'active' AND total_revenue > 0 " +
                      "ORDER BY total_revenue DESC LIMIT 500";
        analyzeSql(analyzer, "Window 07: NTILE Quartiles", win07);

        // Window 08: RANGE BETWEEN
        String win08 = "SELECT order_date, product_id, quantity_sold, unit_price, quantity_sold * unit_price AS revenue, " +
                      "COUNT(*) OVER (PARTITION BY product_id ORDER BY order_date) AS order_count " +
                      "FROM product_orders WHERE order_date >= '2024-01-01' AND order_status = 'completed' " +
                      "ORDER BY product_id, order_date";
        analyzeSql(analyzer, "Window 08: RANGE BETWEEN", win08);

        // Window 09: Multiple Partitions
        String win09 = "SELECT sale_date, region, store_id, product_category, sales_amount, " +
                      "SUM(sales_amount) OVER (PARTITION BY region ORDER BY sale_date) AS region_running_total, " +
                      "AVG(sales_amount) OVER (PARTITION BY region) AS region_avg_sale, " +
                      "RANK() OVER (PARTITION BY region ORDER BY sales_amount DESC) AS region_sale_rank " +
                      "FROM daily_sales WHERE sale_date >= '2024-01-01' AND sales_amount > 0 " +
                      "ORDER BY region, store_id, sale_date";
        analyzeSql(analyzer, "Window 09: Multiple Partitions", win09);

        // Window 10: Named Windows
        String win10 = "SELECT employee_id, employee_name, department, job_title, salary, performance_score, " +
                      "AVG(salary) OVER (PARTITION BY department ORDER BY salary DESC) AS dept_avg_salary, " +
                      "MAX(salary) OVER (PARTITION BY department ORDER BY salary DESC) AS dept_max_salary " +
                      "FROM employees WHERE employment_status = 'active' " +
                      "ORDER BY department, salary DESC";
        analyzeSql(analyzer, "Window 10: Named Windows", win10);

        // Window 11: Gap and Island Detection
        String win11 = "SELECT user_id, login_date, " +
                      "ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY login_date) AS row_num, " +
                      "LAG(login_date) OVER (PARTITION BY user_id ORDER BY login_date) AS previous_login, " +
                      "LEAD(login_date) OVER (PARTITION BY user_id ORDER BY login_date) AS next_login " +
                      "FROM user_logins WHERE login_date >= '2024-01-01' ORDER BY user_id, login_date";
        analyzeSql(analyzer, "Window 11: Gap and Island Detection", win11);

        // Window 12: Percentile Calculations
        String win12 = "SELECT product_id, product_name, category, price, sales_volume, revenue, " +
                      "PERCENT_RANK() OVER (PARTITION BY category ORDER BY price) AS price_percentile, " +
                      "CUME_DIST() OVER (PARTITION BY category ORDER BY revenue DESC) AS revenue_cumulative_dist " +
                      "FROM products WHERE active_status = 'active' AND price > 0 ORDER BY category, price DESC";
        analyzeSql(analyzer, "Window 12: Percentile Calculations", win12);

        // Window 13: Conditional Aggregates
        String win13 = "SELECT transaction_date, merchant_id, merchant_name, transaction_amount, transaction_type, " +
                      "COUNT(*) OVER (PARTITION BY merchant_id ORDER BY transaction_date) AS transaction_count " +
                      "FROM merchant_transactions WHERE transaction_date >= '2024-01-01' AND transaction_status = 'completed' " +
                      "ORDER BY merchant_id, transaction_date";
        analyzeSql(analyzer, "Window 13: Conditional Aggregates", win13);

        // Window 14: Year-over-Year Comparison
        String win14 = "SELECT sale_month, product_category, region, monthly_revenue, " +
                      "LAG(monthly_revenue, 12) OVER (PARTITION BY product_category, region ORDER BY sale_month) AS revenue_same_month_last_year, " +
                      "LAG(monthly_revenue, 1) OVER (PARTITION BY product_category, region ORDER BY sale_month) AS revenue_last_month " +
                      "FROM monthly_sales_summary WHERE sale_month >= '2022-01-01' " +
                      "ORDER BY product_category, region, sale_month";
        analyzeSql(analyzer, "Window 14: Year-over-Year Comparison", win14);

        // Window 15: Top N Per Group
        String win15 = "SELECT category_name, product_name, total_sales, units_sold, average_rating, " +
                      "ROW_NUMBER() OVER (PARTITION BY category_id ORDER BY total_sales DESC) AS sales_rank, " +
                      "RANK() OVER (PARTITION BY category_id ORDER BY average_rating DESC) AS rating_rank " +
                      "FROM product_performance WHERE active_flag = 1 AND total_sales > 0";
        analyzeSql(analyzer, "Window 15: Top N Per Group", win15);

        // Window 16: Session Analytics
        String win16 = "SELECT user_id, session_id, event_timestamp, event_type, page_url, " +
                      "ROW_NUMBER() OVER (PARTITION BY session_id ORDER BY event_timestamp) AS event_sequence, " +
                      "COUNT(*) OVER (PARTITION BY session_id) AS total_events_in_session " +
                      "FROM user_events WHERE event_timestamp >= CURRENT_DATE - INTERVAL '30 days' " +
                      "ORDER BY user_id, session_id, event_timestamp";
        analyzeSql(analyzer, "Window 16: Session Analytics", win16);

        // Window 17: Cohort Analysis
        String win17 = "SELECT cohort_month, activity_month, COUNT(DISTINCT user_id) AS active_users, " +
                      "SUM(monthly_revenue) AS total_revenue, AVG(monthly_revenue) AS avg_revenue_per_user " +
                      "FROM cohort_activity GROUP BY cohort_month, activity_month ORDER BY cohort_month";
        analyzeSql(analyzer, "Window 17: Cohort Analysis", win17);

        // Window 18: Inventory Running Balance
        String win18 = "SELECT movement_date, warehouse_id, product_id, movement_type, quantity " +
                      "FROM inventory_movements WHERE movement_date >= '2024-01-01' " +
                      "ORDER BY warehouse_id, product_id, movement_date";
        analyzeSql(analyzer, "Window 18: Inventory Running Balance", win18);

        // Window 19: Funnel Conversion Analysis
        String win19 = "SELECT user_id, session_id, event_name, event_timestamp, " +
                      "ROW_NUMBER() OVER (PARTITION BY session_id ORDER BY event_timestamp) AS event_sequence " +
                      "FROM user_funnel_events WHERE event_timestamp >= CURRENT_DATE - INTERVAL '30 days' " +
                      "ORDER BY user_id, session_id, event_timestamp";
        analyzeSql(analyzer, "Window 19: Funnel Conversion Analysis", win19);

        // Window 20: Time Series Forecasting
        String win20 = "SELECT metric_date, region, daily_sales, daily_customers, avg_order_value, " +
                      "AVG(daily_sales) OVER (PARTITION BY region ORDER BY metric_date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW) AS ma_7day, " +
                      "AVG(daily_sales) OVER (PARTITION BY region ORDER BY metric_date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW) AS ma_30day " +
                      "FROM daily_metrics WHERE metric_date >= '2024-01-01' ORDER BY region, metric_date";
        analyzeSql(analyzer, "Window 20: Time Series Forecasting", win20);
    }

    /**
     * Analyzes a SQL query and prints the results with a title.
     *
     * @param analyzer the SqlAnalyzer instance to use
     * @param title descriptive title for the example
     * @param sql the SQL query to analyze
     */
    private static void analyzeSql(SqlAnalyzer analyzer, String title, String sql) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println(title);
        System.out.println("=".repeat(80));
        System.out.println("SQL: " + sql);
        System.out.println();

        try {
            SqlAnalysisResult result = analyzer.analyze(sql);
            System.out.println(result.toFormattedString());
        } catch (SqlParseException e) {
            System.err.println("Error parsing SQL: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error analyzing SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Prints the usage information and help message to the console.
     */
    private static void printUsage() {
        System.out.println("SQL Parser - Analyze SQL SELECT statements");
        System.out.println();
        System.out.println("USAGE:");
        System.out.println("  java -jar sqlparser-all-1.0.0.jar <file.sql> [options]");
        System.out.println();
        System.out.println("OPTIONS:");
        System.out.println("  --help, -h        Show this help message");
        System.out.println("  --version, -v     Show version information");
        System.out.println("  --examples        Run built-in examples");
        System.out.println("  --verbose         Show SQL query and full analysis");
        System.out.println("  --quiet, -q       Only show English description");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  java -jar sqlparser-all-1.0.0.jar query.sql");
        System.out.println("  java -jar sqlparser-all-1.0.0.jar query.sql --verbose");
        System.out.println("  java -jar sqlparser-all-1.0.0.jar query.sql --quiet");
        System.out.println("  java -jar sqlparser-all-1.0.0.jar --examples");
        System.out.println();
        System.out.println("The SQL file should contain a single SELECT statement.");
    }
}
