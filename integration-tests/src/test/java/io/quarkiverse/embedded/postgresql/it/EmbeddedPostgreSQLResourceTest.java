package io.quarkiverse.embedded.postgresql.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import io.restassured.http.ContentType;

public abstract class EmbeddedPostgreSQLResourceTest {

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

        given()
                .when().contentType(ContentType.JSON)
                .accept(ContentType.JSON).body(Collections.singletonMap("name", "javierito")).put("/inmemory-postgresql/1")
                .then()
                .statusCode(201);

        given()
                .when().contentType(ContentType.JSON)
                .accept(ContentType.JSON).body(Collections.singletonMap("name", "javierito")).put("/inmemory-postgresql/1")
                .then()
                .statusCode(200);

        given()
                .when().get("/inmemory-postgresql")
                .then()
                .statusCode(200)
                .body(
                        containsString("javierito"),
                        containsString("test2"),
                        containsString("test3"),
                        containsString("test4"));

    }
}
