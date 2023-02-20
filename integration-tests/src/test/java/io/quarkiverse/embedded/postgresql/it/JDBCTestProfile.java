package io.quarkiverse.embedded.postgresql.it;

import io.quarkus.test.junit.QuarkusTestProfile;

public class JDBCTestProfile implements QuarkusTestProfile {

    public String getConfigProfile() {
        return "jdbc";
    }
}
