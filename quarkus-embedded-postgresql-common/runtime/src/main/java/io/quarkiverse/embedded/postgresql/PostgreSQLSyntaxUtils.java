package io.quarkiverse.embedded.postgresql;

public class PostgreSQLSyntaxUtils {

    //https://www.postgresql.org/docs/current/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS
    private static final String NOT_IDENTIFIER_PATTERN = "[^[a-zA-Z_][a-zA-Z0-9_$]*]";

    public static String sanitizeDbName(String datasourceName) {
        return datasourceName.replaceAll(NOT_IDENTIFIER_PATTERN, "_");
    }
}
