package io.github.soldelv.sql.builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import utils.Printer;

import java.util.ArrayList;
import java.util.List;

import static io.github.soldelv.sql.builder.QueryBuilder.MainStatement.*;
import static io.github.soldelv.sql.builder.QueryBuilder.Operator.*;
import static io.github.soldelv.sql.builder.QueryBuilder.Statement.*;
import static utils.StringUtilities.contextCheck;

public class QueryBuilder {
    static Printer log = new Printer(QueryBuilder.class);

    @Getter
    protected enum MainStatement {
        SELECT("SELECT "),
        FROM(" FROM "),
        GROUP_BY(" GROUP BY "),
        HAVING(" HAVING {condition} "),
        ORDER_BY(" ORDER BY ");

        public final String value;

        MainStatement(String operator) {
            this.value = operator;
        }
    }

    @Getter
    public enum Statement {
        WHERE(" WHERE "),
        AND(" AND ");

        public final String value;

        Statement(String operator) {
            this.value = operator;
        }
    }

    @Getter
    protected enum SelectionType {
        ALL("*"),
        TOP_COLUMN("TOP({number}) {column} AS singleResult"),
        TOP_ALL("TOP({number}) * "),
        COUNT_OF("COUNT({column}) AS singleResult"),
        COUNT_ALL("COUNT(*) AS singleResult"),
        DISTINCT("DISTINCT {column}");
        final String value;

        SelectionType(String key) {
            this.value = key;
        }
    }

    @Data
    @AllArgsConstructor
    public static class JoinClause {
        @Getter
        public enum JoinType {
            JOIN(" JOIN "),
            LEFT_JOIN(" LEFT JOIN "),
            RIGHT_JOIN(" RIGHT JOIN "),
            INNER_JOIN(" INNER JOIN "),
            FULL_JOIN(" FULL OUTER JOIN ");

            public final String value;

            JoinType(String operator) {
                this.value = operator;
            }
        }

        JoinType join;
        String table2;
        String t1ColumnName;
        String t2ColumnName;
    }

    @Getter
    protected enum OrderType {
        DESC(" DESC"),
        ASC(" ASC");

        public final String value;

        OrderType(String operator) {
            this.value = operator;
        }
    }

    @Getter
    public enum Operator {
        NO_LOCK(" WITH (NOLOCK) "),
        IS(" = "),
        IS_NOT(" != "),
        IS_NOT_EQUAL(" <> "),
        IS_GREATER_THAN(" > "),
        IS_MINOR_THAN(" < "),
        IS_GREATER_THAN_OR_EQUAL(" >= "),
        IS_LESS_THAN_OR_EQUAL(" <= "),
        IS_LIKE(" LIKE "),
        IS_NOT_LIKE(" NOT LIKE "),
        IS_IN(" IN "),
        IS_NOT_IN(" NOT IN "),
        IS_NULL(" IS NULL "),
        IS_NOT_NULL(" IS NOT NULL "),
        EXISTS(" EXISTS ({column})"),
        NOT_EXISTS(" NOT EXISTS ({column})");

        public final String value;

        Operator(String operator) {
            this.value = operator;
        }
    }

    @Data
    @AllArgsConstructor
    private static class OrderBy {
        String columnName;
        OrderType order;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QueryConditions {
        Statement statement;
        String column;
        Operator operator;
        String value;
    }

    @Data
    public static class QueryAttributes {
        private String select;
        private boolean noLock;
        String from;
        List<JoinClause> joins;
        List<QueryConditions> conditions;
        String groupBy;
        String having;
        OrderBy orderBy;

        public QueryAttributes() {
            this.joins = new ArrayList<>();
            this.conditions = new ArrayList<>();
        }

        public void setSelect(SelectionType select){
            this.select = select.value;
        }

        public void selectTopAll(int maxValues){
            this.select = SelectionType.TOP_ALL.value.replace("{number}", String.valueOf(maxValues));
        }

        public void selectTopFromColumn(int maxValues, String column){
            this.select = SelectionType.TOP_COLUMN.value.replace("{number}", String.valueOf(maxValues)).replace("{column}", column);
        }

        public void selectAll(){
            this.select = SelectionType.ALL.value;
        }

        public void selectDistinct(String column){
            this.select = SelectionType.DISTINCT.value.replace("{column}", column);
        }

        public void countAll(){
            this.select = SelectionType.COUNT_ALL.value;
        }

        public void countOf(String column){
            this.select = SelectionType.COUNT_OF.value.replace("{column}", column);
        }

        public void setOrderBy(String columnName, String type){
            this.orderBy = new OrderBy(columnName, OrderType.valueOf(type));
        }

        public void setOrderBy(String columnName){
            this.orderBy = new OrderBy(columnName, OrderType.DESC);
        }

        public void setWhere(String keyAttribute, Operator operator, String value){
            this.conditions.add(new QueryConditions(WHERE, keyAttribute, operator, value));
        }

        public void setAnd(String keyAttribute, Operator operator, String value){
            this.conditions.add(new QueryConditions(AND, keyAttribute, operator, value));
        }

        public void innerJoin(String table2, String t1ColumnName, String t2ColumnName){
            this.joins.add(new JoinClause(JoinClause.JoinType.INNER_JOIN, table2, t1ColumnName, t2ColumnName));
        }

        public void rightJoin(String table2, String t1ColumnName, String t2ColumnName){
            this.joins.add(new JoinClause(JoinClause.JoinType.RIGHT_JOIN, table2, t1ColumnName, t2ColumnName));
        }

        public void leftJoin(String table2, String t1ColumnName, String t2ColumnName){
            this.joins.add(new JoinClause(JoinClause.JoinType.LEFT_JOIN, table2, t1ColumnName, t2ColumnName));
        }
    }

    /**
     * Builds a SQL query string based on the provided {@link QueryAttributes}.
     *
     * <p>This method constructs a SQL query string using various attributes defined
     * in the {@link QueryAttributes} object. The generated query includes the SELECT,
     * FROM, JOIN, WHERE, GROUP BY, HAVING, and ORDER BY clauses as specified.
     *
     * @param queryAttributes an instance of {@link QueryAttributes} containing the
     *                        various parts of the SQL query to be built
     * @return a {@link String} representing the complete SQL query
     */
    public static String sqlQueryBuilder(QueryAttributes queryAttributes) {
        String baseQuery = SELECT.value + queryAttributes.getSelect() + "\n"
                + FROM.value + queryAttributes.getFrom()
                + (queryAttributes.isNoLock() ? Operator.NO_LOCK.value : "") + "\n";
        StringBuilder queryBuilder = new StringBuilder(baseQuery);

        // Append Join selections
        List<JoinClause> joins = queryAttributes.getJoins();
        if (joins != null && !joins.isEmpty()) {
            for (JoinClause join : joins) {
                queryBuilder
                        .append(join.getJoin().value)
                        .append(join.getTable2())
                        .append(" ON ")
                        .append(join.getT1ColumnName())
                        .append(" = ")
                        .append(join.getT2ColumnName())
                        .append("\n");
            }
        }

        // Append filtering conditions
        List<QueryConditions> conditions = queryAttributes.getConditions();
        if (conditions != null && !conditions.isEmpty()) {
            for (QueryConditions entry : conditions) {
                String value = contextCheck(entry.getValue());
                if (entry.getValue().contains("CONTEXT-")) value = String.format("'%s'", value);
                if (value.contains("WITHIN-hs-"))
                    value = String.format("DATEADD(HOUR, -%s, GETDATE())", value.replace("WITHIN-hs-", ""));
                if (value.contains("WITHIN-min-"))
                    value = String.format("DATEADD(MINUTE, -%s, GETDATE())", value.replace("WITHIN-min-", ""));

                queryBuilder
                        .append(entry.getStatement().value)
                        .append(entry.getColumn())
                        .append(entry.getOperator().value);
                if (!value.contains("no-value")) queryBuilder.append(value);
                queryBuilder.append("\n");
            }
        }

        // Append group by conditions
        if (queryAttributes.getGroupBy() != null)
            queryBuilder.append(GROUP_BY.value).append(queryAttributes.getGroupBy()).append("\n");

        // Append having clause
        if (queryAttributes.getHaving() != null)
            queryBuilder.append(HAVING.value.replace("{condition}", queryAttributes.getHaving())).append("\n");

        // Append order conditions
        if (queryAttributes.getOrderBy() != null) {
            queryBuilder
                    .append(ORDER_BY.value)
                    .append(queryAttributes.getOrderBy().getColumnName())
                    .append(queryAttributes.getOrderBy().getOrder().value);

        }

        String sqlQuery = queryBuilder.toString();
        log.info("Query created:\n" + sqlQuery);

        return sqlQuery;
    }

    /**
     * Generates a SQL query to check if the results of a given input query exist in another table.
     *
     * <p>This method constructs a SQL query to count the number of records from the results of
     * the input query that do not exist in the specified table based on the given column mappings.
     *
     * @param inputQuery   the SQL query whose results need to be checked against the table.
     * @param table2Name   the name of the table where the existence of the query results needs to be checked.
     * @param t1ColumnName the column name in the results of the input query to be checked.
     * @param t2ColumnName the column name in the specified table to be checked against.
     * @return a SQL query string that counts the number of records from the input query that do not exist in the specified table.
     */
    public static String checkIfQueryResultsExistsOnTable(QueryAttributes inputQuery, String table2Name, String t1ColumnName, String t2ColumnName) {
        StringBuilder auxQuery = new StringBuilder();
        auxQuery
                .append(SELECT.value).append("1")
                .append(FROM.value).append(table2Name)
                .append(WHERE.value).append(table2Name).append(".").append(t2ColumnName)
                .append(IS.value).append("result.").append(t1ColumnName);

        String sqlQuery = SELECT.value + SelectionType.COUNT_ALL.value +
                FROM.value + String.format("(%s) AS result ", sqlQueryBuilder(inputQuery)) +
                WHERE.value + NOT_EXISTS.value.replace("{column}", auxQuery);
        log.info("Query: " + sqlQuery);

        return sqlQuery;
    }
}
