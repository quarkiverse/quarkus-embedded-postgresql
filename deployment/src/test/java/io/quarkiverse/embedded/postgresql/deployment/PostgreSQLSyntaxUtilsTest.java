package io.quarkiverse.embedded.postgresql.deployment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkiverse.embedded.postgresql.PostgreSQLSyntaxUtils;

class PostgreSQLSyntaxUtilsTest {

    @Test
    void sanitizeDbNameInvalid() {
        String sanitizeDbName = PostgreSQLSyntaxUtils.sanitizeDbName("database-2");
        Assertions.assertEquals(sanitizeDbName, "database_2");
    }

    @Test
    void sanitizeDbNameValid() {
        String sanitizeDbName = PostgreSQLSyntaxUtils.sanitizeDbName("database2_$$");
        Assertions.assertEquals(sanitizeDbName, "database2_$$");
    }
}
