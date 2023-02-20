package io.quarkiverse.embedded.postgresql;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(phase = ConfigPhase.RUN_TIME, name = "embedded.postgresql")
public class EmbeddedPostgreSQLConfiguration {

    /**
     * Directory where persistence information is hold
     */
    @ConfigItem(name = "data.dir")
    public Optional<String> dataDir;
}
