package io.quarkiverse.embedded.postgresql.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusDevModeTest;

public class PostgreSQLFilesystemStorageTest {

    @RegisterExtension
    static final QuarkusDevModeTest app = new QuarkusDevModeTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(EmbeddedPostgreSQLResource.class)
                    .addPackage("io.quarkiverse.embedded.postgresql.it")
                    .addAsResource("db/migration/V1.0.0__embedded_PostgreSQL.sql")
                    .addAsResource("application.properties")
                    .addAsManifestResource(new StringAsset("Dummy file"), "resources/file.txt"))
            //make embedded PostgreSql to use the filesystem
            .setBuildSystemProperty("quarkus.embedded.postgresql.data.dir",
                    "${java.io.tmpdir}/tests/PostgreSQLFilesystemStorageTest/postgresql-embedded-test");

    @Test
    public void testWithResourceChange() throws InterruptedException {
        //force restart
        app.modifyResourceFile("META-INF/resources/file.txt", s -> "Change on the dummy file");
        testGet();
    }

    @Test
    public void testRequestWithNoResourceChange() {
        testGet();
    }

    private void testGet() {
        given()
                .when().get("/inmemory-postgresql")
                .then()
                .statusCode(200)
                .body("size()", is(4));
    }
}
