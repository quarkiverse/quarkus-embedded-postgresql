package io.quarkiverse.embedded.postgresql;

import java.util.Collections;
import java.util.Map;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

public class EmbeddedPostgreSQLConfigSourceProvider implements ConfigSourceProvider {

    static final String QUARKUS_DATASOURCE_REACTIVE_URL = "quarkus.datasource.reactive.url";
    static final String QUARKUS_DATASOURCE_JDBC_URL = "quarkus.datasource.jdbc.url";
    static final String QUARKUS_DATASOURCE_USERNAME = "quarkus.datasource.username";
    static final String QUARKUS_DATASOURCE_PASSWORD = "quarkus.datasource.password";

    static final String DEFAULT_DATABASE = "postgres";
    static final String DEFAULT_REACTIVE_URL = "postgresql://localhost:%d/" + DEFAULT_DATABASE;
    static final String DEFAULT_JDBC_URL = "jdbc:postgresql://localhost:%d/" + DEFAULT_DATABASE;
    static final String DEFAULT_USERNAME = "postgres";
    static final String DEFAULT_PASSWORD = "postgres";

    private final int port;

    public EmbeddedPostgreSQLConfigSourceProvider(int port) {
        this.port = port;
    }

    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
        return Collections.singleton(new EmbeddedPostgreSQLConfigSource(
                Map.of(QUARKUS_DATASOURCE_REACTIVE_URL, String.format(DEFAULT_REACTIVE_URL, port),
                        QUARKUS_DATASOURCE_JDBC_URL, String.format(DEFAULT_JDBC_URL, port),
                        QUARKUS_DATASOURCE_USERNAME, DEFAULT_USERNAME,
                        QUARKUS_DATASOURCE_PASSWORD, DEFAULT_PASSWORD)));
    }
}
