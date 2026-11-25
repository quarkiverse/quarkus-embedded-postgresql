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
import io.quarkus.core.deployment.service.ServiceRegistrar;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.IsLocalDevelopment;
import io.quarkus.deployment.IsProduction;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.quarkus.vertx.http.runtime.RouteHandler;
import io.vertx.core.Vertx;

@BuildSteps(onlyIfNot = IsProduction.class, onlyIf = DevServicesConfig.Enabled.class)
public class PgAdminProcessor {

    private final static String CONTAINER_ID = "quarkus-embedded-postgresql-pgadmin";

    private final static String PROXY_SERVICE_NAME = "io.quarkiverse.embedded-postgresql.pgadmin-proxy";

    private final static String QUARKUS_EMBEDDED_POSTGRESQL_PORT = "quarkus.embedded.postgresql.port";

    private final static String PGPASS_TEMPLATE = "host.docker.internal:%s:postgres:postgres:postgres";

    private static final Logger log = Logger.getLogger(PgAdminProcessor.class);

    @BuildStep(onlyIf = IsLocalDevelopment.class)
    void registerProxy(
            ServiceRegistrar serviceRegistrar,
            BuildProducer<RouteBuildItem> routes,
            PgAminUiConfig config,
            PgAdminConfigBuildItem pgAdminConfigBuildItem,
            NonApplicationRootPathBuildItem frameworkRoot) {
        if (!config.enabled()) {
            return;
        }
        int port = pgAdminConfigBuildItem.getPgAdminPort();

        serviceRegistrar.forService(RouteHandler.class, PROXY_SERVICE_NAME)
                .require(Vertx.class)
                .onStart((ctx, vertx) -> PgAdminUiProxy.createHandler(vertx, port));

        // RouteBuildItem still only accepts a recorder-style handler proxy; Quarkus 4.0 offers no
        // non-deprecated alternative yet (core extensions use this same bridge).
        @SuppressWarnings("deprecation")
        RouteHandler handler = serviceRegistrar.getRecorderProxy(RouteHandler.class, PROXY_SERVICE_NAME);

        routes.produce(frameworkRoot.routeBuilder()
                .management()
                .route("pgadmin*")
                .displayOnNotFoundPage("pgAdmin UI")
                .handler(handler)
                .build());

        routes.produce(RouteBuildItem.builder()
                .route("/pgadmin*")
                .displayOnNotFoundPage("pgAdmin UI")
                .handler(handler)
                .build());
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    DevServicesResultBuildItem startPgAdminContainer(LaunchModeBuildItem launchMode,
            EmbeddedPostgreSQLDevServicesConfigBuildItem pgBuildConfig,
            BuildProducer<PgAdminConfigBuildItem> pgAdminConfigProducer,
            PgAminUiConfig pgAppConfig) {
        if (launchMode.isNotLocalDevModeType()) {
            log.info("EXITING: PgAdmin is only available in local development mode.");
            return null;
        }
        if (!pgAppConfig.enabled()) {
            log.info("EXITING: PgAdmin is disabled in the configuration.");
            return null;
        }

        String serversJson = generateServersJson(
                Integer.parseInt(pgBuildConfig.getConfig().get(QUARKUS_EMBEDDED_POSTGRESQL_PORT)));
        String pgPass = generatePgPass(
                Integer.parseInt(pgBuildConfig.getConfig().get(QUARKUS_EMBEDDED_POSTGRESQL_PORT)));

        Path serversJsonPath;
        Path pgpassPath;
        try {
            serversJsonPath = Files.writeString(Paths.get("servers.json"), serversJson);
            pgpassPath = Files.writeString(Paths.get("pgpass"), pgPass);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write servers.json, pgpass or config_local.py", e);
        }

        PgAdminContainer container = new PgAdminContainer(pgAppConfig.imageName(), serversJsonPath, pgpassPath);
        container.start();

        var config = Collections.singletonMap(
                "quarkus.pgadmin4.url",
                container.getHost() + ":" + container.getMappedPort(80));

        pgAdminConfigProducer.produce(new PgAdminConfigBuildItem(
                container.getHost(), container.getMappedPort(80)));

        // Quarkus 3's RunningDevService.toBuildItem() produced exactly this and discarded the closeable,
        // so the container lifecycle is unchanged (Testcontainers cleans it up when the JVM exits).
        return DevServicesResultBuildItem.discovered()
                .feature(CONTAINER_ID)
                .containerId(container.getContainerId())
                .config(config)
                .build();
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
