package io.quarkiverse.embedded.postgresql.deployment.devui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import org.jboss.logging.Logger;

import io.quarkiverse.embedded.postgresql.deployment.EmbeddedPostgreSQLDevServicesConfigBuildItem;
import io.quarkiverse.embedded.postgresql.devui.PgAdminUiProxy;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.IsLocalDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.vertx.core.deployment.CoreVertxBuildItem;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;

public class PgAdminProcessor {

    private final static String CONTAINER_ID = "quarkus-embedded-postgresql-pgadmin";

    private final static String QUARKUS_EMBEDDED_POSTGRESQL_PORT = "quarkus.embedded.postgresql.port";

    private final static String PGPASS_TEMPLATE = "host.docker.internal:%s:postgres:postgres:postgres";

    private static final Logger log = Logger.getLogger(PgAdminProcessor.class);

    @BuildStep(onlyIf = IsLocalDevelopment.class)
    @Record(ExecutionTime.RUNTIME_INIT)
    void registerProxy(
            PgAdminUiProxy recorder,
            BuildProducer<RouteBuildItem> routes,
            PgAminUiConfig config,
            PgAdminConfigBuildItem pgAdminConfigBuildItem,
            NonApplicationRootPathBuildItem frameworkRoot,
            CoreVertxBuildItem coreVertx) {
        if (!config.enabled()) {
            return;
        }
        int port = pgAdminConfigBuildItem.getPgAdminPort();

        routes.produce(frameworkRoot.routeBuilder()
                .management()
                .route("pgadmin*")
                .handler(recorder.handler(coreVertx.getVertx(), port))
                .build());

        routes.produce(RouteBuildItem.builder()
                .route("/pgadmin*")
                .handler(recorder.handler(coreVertx.getVertx(), port))
                .build());
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    DevServicesResultBuildItem startPgAdminContainer(LaunchModeBuildItem launchMode,
            EmbeddedPostgreSQLDevServicesConfigBuildItem pgConfig,
            BuildProducer<PgAdminConfigBuildItem> pgAdminConfigProducer) {
        if (launchMode.isNotLocalDevModeType()) {
            log.info("EXITING: PgAdmin is only available in local development mode.");
            return null;
        }
        String serversJson = generateServersJson(
                Integer.parseInt(pgConfig.getConfig().get(QUARKUS_EMBEDDED_POSTGRESQL_PORT)));
        String pgPass = generatePgPass(
                Integer.parseInt(pgConfig.getConfig().get(QUARKUS_EMBEDDED_POSTGRESQL_PORT)));

        Path serversJsonPath;
        Path pgpassPath;
        try {
            serversJsonPath = Files.writeString(Paths.get("servers.json"), serversJson);
            pgpassPath = Files.writeString(Paths.get("pgpass"), pgPass);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write servers.json, pgpass or config_local.py", e);
        }

        PgAdminContainer container = new PgAdminContainer(serversJsonPath, pgpassPath);
        container.start();

        var config = Collections.singletonMap(
                "quarkus.pgadmin4.url",
                container.getHost() + ":" + container.getMappedPort(80));

        pgAdminConfigProducer.produce(new PgAdminConfigBuildItem(
                container.getHost(), container.getMappedPort(80)));

        return new DevServicesResultBuildItem.RunningDevService(CONTAINER_ID, container.getContainerId(),
                () -> {
                    log.info("Stopping PgAdmin container...");
                    try {
                        container.stop();
                        log.info("PgAdmin container stopped successfully.");
                    } catch (Exception e) {
                        log.error("Failed to stop PgAdmin container: " + e.getMessage(), e);
                    }
                }, config)
                .toBuildItem();
    }

    public String generateServersJson(int port) {
        JsonObjectBuilder serverBuilder = Json.createObjectBuilder()
                .add("Name", "Local dev")
                .add("Group", "Local")
                .add("Host", "host.docker.internal")
                .add("Port", port)
                .add("MaintenanceDB", "postgres")
                .add("Username", "postgres")
                .add("PassFile", "/pgpass")
                .add("SSLMode", "prefer")
                .add("SavePassword", true);

        JsonObject servers = Json.createObjectBuilder()
                .add("1", serverBuilder)
                .build();

        JsonObject root = Json.createObjectBuilder()
                .add("Servers", servers)
                .build();

        return root.toString();
    }

    public String generatePgPass(int port) {
        String pgPass = String.format(PGPASS_TEMPLATE, port);
        return pgPass;
    }
}
