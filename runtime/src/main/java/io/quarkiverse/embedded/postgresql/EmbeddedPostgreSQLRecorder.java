package io.quarkiverse.embedded.postgresql;

import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.DEFAULT_DATABASE;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.DEFAULT_PASSWORD;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.DEFAULT_USERNAME;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLDBUtils.createDatabases;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Map;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres.Builder;

@Recorder
public class EmbeddedPostgreSQLRecorder {

    private static final Logger logger = Logger.getLogger(EmbeddedPostgreSQLRecorder.class);

    public void startPostgres(ShutdownContext shutdownContext, int port, Map<String, String> dbNames) {
        Builder builder = EmbeddedPostgres.builder();
        Config config = ConfigProvider.getConfig();
        builder.setPort(port);
        builder.setConnectConfig("stringtype",
                config.getOptionalValue("quarkus.embedded.postgresql.stringtype", String.class).orElse("unspecified"));

        config.getOptionalValue("quarkus.embedded.postgresql.startup.wait", Long.class).ifPresent(
                timeout -> {
                    logger.infov("PG startup timeout set to {0}", timeout);
                    builder.setPGStartupWait(Duration.ofMillis(timeout));
                });

        config.getOptionalValue("quarkus.embedded.postgresql.data.dir", String.class).ifPresent(path -> {
            logger.infov("Setting embedded postgresql data dir to {0}", path);
            builder.setDataDirectory(path);
            builder.setCleanDataDirectory(false);
        });

        EmbeddedPostgres pg;
        try {
            pg = builder.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
        createDatabases(pg, dbNames.values(), DEFAULT_USERNAME);
    }

}
