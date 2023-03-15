package io.quarkiverse.embedded.postgresql;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        Map<String, String> defaultConfigs = Map.of(
                QUARKUS_DATASOURCE_REACTIVE_URL, String.format(DEFAULT_REACTIVE_URL, startupInfo.getPort(), DEFAULT_DATABASE),
                QUARKUS_DATASOURCE_JDBC_URL, String.format(DEFAULT_JDBC_URL, startupInfo.getPort(), DEFAULT_DATABASE),
                QUARKUS_DATASOURCE_USERNAME, DEFAULT_USERNAME,
                QUARKUS_DATASOURCE_PASSWORD, DEFAULT_PASSWORD);

        Map<String, String> namedDataSourcesConfigs = startupInfo.getDatabases().entrySet().stream()
                .flatMap(db -> Stream.of(Map.entry(
                        String.format(QUARKUS_NAMED_DATASOURCE_REACTIVE_URL, db.getKey()),
                        String.format(DEFAULT_REACTIVE_URL, startupInfo.getPort(), db.getValue())),
                        Map.entry(
                                String.format(QUARKUS_NAMED_DATASOURCE_JDBC_URL, db.getKey()),
                                String.format(DEFAULT_JDBC_URL, startupInfo.getPort(), db.getValue())),
                        Map.entry(
                                String.format(QUARKUS_NAMED_DATASOURCE_USERNAME, db.getKey()),
                                DEFAULT_USERNAME),
                        Map.entry(
                                String.format(QUARKUS_NAMED_DATASOURCE_PASSWORD, db.getKey()),
                                DEFAULT_PASSWORD)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, String> allConfigs = new HashMap<>(defaultConfigs);
        allConfigs.putAll(namedDataSourcesConfigs);
        return Collections.singleton(new EmbeddedPostgreSQLConfigSource(allConfigs));
    }
}
