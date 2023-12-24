package io.quarkiverse.embedded.postgresql.deployment;

import java.util.regex.Pattern;

public class PostgreSQLSyntaxUtils {

    //https://www.postgresql.org/docs/current/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_$]*");
    private static final String NOT_IDENTIFIER_PATTERN = "[^" + IDENTIFIER_PATTERN + "]";

    public static String sanitizeDbName(String datasourceName) {
        if (IDENTIFIER_PATTERN.matcher(datasourceName).matches()) {
            return datasourceName;
        }
        return datasourceName.replaceAll(NOT_IDENTIFIER_PATTERN, "_");
    }
}