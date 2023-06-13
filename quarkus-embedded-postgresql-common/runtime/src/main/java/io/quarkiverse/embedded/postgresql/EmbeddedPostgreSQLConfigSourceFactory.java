package io.quarkiverse.embedded.postgresql;

import java.util.Collections;
import java.util.Properties;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;
import io.smallrye.config.PropertiesConfigSource;

public class EmbeddedPostgreSQLConfigSourceFactory implements ConfigSourceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedPostgreSQLConfigSourceFactory.class);
    private static final String QUARKUS_DATASOURCE_DB_KIND = "quarkus.datasource.db-kind";
    private static final String QUARKUS_DATASOURCE_DEVSERVICES_ENABLED = "quarkus.datasource.devservices.enabled";

    @Override
    public Iterable<ConfigSource> getConfigSources(ConfigSourceContext context) {
        Properties pros = new Properties();
        context.iterateNames().forEachRemaining(name -> {
            if (name.startsWith("quarkus.datasource.") && name.endsWith(".db-kind")
                    && "postgresql".equals(context.getValue(name).getValue())) {
                if (QUARKUS_DATASOURCE_DB_KIND.equals(name)) {
                    pros.put(QUARKUS_DATASOURCE_DEVSERVICES_ENABLED, "false");
                } else {
                    String datasource = name.substring(0, name.indexOf("db-kind"));
                    String prop = datasource + "devservices.enabled";
                    pros.put(prop, "false");
                }
            }
        });

        LOGGER.debug("Build Embedded PostgreSQL properties: {}", pros);

        return Collections.singletonList(new PropertiesConfigSource(pros, "Build Embedded PostgreSQL"));
    }
}
