package io.quarkiverse.embedded.postgresql.it;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusIntegrationTest
@TestProfile(MultipleDataSourcesTestProfile.class)
public class ReactiveMultipleDataSourcesEmbeddedPostgreSQLResourceIT extends EmbeddedPostgreSQLResourceTest {

}
