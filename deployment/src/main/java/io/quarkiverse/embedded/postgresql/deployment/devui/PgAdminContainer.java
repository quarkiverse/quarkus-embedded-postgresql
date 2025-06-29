package io.quarkiverse.embedded.postgresql.deployment.devui;

import java.nio.file.Path;
import java.time.Duration;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public final class PgAdminContainer extends GenericContainer<PgAdminContainer> {

    private static final String DEV_SERVICE_LABEL = "quarkus-dev-service-mailpit";
    private static final String DEV_SERVICE_NAME = "pgAdminUI";

    public PgAdminContainer(String dockerImageName, Path serversJsonPath, Path pgpassPath) {
        super(DockerImageName.parse(dockerImageName));
        super.withLabel(DEV_SERVICE_LABEL, DEV_SERVICE_NAME);

        withExposedPorts(80);

        withEnv("PGADMIN_DEFAULT_EMAIL", "admin@admin.com");
        withEnv("PGADMIN_DEFAULT_PASSWORD", "admin");
        withEnv("PGADMIN_CONFIG_SERVER_MODE", "False");
        withEnv("PGADMIN_CONFIG_MASTER_PASSWORD_REQUIRED", "False");
        withEnv("PGADMIN_REPLACE_SERVERS_ON_STARTUP", "True");
        withEnv("SCRIPT_NAME", "/pgadmin");

        withFileSystemBind(
                serversJsonPath.toAbsolutePath().toString(),
                "/pgadmin4/servers.json",
                BindMode.READ_ONLY);
        withFileSystemBind(
                pgpassPath.toAbsolutePath().toString(),
                "/pgpass",
                BindMode.READ_ONLY);
        waitingFor(Wait.forHttp("/pgadmin/")
                .forStatusCode(200)
                .withStartupTimeout(Duration.ofMinutes(2)));
    }
}
