package com.sqlparser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SqlAnalysisResult Tests")
class SqlAnalysisResultTest {

    private List<String> tables;
    private Map<String, Set<String>> tableColumns;
    private List<String> selectedColumns;
    private List<String> whereConditions;
    private List<String> joinConditions;
    private List<String> groupByColumns;
    private List<String> orderByColumns;
    private String havingCondition;
    private Integer limitValue;
    private String description;

    @BeforeEach
    void setUp() {
        tables = new ArrayList<>();
        tableColumns = new LinkedHashMap<>();
        selectedColumns = new ArrayList<>();
        whereConditions = new ArrayList<>();
        joinConditions = new ArrayList<>();
        groupByColumns = new ArrayList<>();
        orderByColumns = new ArrayList<>();
        havingCondition = null;
        limitValue = null;
        description = "Test description";
    }

    @Test
    @DisplayName("Should create result object with all fields")
    void testResultCreation() {
        tables.add("users");
        selectedColumns.add("name");
        selectedColumns.add("email");

        SqlAnalysisResult result = new SqlAnalysisResult(
                tables, tableColumns, selectedColumns, whereConditions,
                joinConditions, groupByColumns, orderByColumns,
                havingCondition, limitValue, description
        );

        assertEquals(1, result.getTables().size());
        assertEquals("users", result.getTables().get(0));
        assertEquals(2, result.getSelectedColumns().size());
        assertEquals("Test description", result.getDescription());
    }

    @Test
    @DisplayName("Should return empty lists for empty inputs")
    void testEmptyLists() {
        SqlAnalysisResult result = new SqlAnalysisResult(
                tables, tableColumns, selectedColumns, whereConditions,
                joinConditions, groupByColumns, orderByColumns,
                havingCondition, limitValue, description
        );

        assertTrue(result.getTables().isEmpty());
        assertTrue(result.getSelectedColumns().isEmpty());
        assertTrue(result.getWhereConditions().isEmpty());
        assertTrue(result.getJoinConditions().isEmpty());
        assertTrue(result.getGroupByColumns().isEmpty());
        assertTrue(result.getOrderByColumns().isEmpty());
        assertNull(result.getHavingCondition());
        assertNull(result.getLimitValue());
    }

    @Test
    @DisplayName("Should return table columns mapping")
    void testTableColumnsMapping() {
        Set<String> userColumns = new LinkedHashSet<>();
        userColumns.add("id");
        userColumns.add("name");
        userColumns.add("email");
        tableColumns.put("users", userColumns);

        Set<String> orderColumns = new LinkedHashSet<>();
        orderColumns.add("order_id");
        orderColumns.add("total");
        tableColumns.put("orders", orderColumns);

        SqlAnalysisResult result = new SqlAnalysisResult(
                tables, tableColumns, selectedColumns, whereConditions,
                joinConditions, groupByColumns, orderByColumns,
                havingCondition, limitValue, description
        );

        Map<String, Set<String>> returnedColumns = result.getTableColumns();
        assertEquals(2, returnedColumns.size());
        assertTrue(returnedColumns.containsKey("users"));
        assertTrue(returnedColumns.containsKey("orders"));
        assertEquals(3, returnedColumns.get("users").size());
        assertEquals(2, returnedColumns.get("orders").size());
    }

    @Test
    @DisplayName("Should format output with all sections")
    void testFormattedStringComplete() {
        tables.add("users (alias: u)");
        tables.add("orders (alias: o)");

        Set<String> userColumns = new LinkedHashSet<>();
        userColumns.add("id");
        userColumns.add("name");
        tableColumns.put("u", userColumns);

        Set<String> orderColumns = new LinkedHashSet<>();
        orderColumns.add("total");
        tableColumns.put("o", orderColumns);

        selectedColumns.add("u.name");
        selectedColumns.add("o.total");

        whereConditions.add("o.total > 100");
        joinConditions.add("INNER JOIN orders o ON u.id = o.user_id");
        groupByColumns.add("u.name");
        orderByColumns.add("o.total DESC");
        havingCondition = "COUNT(*) > 5";
        limitValue = 10;

        SqlAnalysisResult result = new SqlAnalysisResult(
                tables, tableColumns, selectedColumns, whereConditions,
                joinConditions, groupByColumns, orderByColumns,
                havingCondition, limitValue, description
        );

        String formatted = result.toFormattedString();

        // Verify all sections are present
        assertTrue(formatted.contains("=== SQL ANALYSIS ==="));
        assertTrue(formatted.contains("DESCRIPTION:"));
        assertTrue(formatted.contains("Test description"));
        assertTrue(formatted.contains("TABLES:"));
        assertTrue(formatted.contains("users (alias: u)"));
        assertTrue(formatted.contains("orders (alias: o)"));
        assertTrue(formatted.contains("TABLE-COLUMNS:"));
        assertTrue(formatted.contains("SELECTED COLUMNS:"));
        assertTrue(formatted.contains("u.name"));
        assertTrue(formatted.contains("o.total"));
        assertTrue(formatted.contains("WHERE CONDITIONS:"));
        assertTrue(formatted.contains("o.total > 100"));
        assertTrue(formatted.contains("JOIN CONDITIONS:"));
        assertTrue(formatted.contains("INNER JOIN"));
        assertTrue(formatted.contains("GROUP BY:"));
        assertTrue(formatted.contains("HAVING:"));
        assertTrue(formatted.contains("COUNT(*) > 5"));
        assertTrue(formatted.contains("ORDER BY:"));
        assertTrue(formatted.contains("o.total DESC"));
        assertTrue(formatted.contains("LIMIT:"));
        assertTrue(formatted.contains("10"));
    }

    @Test
    @DisplayName("Should format output with minimal data")
    void testFormattedStringMinimal() {
        tables.add("users");
        selectedColumns.add("name");

        SqlAnalysisResult result = new SqlAnalysisResult(
                tables, tableColumns, selectedColumns, whereConditions,
                joinConditions, groupByColumns, orderByColumns,
                havingCondition, limitValue, description
        );

        String formatted = result.toFormattedString();

        assertTrue(formatted.contains("TABLES:"));
        assertTrue(formatted.contains("users"));
        assertTrue(formatted.contains("SELECTED COLUMNS:"));
        assertTrue(formatted.contains("name"));
        assertFalse(formatted.contains("WHERE CONDITIONS:"));
        assertFalse(formatted.contains("JOIN CONDITIONS:"));
        assertFalse(formatted.contains("GROUP BY:"));
        assertFalse(formatted.contains("HAVING:"));
        assertFalse(formatted.contains("ORDER BY:"));
        assertFalse(formatted.contains("LIMIT:"));
    }

    @Test
    @DisplayName("Should not include empty table columns")
    void testFormattedStringEmptyTableColumns() {
        tables.add("users");
        selectedColumns.add("*");

        Set<String> emptyColumns = new LinkedHashSet<>();
        tableColumns.put("users", emptyColumns);

        SqlAnalysisResult result = new SqlAnalysisResult(
                tables, tableColumns, selectedColumns, whereConditions,
                joinConditions, groupByColumns, orderByColumns,
                havingCondition, limitValue, description
        );

        String formatted = result.toFormattedString();

        assertTrue(formatted.contains("TABLE-COLUMNS:"));
        // Empty table should not have columns listed
        int tableColumnsIndex = formatted.indexOf("TABLE-COLUMNS:");
        int selectedColumnsIndex = formatted.indexOf("SELECTED COLUMNS:");
        String tableColumnsSection = formatted.substring(tableColumnsIndex, selectedColumnsIndex);
        assertFalse(tableColumnsSection.contains("users:"));
    }

    @Test
    @DisplayName("Should include WHERE section when conditions exist")
    void testFormattedStringWithWhere() {
        tables.add("users");
        selectedColumns.add("name");
        whereConditions.add("age > 18");

        SqlAnalysisResult result = new SqlAnalysisResult(
                tables, tableColumns, selectedColumns, whereConditions,
                joinConditions, groupByColumns, orderByColumns,
                havingCondition, limitValue, description
        );

        String formatted = result.toFormattedString();

        assertTrue(formatted.contains("WHERE CONDITIONS:"));
        assertTrue(formatted.contains("age > 18"));
    }

    @Test
    @DisplayName("Should include JOIN section when joins exist")
    void testFormattedStringWithJoin() {
        tables.add("users");
        selectedColumns.add("name");
        joinConditions.add("INNER JOIN orders ON users.id = orders.user_id");

        SqlAnalysisResult result = new SqlAnalysisResult(
                tables, tableColumns, selectedColumns, whereConditions,
                joinConditions, groupByColumns, orderByColumns,
                havingCondition, limitValue, description
        );

        String formatted = result.toFormattedString();

        assertTrue(formatted.contains("JOIN CONDITIONS:"));
        assertTrue(formatted.contains("INNER JOIN orders"));
    }

    @Test
    @DisplayName("Should include GROUP BY section when grouping exists")
    void testFormattedStringWithGroupBy() {
        tables.add("employees");
        selectedColumns.add("department");
        selectedColumns.add("COUNT(*)");
        groupByColumns.add("department");

        SqlAnalysisResult result = new SqlAnalysisResult(
                tables, tableColumns, selectedColumns, whereConditions,
                joinConditions, groupByColumns, orderByColumns,
                havingCondition, limitValue, description
        );

        String formatted = result.toFormattedString();

        assertTrue(formatted.contains("GROUP BY:"));
        assertTrue(formatted.contains("department"));
    }

    @Test
    @DisplayName("Should include ORDER BY section when sorting exists")
    void testFormattedStringWithOrderBy() {
        tables.add("users");
        selectedColumns.add("name");
        orderByColumns.add("name ASC");

        SqlAnalysisResult result = new SqlAnalysisResult(
                tables, tableColumns, selectedColumns, whereConditions,
                joinConditions, groupByColumns, orderByColumns,
                havingCondition, limitValue, description
        );

        String formatted = result.toFormattedString();

        assertTrue(formatted.contains("ORDER BY:"));
        assertTrue(formatted.contains("name ASC"));
    }

    @Test
    @DisplayName("Should include LIMIT section when limit exists")
    void testFormattedStringWithLimit() {
        tables.add("users");
        selectedColumns.add("name");
        limitValue = 25;

        SqlAnalysisResult result = new SqlAnalysisResult(
                tables, tableColumns, selectedColumns, whereConditions,
                joinConditions, groupByColumns, orderByColumns,
                havingCondition, limitValue, description
        );

        String formatted = result.toFormattedString();

        assertTrue(formatted.contains("LIMIT:"));
        assertTrue(formatted.contains("25"));
    }

    @Test
    @DisplayName("Should include HAVING section when having exists")
    void testFormattedStringWithHaving() {
        tables.add("employees");
        selectedColumns.add("department");
        groupByColumns.add("department");
        havingCondition = "AVG(salary) > 50000";

        SqlAnalysisResult result = new SqlAnalysisResult(
                tables, tableColumns, selectedColumns, whereConditions,
                joinConditions, groupByColumns, orderByColumns,
                havingCondition, limitValue, description
        );

        String formatted = result.toFormattedString();

        assertTrue(formatted.contains("HAVING:"));
        assertTrue(formatted.contains("AVG(salary) > 50000"));
    }

    @Test
    @DisplayName("Should handle multiple WHERE conditions")
    void testMultipleWhereConditions() {
        tables.add("users");
        selectedColumns.add("name");
        whereConditions.add("age > 18");
        whereConditions.add("status = 'active'");

        SqlAnalysisResult result = new SqlAnalysisResult(
                tables, tableColumns, selectedColumns, whereConditions,
                joinConditions, groupByColumns, orderByColumns,
                havingCondition, limitValue, description
        );

        assertEquals(2, result.getWhereConditions().size());

        String formatted = result.toFormattedString();
        assertTrue(formatted.contains("age > 18"));
        assertTrue(formatted.contains("status = 'active'"));
    }

    @Test
    @DisplayName("Should handle multiple JOIN conditions")
    void testMultipleJoinConditions() {
        tables.add("users");
        selectedColumns.add("name");
        joinConditions.add("INNER JOIN orders ON users.id = orders.user_id");
        joinConditions.add("LEFT JOIN profiles ON users.id = profiles.user_id");

        SqlAnalysisResult result = new SqlAnalysisResult(
                tables, tableColumns, selectedColumns, whereConditions,
                joinConditions, groupByColumns, orderByColumns,
                havingCondition, limitValue, description
        );

        assertEquals(2, result.getJoinConditions().size());

        String formatted = result.toFormattedString();
        assertTrue(formatted.contains("INNER JOIN orders"));
        assertTrue(formatted.contains("LEFT JOIN profiles"));
    }

    @Test
    @DisplayName("Should handle multiple ORDER BY columns")
    void testMultipleOrderByColumns() {
        tables.add("users");
        selectedColumns.add("name");
        orderByColumns.add("last_name ASC");
        orderByColumns.add("first_name ASC");

        SqlAnalysisResult result = new SqlAnalysisResult(
                tables, tableColumns, selectedColumns, whereConditions,
                joinConditions, groupByColumns, orderByColumns,
                havingCondition, limitValue, description
        );

        assertEquals(2, result.getOrderByColumns().size());

        String formatted = result.toFormattedString();
        assertTrue(formatted.contains("last_name ASC"));
        assertTrue(formatted.contains("first_name ASC"));
    }

    @Test
    @DisplayName("Should preserve order of selected columns")
    void testColumnOrderPreserved() {
        selectedColumns.add("id");
        selectedColumns.add("name");
        selectedColumns.add("email");
        selectedColumns.add("phone");

        SqlAnalysisResult result = new SqlAnalysisResult(
                tables, tableColumns, selectedColumns, whereConditions,
                joinConditions, groupByColumns, orderByColumns,
                havingCondition, limitValue, description
        );

        List<String> cols = result.getSelectedColumns();
        assertEquals("id", cols.get(0));
        assertEquals("name", cols.get(1));
        assertEquals("email", cols.get(2));
        assertEquals("phone", cols.get(3));
    }

    @Test
    @DisplayName("Should preserve order of tables")
    void testTableOrderPreserved() {
        tables.add("users");
        tables.add("orders");
        tables.add("products");

        SqlAnalysisResult result = new SqlAnalysisResult(
                tables, tableColumns, selectedColumns, whereConditions,
                joinConditions, groupByColumns, orderByColumns,
                havingCondition, limitValue, description
        );

        List<String> resultTables = result.getTables();
        assertEquals("users", resultTables.get(0));
        assertEquals("orders", resultTables.get(1));
        assertEquals("products", resultTables.get(2));
    }
}
