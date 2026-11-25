package io.quarkiverse.embedded.postgresql;

import java.util.Map;
import java.util.Optional;

import io.quarkus.core.StartContext;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

/**
 * Runtime start/stop logic for the embedded PostgreSQL server, invoked from the service registered
 * by the deployment processor (replaces the former {@code @Recorder}).
 */
public final class EmbeddedPostgreSQLService {

    private EmbeddedPostgreSQLService() {
    }

    /**
     * Starts the embedded PostgreSQL server and registers its shutdown with the service lifecycle.
     * Nullable parameters mean "not configured".
     */
    public static void start(StartContext ctx, int port, String listenAddress, Map<String, String> dbNames,
            String stringType, Long startUpWait, String dataDir) {
        EmbeddedPostgres pg = EmbeddedPostgreSQLDBUtils.startPostgres(Optional.of(port), Optional.ofNullable(listenAddress),
                dbNames, stringType, Optional.ofNullable(startUpWait), Optional.ofNullable(dataDir));
        ctx.onStop(() -> EmbeddedPostgreSQLDBUtils.close(pg));
    }
}
