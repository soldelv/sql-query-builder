# sql-query-builder
[![Maven Central](https://img.shields.io/maven-central/v/io.github.soldelv/sql-query-builder?color=brightgreen&label=sql-query-builder)](https://mvnrepository.com/artifact/io.github.soldelv/sql-query-builder/latest)

Java utility library for iteratively building SQL queries to handle database interactions.

The sql-query-builder library simplifies the creation of SQL queries in a programmatic way. It supports various SQL operations such as **SELECT**, **JOIN**, **WHERE**, **GROUP BY**, and **ORDER BY**, enabling the generation of dynamic and complex queries based on input parameters.

### Dependency
Add the following maven dependencies to your pom.xml file

```xml
<dependencies>
    <!-- SQL Query Builder -->
    <dependency>
        <groupId>io.github.soldelv</groupId>
        <artifactId>sql-query-builder</artifactId>
        <version>{latest_version}</version>
    </dependency>
</dependencies>
```

### Key Classes and Methods
- The **QueryBuilder** class is the core of the library, responsible for constructing SQL queries based on provided attributes.
- **sqlQueryBuilder(QueryAttributes queryAttributes)**: Generates the SQL query string.
- **QueryAttributes**: This class holds the components of an SQL query, such as SELECT, FROM, JOIN, WHERE, etc.
- **setSelect(String select)**: Defines the SELECT clause.
- **setFrom(String from)**: Sets the FROM clause.
- **setJoins(List<JoinClause> joins)**: Specifies the JOIN clauses.
- **setConditions(List<QueryConditions> conditions)**: Adds WHERE and AND conditions.

#### Advanced Usage
To further customize queries, you can:

- Use different JoinType (e.g., **INNER_JOIN**, **LEFT_JOIN**) to control how tables are joined.
- Chain multiple conditions with **AND** and **OR** statements.
- Group and order results with **GROUP BY** and **ORDER BY** clauses.
- **checkIfQueryResultsExistsOnTable(QueryAttributes inputQuery, String table2Name, String t1ColumnName, String t2ColumnName)**: Generates a query to check if results exist in a given table.

### General Usage
#### 1.Select the Type of Selection (SELECT Clause)
As this is the first statement in the query, here we should initialize the QueryAttributes, and after that you start by selecting all or distinct values from a specific column of a table.

```java
CustomerDataPlatform.QueryAttributes queryValues = new CustomerDataPlatform.QueryAttributes();
queryValues.selectAll();
```

You can run these types of selection:
```sql
SELECT * -- Select All
SELECT TOP(10) * -- Here you can use any number
SELECT TOP(10) columnName -- Select the first 10 records from specific column
COUNT({columnName}) AS singleResult
COUNT(*) AS singleResult -- Count all
DISTINCT {column}
```
By using the corresponding method:
```java
queryValues.selectAll();
queryValues.selectTopAll(maxValues);
queryValues.selectTopFromColumn(maxValues, columnName);
queryValues.countOf(columnName);
queryValues.countAll();
queryValues.selectDistinct(columnName);
```
#### 2.Specify the Source Table (FROM Clause)
The **FROM** clause is set automatically when you specify the table. Normally in steps you should send an enum of the mapped tables for CDP, so we have to get the string value of it.
```java
queryValues.setFrom("tableName");
```

#### 3.Add Filter Criteria (WHERE & AND Clause)
You'll provide the conditions, such as WHERE... AND ... as a list of QueryConditions. Basic Operators usage here is mandatory.
```java
queryValues.setConditions(conditions);
```
You can also set them individually by using setWhere() and setAnd()methods:
```java
queryValues.setWhere("user.name", IS, "'John'");
queryValues.setAnd("user.lastname", IS, "'Smith'");
```

#### 4.Add Grouping (Optional) (GROUP BY Clause)
If grouping is necessary, you can include it as queryValues.setGroupBy(columnName):

#### 5.Add Sorting (Optional) (ORDER BY Clause)
For order clause you have to set two parameters:

- orderType: Specifies the order type, which can be either "ASC" for ascending or "DESC" for descending.
- columnName: Specifies the column by which the query results should be ordered. If you don’t send this parameter it uses a DESC order by default.

### Basic Operators
The class QueryBuilder has Operator enum witch can be useful to compare column (or query results) values to a specific value:

```java
public static enum Operator {
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
```
### License

This project is licensed under the MIT License.