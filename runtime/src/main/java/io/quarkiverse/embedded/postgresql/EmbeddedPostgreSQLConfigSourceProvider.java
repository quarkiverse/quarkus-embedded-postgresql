package io.quarkiverse.embedded.postgresql;

import static java.lang.String.format;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

public class EmbeddedPostgreSQLConfigSourceProvider implements ConfigSourceProvider {

    static final String QUARKUS_DATASOURCE_REACTIVE_URL = "quarkus.datasource.reactive.url";
    static final String QUARKUS_DATASOURCE_JDBC_URL = "quarkus.datasource.jdbc.url";
    static final String QUARKUS_NAMED_DATASOURCE_REACTIVE_URL = "quarkus.datasource.\"%s\".reactive.url";
    static final String QUARKUS_NAMED_DATASOURCE_JDBC_URL = "quarkus.datasource.\"%s\".jdbc.url";
    static final String QUARKUS_DATASOURCE_USERNAME = "quarkus.datasource.username";
    static final String QUARKUS_DATASOURCE_PASSWORD = "quarkus.datasource.password";
    static final String QUARKUS_NAMED_DATASOURCE_USERNAME = "quarkus.datasource.\"%s\".username";
    static final String QUARKUS_NAMED_DATASOURCE_PASSWORD = "quarkus.datasource.\"%s\".password";
    static final String DEFAULT_DATABASE = "postgres";
    static final String DEFAULT_REACTIVE_URL = "postgresql://localhost:%d/%s";
    static final String DEFAULT_JDBC_URL = "jdbc:postgresql://localhost:%d/%s";
    static final String DEFAULT_USERNAME = "postgres";
    static final String DEFAULT_PASSWORD = "postgres";
    private StartupInfo startupInfo;

    public EmbeddedPostgreSQLConfigSourceProvider(StartupInfo startupInfo) {
        this.startupInfo = startupInfo;
    }

    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
        Map<String, String> allConfigs = new HashMap<>();

        startupInfo.getDatabases().forEach((key, value) -> {
            allConfigs.put(format(QUARKUS_NAMED_DATASOURCE_REACTIVE_URL, key),
                    format(DEFAULT_REACTIVE_URL, startupInfo.getPort(), value));
            allConfigs.put(format(QUARKUS_NAMED_DATASOURCE_JDBC_URL, key),
                    format(DEFAULT_JDBC_URL, startupInfo.getPort(), value));
            allConfigs.put(format(QUARKUS_NAMED_DATASOURCE_USERNAME, key), DEFAULT_USERNAME);
            allConfigs.put(format(QUARKUS_NAMED_DATASOURCE_PASSWORD, key), DEFAULT_PASSWORD);
        });

        allConfigs.put(QUARKUS_DATASOURCE_REACTIVE_URL, format(DEFAULT_REACTIVE_URL, startupInfo.getPort(), DEFAULT_DATABASE));
        allConfigs.put(QUARKUS_DATASOURCE_JDBC_URL, format(DEFAULT_JDBC_URL, startupInfo.getPort(), DEFAULT_DATABASE));
        allConfigs.put(QUARKUS_DATASOURCE_USERNAME, DEFAULT_USERNAME);
        allConfigs.put(QUARKUS_DATASOURCE_PASSWORD, DEFAULT_PASSWORD);

        return Collections.singleton(new EmbeddedPostgreSQLConfigSource(allConfigs));
    }
}
