package io.quarkiverse.embedded.postgresql;

import java.util.Map;
import java.util.Optional;

import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

@Recorder
public class EmbeddedPostgreSQLRecorder {

    public void startPostgres(ShutdownContext shutdownContext, int port, Optional<String> listenAddress,
            Map<String, String> dbNames,
            String stringType, Optional<Long> startUpWait, Optional<String> dataDir) {
        EmbeddedPostgres pg = EmbeddedPostgreSQLDBUtils.startPostgres(Optional.of(port), listenAddress, dbNames,
                stringType,
                startUpWait, dataDir);
        shutdownContext.addShutdownTask(() -> EmbeddedPostgreSQLDBUtils.close(pg));
    }
}
