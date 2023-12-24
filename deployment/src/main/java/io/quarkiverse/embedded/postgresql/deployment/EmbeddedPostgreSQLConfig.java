package io.quarkiverse.embedded.postgresql.deployment;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Allows configuring the embedded PostgreSQL server.
 */
@ConfigMapping(prefix = "quarkus.embedded.postgresql")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface EmbeddedPostgreSQLConfig {

    /**
     * Directory where persistence information is hold
     */
    @WithName("data.dir")
    Optional<String> dataDir();

    /**
     * How long PostgreSQL will have to start before it times out. Value is milliseconds.
     */
    @WithName("startup.wait")
    Optional<Long> startupWait();

    /**
     * Optionally configurable port for the postgresql server. If not set, 62537 is picked.
     */
    @WithDefault("62537")
    Optional<Integer> port();

}