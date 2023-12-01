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

    /**
     * How long PostgreSQL will have to start before it times out. Value is milliseconds.
     */
    @ConfigItem(name = "startup.wait")
    public Optional<Long> startupWait;

    /**
     * Optionally configurable port for the postgresql server. If not set, a random port is picked.
     */
    @ConfigItem(name = "port")
    public Optional<Integer> port;

}
