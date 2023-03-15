package io.quarkiverse.embedded.postgresql;

import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigSourceProvider.DEFAULT_DATABASE;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigSourceProvider.DEFAULT_PASSWORD;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigSourceProvider.DEFAULT_USERNAME;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;
import org.jboss.logging.Logger;

import io.quarkus.datasource.runtime.DataSourcesBuildTimeConfig;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres.Builder;

@Recorder
public class EmbeddedPostgreSQLRecorder {

    private static final Logger logger = Logger.getLogger(EmbeddedPostgreSQLRecorder.class);

    public RuntimeValue<StartupInfo> startPostgres(ShutdownContext shutdownContext,
            DataSourcesBuildTimeConfig dataSourcesBuildTimeConfig) throws IOException {
        Builder builder = EmbeddedPostgres.builder();
        ConfigProvider.getConfig().getOptionalValue("quarkus.embedded.postgresql.data.dir", String.class).ifPresent(path -> {
            logger.infov("Setting embedded postgresql data dir to {0}", path);
            builder.setDataDirectory(path);
            builder.setCleanDataDirectory(false);
        });

        EmbeddedPostgres pg = builder.start();
        Map<String, String> databases = createDatabases(pg, dataSourcesBuildTimeConfig, DEFAULT_USERNAME);
        logger.infov(
                "Embedded Postgres started at port \"{0,number,#}\" with database \"{1}\", user \"{2}\" and password \"{3}\"",
                pg.getPort(), DEFAULT_DATABASE, DEFAULT_USERNAME, DEFAULT_PASSWORD);
        shutdownContext.addShutdownTask(() -> {
            try {
                pg.close();
            } catch (IOException e) {
                logger.warn("Error shutting down embedded postgres", e);
            }
        });
        return new RuntimeValue<>(new StartupInfo(pg.getPort(), databases));
    }

    private Map<String, String> createDatabases(EmbeddedPostgres pg, DataSourcesBuildTimeConfig dataSourcesBuildTimeConfig,
            String userName) {
        pg.getDatabase(DEFAULT_USERNAME, DEFAULT_DATABASE);
        return dataSourcesBuildTimeConfig.namedDataSources.entrySet().stream()
                .filter(ds -> Objects.equals(ds.getValue().dbKind.get(), "postgresql"))
                .map(Map.Entry::getKey)
                .map(db -> Map.entry(db, createDatabase(pg.getPostgresDatabase(), db, userName)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private String createDatabase(final DataSource dataSource, final String dbName, final String userName) {
        Objects.requireNonNull(dbName);
        Objects.requireNonNull(userName);
        String sanitizedDbName = dbName.replace("-", "_");
        String createDbStatement = String.format("CREATE DATABASE %s OWNER %s", sanitizedDbName, userName);
        try (PreparedStatement stmt = dataSource.getConnection().prepareStatement(createDbStatement)) {
            stmt.executeUpdate();
            return sanitizedDbName;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating DB " + dbName, e);
        }
    }

    public RuntimeValue<ConfigSourceProvider> configSources(RuntimeValue<StartupInfo> info) {
        return new RuntimeValue<>(new EmbeddedPostgreSQLConfigSourceProvider(info.getValue()));
    }
}
