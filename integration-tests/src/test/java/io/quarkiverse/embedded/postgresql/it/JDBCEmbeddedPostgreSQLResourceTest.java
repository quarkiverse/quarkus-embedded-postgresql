package io.quarkiverse.embedded.postgresql.it;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(JDBCTestProfile.class)
public class JDBCEmbeddedPostgreSQLResourceTest extends EmbeddedPostgreSQLResourceTest {

}
