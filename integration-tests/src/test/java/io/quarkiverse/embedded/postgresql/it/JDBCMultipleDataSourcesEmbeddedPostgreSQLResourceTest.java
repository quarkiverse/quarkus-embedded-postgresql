package io.quarkiverse.embedded.postgresql.it;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(JDBCMultipleDataSourcesTestProfile.class)
public class JDBCMultipleDataSourcesEmbeddedPostgreSQLResourceTest extends EmbeddedPostgreSQLResourceTest {
}
