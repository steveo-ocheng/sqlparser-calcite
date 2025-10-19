package com.sqlparser;

import java.util.*;

/**
 * Immutable data class that holds the results of SQL query analysis.
 * <p>
 * This class encapsulates all information extracted from a SQL SELECT statement,
 * including tables, columns, conditions, joins, and other SQL clauses. It also
 * includes a human-readable description of what the query does.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>
 * SqlAnalyzer analyzer = new SqlAnalyzer();
 * SqlAnalysisResult result = analyzer.analyze(sql);
 *
 * // Access individual components
 * List&lt;String&gt; tables = result.getTables();
 * String description = result.getDescription();
 *
 * // Get formatted output
 * System.out.println(result.toFormattedString());
 * </pre>
 *
 * <p>
 * All fields in this class are final and cannot be modified after construction,
 * ensuring immutability and thread safety.
 * </p>
 *
 * @author SQL Parser Team
 * @version 1.0.0
 * @see SqlAnalyzer
 */
public class SqlAnalysisResult {
    /** List of table names with their aliases (if any). */
    private final List<String> tables;

    /** Map of table names to sets of column names referenced in the query. */
    private final Map<String, Set<String>> tableColumns;

    /** List of columns or expressions in the SELECT clause. */
    private final List<String> selectedColumns;

    /** List of conditions from the WHERE clause. */
    private final List<String> whereConditions;

    /** List of JOIN conditions as they appear in the query. */
    private final List<String> joinConditions;

    /** List of columns or expressions in the GROUP BY clause. */
    private final List<String> groupByColumns;

    /** List of columns or expressions in the ORDER BY clause. */
    private final List<String> orderByColumns;

    /** Condition from the HAVING clause, or null if not present. */
    private final String havingCondition;

    /** Limit value from the LIMIT clause, or null if not present. */
    private final Integer limitValue;

    /** Human-readable description of what the query does. */
    private final String description;

    /**
     * Constructs a new SqlAnalysisResult with all analysis components.
     *
     * @param tables list of table names with aliases
     * @param tableColumns map of table names to their referenced columns
     * @param selectedColumns list of SELECT clause items
     * @param whereConditions list of WHERE clause conditions
     * @param joinConditions list of JOIN conditions
     * @param groupByColumns list of GROUP BY clause items
     * @param orderByColumns list of ORDER BY clause items
     * @param havingCondition HAVING clause condition (can be null)
     * @param limitValue LIMIT value (can be null)
     * @param description human-readable query description
     */
    public SqlAnalysisResult(
            List<String> tables,
            Map<String, Set<String>> tableColumns,
            List<String> selectedColumns,
            List<String> whereConditions,
            List<String> joinConditions,
            List<String> groupByColumns,
            List<String> orderByColumns,
            String havingCondition,
            Integer limitValue,
            String description) {
        this.tables = tables;
        this.tableColumns = tableColumns;
        this.selectedColumns = selectedColumns;
        this.whereConditions = whereConditions;
        this.joinConditions = joinConditions;
        this.groupByColumns = groupByColumns;
        this.orderByColumns = orderByColumns;
        this.havingCondition = havingCondition;
        this.limitValue = limitValue;
        this.description = description;
    }

    /**
     * Returns the list of table names with their aliases.
     *
     * @return list of tables (e.g., "users (alias: u)")
     */
    public List<String> getTables() {
        return tables;
    }

    /**
     * Returns the mapping of table names to their referenced columns.
     *
     * @return map where keys are table names and values are sets of column names
     */
    public Map<String, Set<String>> getTableColumns() {
        return tableColumns;
    }

    /**
     * Returns the list of selected columns or expressions.
     *
     * @return list of SELECT clause items
     */
    public List<String> getSelectedColumns() {
        return selectedColumns;
    }

    /**
     * Returns the list of WHERE clause conditions.
     *
     * @return list of WHERE conditions (empty if no WHERE clause)
     */
    public List<String> getWhereConditions() {
        return whereConditions;
    }

    /**
     * Returns the list of JOIN conditions.
     *
     * @return list of JOIN conditions (empty if no joins)
     */
    public List<String> getJoinConditions() {
        return joinConditions;
    }

    /**
     * Returns the list of GROUP BY columns or expressions.
     *
     * @return list of GROUP BY items (empty if no GROUP BY clause)
     */
    public List<String> getGroupByColumns() {
        return groupByColumns;
    }

    /**
     * Returns the list of ORDER BY columns or expressions.
     *
     * @return list of ORDER BY items (empty if no ORDER BY clause)
     */
    public List<String> getOrderByColumns() {
        return orderByColumns;
    }

    /**
     * Returns the HAVING clause condition.
     *
     * @return HAVING condition, or null if no HAVING clause
     */
    public String getHavingCondition() {
        return havingCondition;
    }

    /**
     * Returns the LIMIT value.
     *
     * @return limit value, or null if no LIMIT clause
     */
    public Integer getLimitValue() {
        return limitValue;
    }

    /**
     * Returns the human-readable description of the query.
     *
     * @return natural language description of what the query does
     */
    public String getDescription() {
        return description;
    }

    /**
     * Formats the analysis results as a human-readable string.
     * <p>
     * The output includes all components of the analysis organized into sections:
     * DESCRIPTION, TABLES, TABLE-COLUMNS, SELECTED COLUMNS, WHERE CONDITIONS,
     * JOIN CONDITIONS, GROUP BY, HAVING, ORDER BY, and LIMIT.
     * </p>
     *
     * @return formatted string representation of the analysis
     */
    public String toFormattedString() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== SQL ANALYSIS ===\n\n");

        sb.append("DESCRIPTION:\n");
        sb.append("  ").append(description).append("\n\n");

        sb.append("TABLES:\n");
        for (String table : tables) {
            sb.append("  - ").append(table).append("\n");
        }
        sb.append("\n");

        sb.append("TABLE-COLUMNS:\n");
        for (Map.Entry<String, Set<String>> entry : tableColumns.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                sb.append("  ").append(entry.getKey()).append(":\n");
                for (String column : entry.getValue()) {
                    sb.append("    - ").append(column).append("\n");
                }
            }
        }
        sb.append("\n");

        sb.append("SELECTED COLUMNS:\n");
        for (String col : selectedColumns) {
            sb.append("  - ").append(col).append("\n");
        }
        sb.append("\n");

        if (!whereConditions.isEmpty()) {
            sb.append("WHERE CONDITIONS:\n");
            for (String condition : whereConditions) {
                sb.append("  - ").append(condition).append("\n");
            }
            sb.append("\n");
        }

        if (!joinConditions.isEmpty()) {
            sb.append("JOIN CONDITIONS:\n");
            for (String join : joinConditions) {
                sb.append("  - ").append(join).append("\n");
            }
            sb.append("\n");
        }

        if (!groupByColumns.isEmpty()) {
            sb.append("GROUP BY:\n");
            for (String col : groupByColumns) {
                sb.append("  - ").append(col).append("\n");
            }
            sb.append("\n");
        }

        if (havingCondition != null) {
            sb.append("HAVING:\n");
            sb.append("  - ").append(havingCondition).append("\n\n");
        }

        if (!orderByColumns.isEmpty()) {
            sb.append("ORDER BY:\n");
            for (String col : orderByColumns) {
                sb.append("  - ").append(col).append("\n");
            }
            sb.append("\n");
        }

        if (limitValue != null) {
            sb.append("LIMIT:\n");
            sb.append("  - ").append(limitValue).append("\n\n");
        }

        return sb.toString();
    }
}
