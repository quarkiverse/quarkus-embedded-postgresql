package io.quarkiverse.embedded.postgresql.deployment.devui;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.pgadmin-ui")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface PgAminUiConfig {

    String DEFAULT_IMAGE = "dpage/pgadmin4:9.4.0";

    /**
     * Enable or disable the PgAdmin UI.
     *
     * @return true if enabled, false otherwise
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * The name of the Docker image to use for PgAdmin.
     *
     * @return the Docker image name
     */
    @WithDefault(DEFAULT_IMAGE)
    String imageName();

}
