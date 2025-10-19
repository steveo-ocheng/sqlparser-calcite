package com.sqlparser;

import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlBasicVisitor;
import org.apache.calcite.sql.validate.SqlConformanceEnum;

import java.util.*;

/**
 * Analyzes SQL SELECT statements and extracts detailed information about their structure.
 * <p>
 * This class uses the Apache Calcite library to parse SQL queries and extract various components
 * including tables, columns, conditions, joins, and other SQL clauses. It also generates
 * human-readable descriptions of what the SQL query does.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>
 * SqlAnalyzer analyzer = new SqlAnalyzer();
 * String sql = "SELECT u.name, o.total FROM users u INNER JOIN orders o ON u.id = o.user_id WHERE o.total &gt; 100";
 * SqlAnalysisResult result = analyzer.analyze(sql);
 * System.out.println(result.getDescription());
 * </pre>
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Extracts all tables and their aliases</li>
 *   <li>Maps columns to their respective tables</li>
 *   <li>Identifies SELECT items, WHERE conditions, and JOIN conditions</li>
 *   <li>Processes GROUP BY, HAVING, ORDER BY, and LIMIT clauses</li>
 *   <li>Generates natural language descriptions of queries</li>
 * </ul>
 *
 * <p>
 * <b>Note:</b> This class maintains internal state during analysis and automatically resets
 * before each new analysis. The same instance can be safely reused for multiple analyses.
 * </p>
 *
 * @author SQL Parser Team
 * @version 1.0.0
 * @see SqlAnalysisResult
 * @see org.apache.calcite.sql.parser.SqlParser
 */
public class SqlAnalyzer {

    /** Set of table names with their aliases (if any) found in the query. */
    private Set<String> tables = new LinkedHashSet<>();

    /** Map of table names to their columns referenced in the query. */
    private Map<String, Set<String>> tableColumns = new LinkedHashMap<>();

    /** List of columns or expressions selected in the SELECT clause. */
    private List<String> selectedColumns = new ArrayList<>();

    /** List of conditions found in the WHERE clause. */
    private List<String> whereConditions = new ArrayList<>();

    /** List of JOIN conditions as they appear in the query. */
    private List<String> joinConditions = new ArrayList<>();

    /** List of columns or expressions in the GROUP BY clause. */
    private List<String> groupByColumns = new ArrayList<>();

    /** List of columns or expressions in the ORDER BY clause. */
    private List<String> orderByColumns = new ArrayList<>();

    /** Condition from the HAVING clause, if present. */
    private String havingCondition = null;

    /** Limit value from the LIMIT/FETCH clause, if present. */
    private Integer limitValue = null;

    /**
     * Analyzes a SQL SELECT statement and extracts its components.
     * <p>
     * This method parses the provided SQL string, extracts all relevant components
     * (tables, columns, conditions, etc.), generates a natural language description,
     * and returns an immutable result object containing all the analysis data.
     * </p>
     *
     * <p>
     * The internal state is automatically reset before each analysis, so the same
     * analyzer instance can be reused for multiple queries.
     * </p>
     *
     * @param sql the SQL SELECT statement to analyze (must be a valid SELECT query)
     * @return a {@link SqlAnalysisResult} object containing all extracted information
     * @throws SqlParseException if the SQL cannot be parsed (invalid syntax)
     * @throws IllegalArgumentException if the statement is not a SELECT statement
     * @see SqlAnalysisResult
     */
    public SqlAnalysisResult analyze(String sql) throws SqlParseException {
        reset();

        // Configure parser for more lenient SQL parsing
        SqlParser.Config config = SqlParser.config()
            .withConformance(SqlConformanceEnum.DEFAULT)
            .withCaseSensitive(false);

        SqlParser parser = SqlParser.create(sql, config);
        SqlNode sqlNode = parser.parseQuery();

        if (sqlNode instanceof SqlSelect) {
            processSelect((SqlSelect) sqlNode);
        } else if (sqlNode instanceof SqlOrderBy) {
            // Handle queries wrapped in ORDER BY node
            SqlOrderBy orderBy = (SqlOrderBy) sqlNode;
            if (orderBy.query instanceof SqlSelect) {
                processSelect((SqlSelect) orderBy.query);
                processOrderBy(orderBy);
            }
        } else {
            throw new IllegalArgumentException("Only SELECT statements are supported");
        }

        return new SqlAnalysisResult(
            new ArrayList<>(tables),
            tableColumns,
            selectedColumns,
            whereConditions,
            joinConditions,
            groupByColumns,
            orderByColumns,
            havingCondition,
            limitValue,
            generateDescription()
        );
    }

    /**
     * Resets all internal state to prepare for a new analysis.
     * <p>
     * Clears all collections and nullifies optional fields. This method is
     * automatically called at the beginning of each {@link #analyze(String)} call.
     * </p>
     */
    private void reset() {
        tables.clear();
        tableColumns.clear();
        selectedColumns.clear();
        whereConditions.clear();
        joinConditions.clear();
        groupByColumns.clear();
        orderByColumns.clear();
        havingCondition = null;
        limitValue = null;
    }

    /**
     * Processes a SELECT statement and extracts all its components.
     * <p>
     * This method traverses the SQL Abstract Syntax Tree (AST) and extracts:
     * FROM items, JOINs, SELECT items, WHERE conditions, GROUP BY, HAVING,
     * ORDER BY, and LIMIT clauses.
     * </p>
     *
     * @param select the parsed SELECT statement to process
     */
    private void processSelect(SqlSelect select) {
        // Process FROM clause
        if (select.getFrom() != null) {
            processFromClause(select.getFrom());
        }

        // Process SELECT list
        if (select.getSelectList() != null) {
            for (SqlNode selectItem : select.getSelectList()) {
                processSelectItem(selectItem);
            }
        }

        // Process WHERE clause
        if (select.getWhere() != null) {
            processWhereExpression(select.getWhere());
        }

        // Process GROUP BY
        if (select.getGroup() != null) {
            for (SqlNode groupByNode : select.getGroup()) {
                String groupByStr = groupByNode.toString();
                groupByColumns.add(groupByStr);
                extractColumnsFromNode(groupByNode);
            }
        }

        // Process HAVING
        if (select.getHaving() != null) {
            havingCondition = select.getHaving().toString();
            extractColumnsFromNode(select.getHaving());
        }

        // Process ORDER BY (if directly in SELECT)
        if (select.getOrderList() != null) {
            for (SqlNode orderByNode : select.getOrderList()) {
                orderByColumns.add(orderByNode.toString());
                extractColumnsFromNode(orderByNode);
            }
        }

        // Process FETCH/LIMIT
        if (select.getFetch() != null) {
            try {
                limitValue = Integer.parseInt(select.getFetch().toString());
            } catch (NumberFormatException e) {
                // If it's not a simple number, store it as a string representation
            }
        }
    }

    /**
     * Processes ORDER BY clause from SqlOrderBy wrapper.
     *
     * @param orderBy the SqlOrderBy node
     */
    private void processOrderBy(SqlOrderBy orderBy) {
        if (orderBy.orderList != null) {
            for (SqlNode orderByNode : orderBy.orderList) {
                orderByColumns.add(orderByNode.toString());
                extractColumnsFromNode(orderByNode);
            }
        }

        if (orderBy.fetch != null) {
            try {
                limitValue = Integer.parseInt(orderBy.fetch.toString());
            } catch (NumberFormatException e) {
                // If it's not a simple number, ignore
            }
        }
    }

    /**
     * Processes a FROM clause (table or JOIN).
     *
     * @param fromNode the FROM clause node
     */
    private void processFromClause(SqlNode fromNode) {
        if (fromNode instanceof SqlJoin) {
            SqlJoin join = (SqlJoin) fromNode;

            // Process left side of join
            processFromClause(join.getLeft());

            // Process right side of join
            processFromClause(join.getRight());

            // Process join condition
            if (join.getCondition() != null) {
                joinConditions.add(join.toString());
                extractColumnsFromNode(join.getCondition());
            }
        } else if (fromNode instanceof SqlBasicCall) {
            SqlBasicCall call = (SqlBasicCall) fromNode;
            // Handle aliased tables: table AS alias
            if (call.getOperator().getName().equals("AS")) {
                SqlNode table = call.operand(0);
                SqlNode alias = call.operand(1);
                String tableName = extractTableName(table);
                String aliasName = alias.toString();
                tables.add(tableName + " (alias: " + aliasName + ")");
                tableColumns.putIfAbsent(tableName, new LinkedHashSet<>());
            } else {
                // Handle other table expressions
                String tableName = call.toString();
                tables.add(tableName);
                tableColumns.putIfAbsent(tableName, new LinkedHashSet<>());
            }
        } else if (fromNode instanceof SqlIdentifier) {
            String tableName = ((SqlIdentifier) fromNode).toString();
            tables.add(tableName);
            tableColumns.putIfAbsent(tableName, new LinkedHashSet<>());
        } else if (fromNode instanceof SqlSelect) {
            // Subquery
            processSelect((SqlSelect) fromNode);
        }
    }

    /**
     * Extracts table name from a node.
     *
     * @param node the node to extract from
     * @return the table name
     */
    private String extractTableName(SqlNode node) {
        if (node instanceof SqlIdentifier) {
            return ((SqlIdentifier) node).toString();
        }
        return node.toString();
    }

    /**
     * Processes a SELECT item (column, expression, or *).
     *
     * @param selectItem the SELECT item node
     */
    private void processSelectItem(SqlNode selectItem) {
        if (selectItem instanceof SqlBasicCall) {
            SqlBasicCall call = (SqlBasicCall) selectItem;
            // Handle AS alias
            if (call.getOperator().getName().equals("AS")) {
                SqlNode expr = call.operand(0);
                SqlNode alias = call.operand(1);
                selectedColumns.add(expr.toString() + " AS " + alias.toString());
                extractColumnsFromNode(expr);
            } else {
                // Other expressions (functions, operators, etc.)
                selectedColumns.add(call.toString());
                extractColumnsFromNode(call);
            }
        } else if (selectItem instanceof SqlIdentifier) {
            String columnStr = selectItem.toString();
            selectedColumns.add(columnStr);

            // Check if it's a qualified column (table.column)
            SqlIdentifier identifier = (SqlIdentifier) selectItem;
            if (identifier.names.size() > 1) {
                String tableName = identifier.names.get(0);
                String columnName = identifier.names.get(1);
                tableColumns.putIfAbsent(tableName, new LinkedHashSet<>());
                tableColumns.get(tableName).add(columnName);
            }
        } else {
            selectedColumns.add(selectItem.toString());
            extractColumnsFromNode(selectItem);
        }
    }

    /**
     * Recursively extracts column references from a SqlNode.
     *
     * @param node the node to analyze
     */
    private void extractColumnsFromNode(SqlNode node) {
        if (node == null) {
            return;
        }

        if (node instanceof SqlIdentifier) {
            SqlIdentifier identifier = (SqlIdentifier) node;
            if (identifier.names.size() > 1) {
                // Qualified column: table.column
                String tableName = identifier.names.get(0);
                String columnName = identifier.names.get(1);
                tableColumns.putIfAbsent(tableName, new LinkedHashSet<>());
                tableColumns.get(tableName).add(columnName);
            } else if (identifier.names.size() == 1) {
                // Unqualified column
                String columnName = identifier.names.get(0);
                tableColumns.putIfAbsent("unknown", new LinkedHashSet<>());
                tableColumns.get("unknown").add(columnName);
            }
        } else if (node instanceof SqlBasicCall) {
            SqlBasicCall call = (SqlBasicCall) node;
            for (SqlNode operand : call.getOperandList()) {
                extractColumnsFromNode(operand);
            }
        } else if (node instanceof SqlNodeList) {
            SqlNodeList nodeList = (SqlNodeList) node;
            for (SqlNode n : nodeList) {
                extractColumnsFromNode(n);
            }
        }
    }

    /**
     * Processes a WHERE clause expression.
     *
     * @param whereNode the WHERE expression node
     */
    private void processWhereExpression(SqlNode whereNode) {
        whereConditions.add(whereNode.toString());
        extractColumnsFromNode(whereNode);
    }

    /**
     * Generates a human-readable English description of the SQL query.
     * <p>
     * The description summarizes what the query does, including what columns
     * are selected, from which tables, with what conditions, joins, grouping,
     * ordering, and limits.
     * </p>
     *
     * @return a natural language description of the query
     */
    private String generateDescription() {
        StringBuilder desc = new StringBuilder();

        desc.append("This query ");

        // Describe what is being selected
        if (selectedColumns.stream().anyMatch(c -> c.equals("*") || c.contains(".*"))) {
            desc.append("retrieves all columns");
        } else if (selectedColumns.size() == 1) {
            desc.append("retrieves the column ").append(selectedColumns.get(0));
        } else {
            desc.append("retrieves ").append(selectedColumns.size()).append(" columns (");
            desc.append(String.join(", ", selectedColumns.subList(0, Math.min(3, selectedColumns.size()))));
            if (selectedColumns.size() > 3) {
                desc.append(", and ").append(selectedColumns.size() - 3).append(" more");
            }
            desc.append(")");
        }

        // Describe tables
        if (tables.size() == 1) {
            desc.append(" from the table ").append(tables.iterator().next());
        } else if (tables.size() > 1) {
            desc.append(" from ").append(tables.size()).append(" tables (");
            List<String> tableList = new ArrayList<>(tables);
            desc.append(String.join(", ", tableList));
            desc.append(")");
        }

        // Describe joins
        if (!joinConditions.isEmpty()) {
            desc.append(", joining tables");
            if (joinConditions.size() == 1) {
                desc.append(" on condition: ").append(joinConditions.get(0));
            } else {
                desc.append(" using ").append(joinConditions.size()).append(" join conditions");
            }
        }

        // Describe WHERE clause
        if (!whereConditions.isEmpty()) {
            desc.append(". The results are filtered");
            if (whereConditions.size() == 1) {
                desc.append(" where ").append(whereConditions.get(0));
            } else {
                desc.append(" using ").append(whereConditions.size()).append(" conditions");
            }
        }

        // Describe GROUP BY
        if (!groupByColumns.isEmpty()) {
            desc.append(". Results are grouped by ").append(String.join(", ", groupByColumns));
        }

        // Describe HAVING
        if (havingCondition != null) {
            desc.append(", with groups filtered by ").append(havingCondition);
        }

        // Describe ORDER BY
        if (!orderByColumns.isEmpty()) {
            desc.append(". Results are sorted by ").append(String.join(", ", orderByColumns));
        }

        // Describe LIMIT
        if (limitValue != null) {
            desc.append(", limited to ").append(limitValue).append(" rows");
        }

        desc.append(".");

        return desc.toString();
    }
}
