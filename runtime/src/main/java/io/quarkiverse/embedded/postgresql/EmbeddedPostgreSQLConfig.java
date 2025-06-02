package io.quarkiverse.embedded.postgresql;

import java.util.Objects;
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
    Optional<Integer> port();

    /**
     * Optionally configurable host (listen_addresses) for the PostgreSQL server.
     * <p>
     * If not set, {@code localhost} is used by default.
     * Valid values include:
     * <ul>
     * <li>{@code localhost} — the loopback interfaces (127.0.0.1 and ::1)</li>
     * <li>{@code 0.0.0.0} — all IPv4 interfaces</li>
     * <li>{@code ::} — all IPv6 interfaces</li>
     * <li>{@code *} — all available IP interfaces (both IPv4 and IPv6)</li>
     * <li>A specific hostname or IP address, e.g. {@code db.mycompany.local} or {@code 192.168.1.100}</li>
     * <li>Multiple values separated by commas, e.g. {@code localhost,192.168.1.100}, {@code 0.0.0.0,::}</li>
     * </ul>
     *
     * @return an {@link Optional} containing the hostname(s) or IP address(es) to bind the PostgreSQL server.
     */
    Optional<String> listenAddress();

    /**
     * Set string type
     *
     * @see <a href="https://jdbc.postgresql.org/documentation/use/">...</a>
     */
    @WithDefault("unspecified")
    String stringType();

    static boolean isEqual(EmbeddedPostgreSQLConfig d1, EmbeddedPostgreSQLConfig d2) {
        if (!Objects.equals(d1.dataDir(), d2.dataDir())) {
            return false;
        }
        if (!Objects.equals(d1.startupWait(), d2.startupWait())) {
            return false;
        }
        if (!Objects.equals(d1.port(), d2.port())) {
            return false;
        }
        if (!Objects.equals(d1.stringType(), d2.stringType())) {
            return false;
        }
        return true;
    }
}