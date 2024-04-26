package io.quarkiverse.embedded.postgresql;

import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import javax.sql.DataSource;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

public class EmbeddedPostgreSQLDBUtils {

    public static void createDatabases(EmbeddedPostgres pg, Collection<String> dbNames,
            String userName) {
        pg.getDatabase(DEFAULT_USERNAME, DEFAULT_DATABASE);
        dbNames.forEach(ds -> createDatabase(pg.getPostgresDatabase(), ds, userName));
    }

    private static void createDatabase(final DataSource dataSource, final String sanitizedDbName, final String userName) {
        String createDbStatement = String.format(
                "SELECT 'CREATE DATABASE %s OWNER %s' as createQuery WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '%s')",
                sanitizedDbName, userName, sanitizedDbName);
        try (Connection connection = dataSource.getConnection();
                Statement stmt = connection.createStatement()) {
            ResultSet result = stmt.executeQuery(createDbStatement);
            if (result.next()) {
                stmt.executeUpdate(result.getString("createQuery"));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error creating DB " + sanitizedDbName, e);
        }
    }

}
