package io.quarkiverse.embedded.postgresql.it;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusIntegrationTest
@TestProfile(JDBCMultipleDataSourcesTestProfile.class)
public class JDBCMultipleDataSourcesEmbeddedPostgreSQLResourceIT extends EmbeddedPostgreSQLResourceTest {
}
