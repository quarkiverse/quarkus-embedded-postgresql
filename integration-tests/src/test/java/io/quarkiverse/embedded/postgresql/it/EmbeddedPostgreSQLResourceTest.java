package io.quarkiverse.embedded.postgresql.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class EmbeddedPostgreSQLResourceTest {

    @Test
    public void testListAll() {
        // List all, should have all the database has initially
        given()
                .when().get("/inmemory-postgresql")
                .then()
                .statusCode(200)
                .body(
                        containsString("test1"),
                        containsString("test2"),
                        containsString("test3"),
                        containsString("test4"));

        // Delete the test1
        given()
                .when().delete("/inmemory-postgresql/1")
                .then()
                .statusCode(204);

        // List all, test1 should be missing now
        given()
                .when().get("/inmemory-postgresql")
                .then()
                .statusCode(200)
                .body(
                        not(containsString("test1")),
                        containsString("test2"),
                        containsString("test3"),
                        containsString("test4"));
    }
}
