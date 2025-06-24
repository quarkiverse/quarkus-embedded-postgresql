package io.quarkiverse.embedded.postgresql.deployment.devui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import org.jboss.logging.Logger;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.quarkiverse.embedded.postgresql.deployment.EmbeddedPostgreSQLDevServicesConfigBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;

public class PgAdminProcessor {

    private final static String CONTAINER_ID = "quarkus-embedded-postgresql-pgadmin";

    private final static String QUARKUS_EMBEDDED_POSTGRESQL_PORT = "quarkus.embedded.postgresql.port";

    private final static String PGPASS_TEMPLATE = "host.docker.internal:%s:postgres:postgres:postgres";

    private final static String DOCKER_IMAGE_NAME = "dpage/pgadmin4:9.4.0";

    private static final Logger log = Logger.getLogger(PgAdminProcessor.class);

    @BuildStep(onlyIf = IsDevelopment.class)
    DevServicesResultBuildItem startPgAdminContainer(LaunchModeBuildItem launchMode,
            EmbeddedPostgreSQLDevServicesConfigBuildItem pgConfig,
            BuildProducer<PgAdminConfigBuildItem> pgAdminConfigProducer) {
        if (!launchMode.getLaunchMode().isDevOrTest()) {
            return null;
        }

        log.info("Starting PgAdmin container...");

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

        DockerImageName imageName = DockerImageName.parse(DOCKER_IMAGE_NAME);

        GenericContainer<?> container = new GenericContainer<>(imageName)
                .withExposedPorts(80)
                .withEnv("PGADMIN_DEFAULT_EMAIL", "admin@admin.com")
                .withEnv("PGADMIN_DEFAULT_PASSWORD", "admin")
                .withEnv("PGADMIN_CONFIG_SERVER_MODE", "False")
                .withEnv("PGADMIN_CONFIG_MASTER_PASSWORD_REQUIRED", "False")
                .withEnv("PGADMIN_REPLACE_SERVERS_ON_STARTUP", "True")
                .withFileSystemBind(
                        serversJsonPath.toAbsolutePath().toString(),
                        "/pgadmin4/servers.json",
                        BindMode.READ_ONLY)
                .withFileSystemBind(
                        pgpassPath.toAbsolutePath().toString(),
                        "/pgpass",
                        BindMode.READ_ONLY)
                .withLogConsumer(outputFrame -> {
                    log.info("[PgAdmin4] " + outputFrame.getUtf8String());
                })
                .waitingFor(Wait.forHttp("/")
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(2)));

        container.start();

        var config = Collections.singletonMap(
                "quarkus.pgadmin4.url",
                container.getHost() + ":" + container.getMappedPort(80));

        pgAdminConfigProducer.produce(new PgAdminConfigBuildItem(
                config.get("quarkus.pgadmin4.url"),
                String.valueOf(container.getMappedPort(80))));

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
