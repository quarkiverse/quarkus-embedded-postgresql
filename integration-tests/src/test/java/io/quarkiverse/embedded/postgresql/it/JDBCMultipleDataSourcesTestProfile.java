package io.quarkiverse.embedded.postgresql.it;

import io.quarkus.test.junit.QuarkusTestProfile;

public class JDBCMultipleDataSourcesTestProfile implements QuarkusTestProfile {

    public String getConfigProfile() {
        return "jdbc-multiple-datasources";
    }
}
