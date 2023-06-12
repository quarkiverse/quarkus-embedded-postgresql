package io.quarkiverse.embedded.postgresql.it;

import io.quarkus.test.junit.QuarkusTestProfile;

public class MultipleDataSourcesTestProfile implements QuarkusTestProfile {

    public String getConfigProfile() {
        return "multiple-datasources";
    }
}
