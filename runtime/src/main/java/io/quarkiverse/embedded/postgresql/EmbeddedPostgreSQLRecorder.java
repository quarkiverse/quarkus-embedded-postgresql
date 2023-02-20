package io.quarkiverse.embedded.postgresql;

import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigSourceProvider.*;

import java.io.IOException;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;
import org.jboss.logging.Logger;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres.Builder;

@Recorder
public class EmbeddedPostgreSQLRecorder {

    private static final Logger logger = Logger.getLogger(EmbeddedPostgreSQLRecorder.class);

    public RuntimeValue<Integer> startPostgres(ShutdownContext shutdownContext) throws IOException {
        Builder builder = EmbeddedPostgres.builder();
        ConfigProvider.getConfig().getOptionalValue("quarkus.embedded.postgresql.data.dir", String.class).ifPresent(path -> {
            logger.infov("Setting embedded postgresql data dir to {0}", path);
            builder.setDataDirectory(path);
            builder.setCleanDataDirectory(false);
        });
        EmbeddedPostgres pg = builder.start();
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
        return new RuntimeValue<>(pg.getPort());
    }

    public RuntimeValue<ConfigSourceProvider> configSources(RuntimeValue<Integer> port) {
        return new RuntimeValue<>(new EmbeddedPostgreSQLConfigSourceProvider(port.getValue()));
    }
}
