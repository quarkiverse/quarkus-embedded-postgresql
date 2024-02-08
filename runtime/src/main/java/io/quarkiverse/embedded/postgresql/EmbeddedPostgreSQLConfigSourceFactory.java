package io.quarkiverse.embedded.postgresql;

import static java.lang.String.format;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.datasource.runtime.DataSourcesBuildTimeConfig;
import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;
import io.smallrye.config.SmallRyeConfigBuilder;

public class EmbeddedPostgreSQLConfigSourceFactory
        implements ConfigSourceFactory.ConfigurableConfigSourceFactory<EmbeddedPostgreSQLConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedPostgreSQLConfigSourceFactory.class);
    private static final String QUARKUS_DATASOURCE_DB_KIND = "quarkus.datasource.db-kind";
    private static final String QUARKUS_DATASOURCE_DEVSERVICES_ENABLED = "quarkus.datasource.devservices.enabled";

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

    @Override
    public Iterable<ConfigSource> getConfigSources(ConfigSourceContext context, EmbeddedPostgreSQLConfig config) {
        Map<String, String> allConfigs = new HashMap<>();

        // Add default datasource
        allConfigs.put(QUARKUS_DATASOURCE_REACTIVE_URL, format(DEFAULT_REACTIVE_URL, config.port(), DEFAULT_DATABASE));
        allConfigs.put(QUARKUS_DATASOURCE_JDBC_URL, format(DEFAULT_JDBC_URL, config.port(), DEFAULT_DATABASE));
        allConfigs.put(QUARKUS_DATASOURCE_USERNAME, DEFAULT_USERNAME);
        allConfigs.put(QUARKUS_DATASOURCE_PASSWORD, DEFAULT_PASSWORD);

        // Add named datasources
        DataSourcesBuildTimeConfig dataSourcesRuntimeConfig = new SmallRyeConfigBuilder()
                .withSources(new ConfigSourceContext.ConfigSourceContextConfigSource(context))
                .withMapping(DataSourcesBuildTimeConfig.class)
                .withValidateUnknown(false)
                .build().getConfigMapping(DataSourcesBuildTimeConfig.class);
        dataSourcesRuntimeConfig.dataSources().entrySet().stream()
                .filter(e -> !e.getKey().equals("<default>"))
                .filter(ds -> Objects.equals(ds.getValue().dbKind().orElse(""), "postgresql"))
                .map(Map.Entry::getKey).forEach(ds -> {
                    allConfigs.put(format(QUARKUS_NAMED_DATASOURCE_REACTIVE_URL, ds),
                            format(DEFAULT_REACTIVE_URL, config.port(), PostgreSQLSyntaxUtils.sanitizeDbName(ds)));
                    allConfigs.put(format(QUARKUS_NAMED_DATASOURCE_JDBC_URL, ds),
                            format(DEFAULT_JDBC_URL, config.port(), PostgreSQLSyntaxUtils.sanitizeDbName(ds)));
                    allConfigs.put(format(QUARKUS_NAMED_DATASOURCE_USERNAME, ds), DEFAULT_USERNAME);
                    allConfigs.put(format(QUARKUS_NAMED_DATASOURCE_PASSWORD, ds), DEFAULT_PASSWORD);
                });

        // Disable devservices
        context.iterateNames().forEachRemaining(name -> {
            if (name.startsWith("quarkus.datasource.") && name.endsWith(".db-kind")
                    && "postgresql".equals(context.getValue(name).getValue())) {
                if (QUARKUS_DATASOURCE_DB_KIND.equals(name)) {
                    allConfigs.put(QUARKUS_DATASOURCE_DEVSERVICES_ENABLED, "false");
                } else {
                    String datasource = name.substring(0, name.indexOf("db-kind"));
                    String prop = datasource + "devservices.enabled";
                    allConfigs.put(prop, "false");
                }
            }
        });

        LOGGER.debug("Build Embedded PostgreSQL properties: {}", allConfigs);

        return Collections.singletonList(new EmbeddedPostgreSQLConfigSource(allConfigs));
    }
}
