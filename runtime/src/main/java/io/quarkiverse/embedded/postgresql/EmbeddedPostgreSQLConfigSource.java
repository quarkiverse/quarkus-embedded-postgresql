package io.quarkiverse.embedded.postgresql;

import java.util.Map;

import io.smallrye.config.common.MapBackedConfigSource;

public class EmbeddedPostgreSQLConfigSource extends MapBackedConfigSource {

    private static final long serialVersionUID = 1L;
    // this is higher than the file system or jar ordinals, but lower than env vars
    // https://github.com/quarkusio/quarkus/pull/14777
    private static final int ORDINAL = 270;

    public EmbeddedPostgreSQLConfigSource(Map<String, String> propertyMap) {
        super("embedded-postgresql", propertyMap, ORDINAL, false);
    }
}
