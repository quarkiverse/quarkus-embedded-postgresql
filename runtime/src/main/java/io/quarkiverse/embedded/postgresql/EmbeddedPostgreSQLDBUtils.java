package io.quarkiverse.embedded.postgresql;

import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.DEFAULT_DATABASE;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.DEFAULT_PASSWORD;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.DEFAULT_USERNAME;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.sql.DataSource;

import org.jboss.logging.Logger;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres.Builder;

public class EmbeddedPostgreSQLDBUtils {

    private static final Logger logger = Logger.getLogger(EmbeddedPostgreSQLDBUtils.class);

    private static final List<String> preparedListenAddresses = List.of(
            String.format("host    %s     %s             0.0.0.0/0            trust", DEFAULT_USERNAME, DEFAULT_DATABASE),
            String.format("host    %s     %s             ::/0                 trust", DEFAULT_USERNAME, DEFAULT_DATABASE));

    private static void createDatabases(EmbeddedPostgres pg, Collection<String> dbNames,
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

    public static EmbeddedPostgres startPostgres(Optional<Integer> port, Optional<String> listenAddress,
            Map<String, String> dbNames, String stringType,
            Optional<Long> startUpWait, Optional<String> dataDir) {
        Builder builder = EmbeddedPostgres.builder();
        builder.setConnectConfig("stringtype", stringType);
        port.ifPresent(p -> {
            logger.infov("PG port set to {0}", p);
            builder.setPort(p);
        });

        listenAddress.ifPresent(addr -> {
            if (!(addr.contains("localhost") || addr.contains("127.0.0.1") && !addr.contains("*"))) {
                addr = addr + ",localhost";
            }
            logger.infov("PG listen_addresses set to {0}", addr);
            builder.setServerConfig("listen_addresses", addr);
        });

        startUpWait.ifPresent(
                timeout -> {
                    logger.infov("PG startup timeout set to {0}", timeout);
                    builder.setPGStartupWait(Duration.ofMillis(timeout));
                });
        dataDir.ifPresent(path -> {
            logger.infov("Setting embedded postgresql data dir to {0}", path);
            builder.setDataDirectory(path);
            builder.setCleanDataDirectory(false);
        });
        try {
            EmbeddedPostgres pg = builder.start();
            logger.infov(
                    "Embedded Postgres started at port \"{0,number,#}\" with database \"{1}\", user \"{2}\" and password \"{3}\"",
                    pg.getPort(), DEFAULT_DATABASE, DEFAULT_USERNAME, DEFAULT_PASSWORD);
            createDatabases(pg, dbNames.values(), DEFAULT_USERNAME);
            patchPgHbaConfOnNonLocalListenAddr(pg, listenAddress);
            return pg;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    public static void close(Closeable pg) {
        try {
            logger.info("Embedded Postgres shutting down...");
            pg.close();
        } catch (IOException e) {
            logger.warn("Error closing Embedded Postgres", e);
        }
    }

    private EmbeddedPostgreSQLDBUtils() {
    }

    private static void patchPgHbaConfOnNonLocalListenAddr(EmbeddedPostgres pg, Optional<String> listenAddress) {
        listenAddress
                .ifPresent(addr -> {
                    try (Connection conn = pg.getPostgresDatabase().getConnection()) {
                        try (PreparedStatement stmt = conn.prepareStatement("SHOW hba_file");
                                ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                String hbaFilePath = rs.getString(1);
                                Path hbaPath = Paths.get(hbaFilePath);
                                if (Files.exists(hbaPath)) {
                                    Set<String> lines = new LinkedHashSet<>(Files.readAllLines(hbaPath));
                                    lines.addAll(preparedListenAddresses);
                                    Files.write(hbaPath, lines);
                                    PreparedStatement reloadStmt = conn.prepareStatement("SELECT pg_reload_conf()");
                                    reloadStmt.executeQuery();
                                }
                            }
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

}
