package com.sqlparser;

import org.apache.calcite.sql.parser.SqlParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SqlAnalyzer Tests")
class SqlAnalyzerTest {

    private SqlAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new SqlAnalyzer();
    }

    @Test
    @DisplayName("Should parse simple SELECT with single column")
    void testSimpleSelectSingleColumn() throws SqlParseException {
        String sql = "SELECT name FROM users";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(1, result.getTables().size());
        assertTrue(result.getTables().get(0).contains("users"));
        assertEquals(1, result.getSelectedColumns().size());
        assertEquals("name", result.getSelectedColumns().get(0));
        assertTrue(result.getWhereConditions().isEmpty());
        assertNotNull(result.getDescription());
    }

    @Test
    @DisplayName("Should parse SELECT with multiple columns")
    void testSelectMultipleColumns() throws SqlParseException {
        String sql = "SELECT id, name, email FROM users";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(1, result.getTables().size());
        assertEquals(3, result.getSelectedColumns().size());
        assertTrue(result.getSelectedColumns().contains("id"));
        assertTrue(result.getSelectedColumns().contains("name"));
        assertTrue(result.getSelectedColumns().contains("email"));
    }

    @Test
    @DisplayName("Should parse SELECT with wildcard")
    void testSelectWildcard() throws SqlParseException {
        String sql = "SELECT * FROM users";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(1, result.getTables().size());
        assertEquals(1, result.getSelectedColumns().size());
        assertEquals("*", result.getSelectedColumns().get(0));
        assertTrue(result.getDescription().contains("all columns"));
    }

    @Test
    @DisplayName("Should parse SELECT with WHERE clause")
    void testSelectWithWhere() throws SqlParseException {
        String sql = "SELECT name FROM users WHERE age > 18";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(1, result.getWhereConditions().size());
        assertEquals("age > 18", result.getWhereConditions().get(0));

        Map<String, Set<String>> tableColumns = result.getTableColumns();
        assertTrue(tableColumns.containsKey("unknown"));
        assertTrue(tableColumns.get("unknown").contains("age"));
    }

    @Test
    @DisplayName("Should parse SELECT with multiple WHERE conditions")
    void testSelectWithMultipleWhereConditions() throws SqlParseException {
        String sql = "SELECT name FROM users WHERE age > 18 AND status = 'active'";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(1, result.getWhereConditions().size());
        assertTrue(result.getWhereConditions().get(0).contains("age > 18"));
        assertTrue(result.getWhereConditions().get(0).contains("status = 'active'"));
    }

    @Test
    @DisplayName("Should parse SELECT with ORDER BY")
    void testSelectWithOrderBy() throws SqlParseException {
        String sql = "SELECT name FROM users ORDER BY name ASC";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(1, result.getOrderByColumns().size());
        assertTrue(result.getOrderByColumns().get(0).contains("name"));
        assertTrue(result.getDescription().contains("sorted"));
    }

    @Test
    @DisplayName("Should parse SELECT with ORDER BY DESC")
    void testSelectWithOrderByDesc() throws SqlParseException {
        String sql = "SELECT name FROM users ORDER BY created_at DESC";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(1, result.getOrderByColumns().size());
        assertTrue(result.getOrderByColumns().get(0).contains("created_at"));
        assertTrue(result.getOrderByColumns().get(0).contains("desc"));
    }

    @Test
    @DisplayName("Should parse SELECT with LIMIT")
    void testSelectWithLimit() throws SqlParseException {
        String sql = "SELECT name FROM users LIMIT 10";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertNotNull(result.getLimitValue());
        assertEquals(10, result.getLimitValue());
        assertTrue(result.getDescription().contains("limited to 10"));
    }

    @Test
    @DisplayName("Should parse INNER JOIN")
    void testInnerJoin() throws SqlParseException {
        String sql = "SELECT u.name, o.total FROM users u " +
                     "INNER JOIN orders o ON u.id = o.user_id";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(2, result.getTables().size());
        assertTrue(result.getTables().get(0).contains("users"));
        assertTrue(result.getTables().get(0).contains("(alias: u)"));
        assertTrue(result.getTables().get(1).contains("orders"));
        assertTrue(result.getTables().get(1).contains("(alias: o)"));

        assertEquals(1, result.getJoinConditions().size());
        assertTrue(result.getJoinConditions().get(0).contains("inner join"));
        assertTrue(result.getJoinConditions().get(0).contains("u.id = o.user_id"));
    }

    @Test
    @DisplayName("Should parse LEFT JOIN")
    void testLeftJoin() throws SqlParseException {
        String sql = "SELECT u.name, o.total FROM users u " +
                     "LEFT JOIN orders o ON u.id = o.user_id";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(2, result.getTables().size());
        assertEquals(1, result.getJoinConditions().size());
        assertTrue(result.getJoinConditions().get(0).toLowerCase().contains("left join"));
    }

    @Test
    @DisplayName("Should parse multiple JOINs")
    void testMultipleJoins() throws SqlParseException {
        String sql = "SELECT c.name, o.total, p.product_name " +
                     "FROM customers c " +
                     "INNER JOIN orders o ON c.id = o.customer_id " +
                     "INNER JOIN order_items oi ON o.id = oi.order_id " +
                     "INNER JOIN products p ON oi.product_id = p.id";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(4, result.getTables().size());
        assertEquals(3, result.getJoinConditions().size());
    }

    @Test
    @DisplayName("Should parse GROUP BY")
    void testGroupBy() throws SqlParseException {
        String sql = "SELECT department, COUNT(*) FROM employees GROUP BY department";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(1, result.getGroupByColumns().size());
        assertEquals("department", result.getGroupByColumns().get(0));
        assertTrue(result.getDescription().contains("grouped by"));
    }

    @Test
    @DisplayName("Should parse GROUP BY with multiple columns")
    void testGroupByMultipleColumns() throws SqlParseException {
        String sql = "SELECT department, location, COUNT(*) FROM employees " +
                     "GROUP BY department, location";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(2, result.getGroupByColumns().size());
        assertTrue(result.getGroupByColumns().contains("department"));
        assertTrue(result.getGroupByColumns().contains("location"));
    }

    @Test
    @DisplayName("Should parse HAVING clause")
    void testHaving() throws SqlParseException {
        String sql = "SELECT department, COUNT(*) as cnt FROM employees " +
                     "GROUP BY department HAVING COUNT(*) > 5";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertNotNull(result.getHavingCondition());
        assertTrue(result.getHavingCondition().contains("count(*) > 5"));
        assertTrue(result.getDescription().contains("groups filtered"));
    }

    @Test
    @DisplayName("Should parse aggregate functions")
    void testAggregateFunctions() throws SqlParseException {
        String sql = "SELECT department, COUNT(*) as emp_count, AVG(salary) as avg_salary, " +
                     "SUM(salary) as total_salary FROM employees GROUP BY department";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(4, result.getSelectedColumns().size());
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("count(*)")));
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("avg(salary)")));
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("sum(salary)")));
    }

    @Test
    @DisplayName("Should parse column aliases")
    void testColumnAliases() throws SqlParseException {
        String sql = "SELECT name AS user_name, email AS user_email FROM users";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(2, result.getSelectedColumns().size());
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("as user_name")));
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("as user_email")));
    }

    @Test
    @DisplayName("Should parse table aliases")
    void testTableAliases() throws SqlParseException {
        String sql = "SELECT u.name FROM users u";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertTrue(result.getTables().get(0).contains("(alias: u)"));
        assertEquals(1, result.getSelectedColumns().size());
        assertEquals("u.name", result.getSelectedColumns().get(0));
    }

    @Test
    @DisplayName("Should parse complex query with all clauses")
    void testComplexQuery() throws SqlParseException {
        String sql = "SELECT u.name, COUNT(o.id) as order_count, SUM(o.total) as total_sales " +
                     "FROM users u " +
                     "INNER JOIN orders o ON u.id = o.user_id " +
                     "WHERE o.status = 'completed' AND o.total > 100 " +
                     "GROUP BY u.name " +
                     "HAVING COUNT(o.id) > 3 " +
                     "ORDER BY total_sales DESC " +
                     "LIMIT 10";
        SqlAnalysisResult result = analyzer.analyze(sql);

        // Verify all components are parsed
        assertEquals(2, result.getTables().size());
        assertEquals(3, result.getSelectedColumns().size());
        assertEquals(1, result.getWhereConditions().size());
        assertEquals(1, result.getJoinConditions().size());
        assertEquals(1, result.getGroupByColumns().size());
        assertNotNull(result.getHavingCondition());
        assertEquals(1, result.getOrderByColumns().size());
        assertEquals(10, result.getLimitValue());

        // Verify description contains key elements
        String description = result.getDescription();
        assertTrue(description.contains("retrieves 3 columns"));
        assertTrue(description.contains("2 tables"));
        assertTrue(description.contains("filtered"));
        assertTrue(description.contains("grouped"));
        assertTrue(description.contains("sorted"));
        assertTrue(description.contains("limited to 10"));
    }

    @Test
    @DisplayName("Should extract table-column mappings")
    void testTableColumnMapping() throws SqlParseException {
        String sql = "SELECT u.id, u.name, u.email, o.order_date, o.total " +
                     "FROM users u " +
                     "INNER JOIN orders o ON u.id = o.user_id " +
                     "WHERE u.status = 'active' AND o.total > 100";
        SqlAnalysisResult result = analyzer.analyze(sql);

        Map<String, Set<String>> tableColumns = result.getTableColumns();

        assertTrue(tableColumns.containsKey("u"));
        Set<String> userColumns = tableColumns.get("u");
        assertTrue(userColumns.contains("id"));
        assertTrue(userColumns.contains("name"));
        assertTrue(userColumns.contains("email"));
        assertTrue(userColumns.contains("status"));

        assertTrue(tableColumns.containsKey("o"));
        Set<String> orderColumns = tableColumns.get("o");
        assertTrue(orderColumns.contains("order_date"));
        assertTrue(orderColumns.contains("total"));
        // Note: user_id is only in the JOIN ON clause, which is not currently extracted to table columns
        // Only columns in SELECT, WHERE, GROUP BY, and ORDER BY are extracted
    }

    @Test
    @DisplayName("Should handle subquery in WHERE clause")
    void testSubqueryInWhere() throws SqlParseException {
        String sql = "SELECT name, salary FROM employees " +
                     "WHERE salary > (SELECT AVG(salary) FROM employees)";
        SqlAnalysisResult result = analyzer.analyze(sql);

        // Main query should have the where condition
        assertEquals(1, result.getWhereConditions().size());
        assertTrue(result.getWhereConditions().get(0).toLowerCase().contains("select")
                && result.getWhereConditions().get(0).contains("avg(salary)"));
    }

    @Test
    @DisplayName("Should throw exception for non-SELECT statements")
    void testNonSelectStatement() {
        String sql = "INSERT INTO users (name) VALUES ('John')";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            analyzer.analyze(sql);
        });

        assertTrue(exception.getMessage().contains("Only SELECT statements are supported"));
    }

    @Test
    @DisplayName("Should throw exception for invalid SQL")
    void testInvalidSql() {
        String sql = "SELECT FROM WHERE";

        assertThrows(SqlParseException.class, () -> {
            analyzer.analyze(sql);
        });
    }

    @Test
    @DisplayName("Should handle empty WHERE clause correctly")
    void testNoWhereClause() throws SqlParseException {
        String sql = "SELECT name FROM users";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertTrue(result.getWhereConditions().isEmpty());
        assertFalse(result.getDescription().contains("filtered"));
    }

    @Test
    @DisplayName("Should reset state between analyses")
    void testStateResetBetweenAnalyses() throws SqlParseException {
        // First analysis
        String sql1 = "SELECT name FROM users WHERE age > 18 ORDER BY name LIMIT 10";
        SqlAnalysisResult result1 = analyzer.analyze(sql1);

        assertEquals(1, result1.getWhereConditions().size());
        assertEquals(1, result1.getOrderByColumns().size());
        assertEquals(10, result1.getLimitValue());

        // Second analysis with different query
        String sql2 = "SELECT id FROM products";
        SqlAnalysisResult result2 = analyzer.analyze(sql2);

        // Verify state was reset
        assertTrue(result2.getWhereConditions().isEmpty());
        assertTrue(result2.getOrderByColumns().isEmpty());
        assertNull(result2.getLimitValue());
        assertEquals(1, result2.getTables().size());
        assertTrue(result2.getTables().get(0).contains("products"));
    }

    @Test
    @DisplayName("Should handle qualified column names")
    void testQualifiedColumnNames() throws SqlParseException {
        String sql = "SELECT users.name, users.email FROM users WHERE users.age > 18";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(2, result.getSelectedColumns().size());
        assertTrue(result.getSelectedColumns().contains("users.name"));
        assertTrue(result.getSelectedColumns().contains("users.email"));

        Map<String, Set<String>> tableColumns = result.getTableColumns();
        assertTrue(tableColumns.containsKey("users"));
        assertTrue(tableColumns.get("users").contains("name"));
        assertTrue(tableColumns.get("users").contains("email"));
        assertTrue(tableColumns.get("users").contains("age"));
    }

    @Test
    @DisplayName("Should parse RIGHT JOIN")
    void testRightJoin() throws SqlParseException {
        String sql = "SELECT u.name, o.total FROM users u " +
                     "RIGHT JOIN orders o ON u.id = o.user_id";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(2, result.getTables().size());
        assertEquals(1, result.getJoinConditions().size());
        assertTrue(result.getJoinConditions().get(0).toLowerCase().contains("right join"));
    }

    @Test
    @DisplayName("Should handle expressions in SELECT")
    void testExpressionsInSelect() throws SqlParseException {
        String sql = "SELECT price * quantity as total, " +
                     "CONCAT(first_name, ' ', last_name) as full_name " +
                     "FROM orders";
        SqlAnalysisResult result = analyzer.analyze(sql);

        assertEquals(2, result.getSelectedColumns().size());
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.contains("price * quantity")));
        assertTrue(result.getSelectedColumns().stream()
                .anyMatch(col -> col.toLowerCase().contains("concat")));
    }

    @Test
    @DisplayName("Should generate description for simple query")
    void testDescriptionSimpleQuery() throws SqlParseException {
        String sql = "SELECT name FROM users";
        SqlAnalysisResult result = analyzer.analyze(sql);

        String description = result.getDescription();
        assertTrue(description.contains("retrieves the column name"));
        assertTrue(description.contains("from the table users"));
    }

    @Test
    @DisplayName("Should generate description with multiple columns")
    void testDescriptionMultipleColumns() throws SqlParseException {
        String sql = "SELECT id, name, email, phone, address FROM users";
        SqlAnalysisResult result = analyzer.analyze(sql);

        String description = result.getDescription();
        assertTrue(description.contains("retrieves 5 columns"));
        assertTrue(description.contains("id, name, email"));
        assertTrue(description.contains("and 2 more"));
    }
}
