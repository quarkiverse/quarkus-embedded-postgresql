package io.quarkiverse.embedded.postgresql.deployment;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfig;
import io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConnectionConfigurer;
import io.quarkiverse.embedded.postgresql.PostgreSQLSyntaxUtils;
import io.quarkus.agroal.spi.JdbcDriverBuildItem;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.processor.BuiltinScope;
import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.datasource.common.runtime.DatabaseKind;
import io.quarkus.datasource.runtime.DataSourcesBuildTimeConfig;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.*;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourcePatternsBuildItem;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.deployment.pkg.steps.NativeOrNativeSourcesBuild;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

class EmbeddedPostgreSQLProcessor {

    private static final Logger log = Logger.getLogger(EmbeddedPostgreSQLProcessor.class);

    private static final String FEATURE = "embedded-postgres";
    private static final String DEFAULT_DATABASE = "postgres";
    private static final String DEFAULT_REACTIVE_URL = "postgresql://localhost:%d/%s";
    private static final String DEFAULT_JDBC_URL = "jdbc:postgresql://localhost:%d/%s";
    private static final String DEFAULT_USERNAME = "postgres";
    private static final String DEFAULT_PASSWORD = "postgres";

    static volatile DevServicesResultBuildItem.RunningDevService devService;
    static volatile EmbeddedPostgreSQLConfig cfg;
    static volatile boolean first = true;

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public DevServicesResultBuildItem startPostgresDevService(
            DataSourcesBuildTimeConfig dataSourcesBuildTimeConfig,
            LaunchModeBuildItem launchMode,
            EmbeddedPostgreSQLConfig pgConfig,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem,
            BuildProducer<EmbeddedPostgreSQLDevServicesConfigBuildItem> pgBuildItemBuildProducer) {

        if (devService != null) {
            boolean shouldShutdownTheBroker = !EmbeddedPostgreSQLConfig.isEqual(cfg, pgConfig);
            if (!shouldShutdownTheBroker) {
                return devService.toBuildItem();
            }
            shutdown();
            cfg = null;
        }

        StartupLogCompressor compressor = new StartupLogCompressor(
                (launchMode.isTest() ? "(test) " : "") + "Embedded PostgreSQL Starting:",
                consoleInstalledBuildItem, loggingSetupBuildItem);
        try {
            devService = startPostgres(dataSourcesBuildTimeConfig, pgConfig);
            if (devService == null) {
                compressor.closeAndDumpCaptured();
            } else {
                compressor.close();
            }
        } catch (Throwable t) {
            compressor.closeAndDumpCaptured();
            throw new RuntimeException(t);
        }

        if (devService == null) {
            return null;
        }

        if (devService.isOwner()) {
            log.info("Embedded PostgreSQL started.");
            pgBuildItemBuildProducer.produce(new EmbeddedPostgreSQLDevServicesConfigBuildItem(devService.getConfig()));
        }

        // Configure the watch dog
        if (first) {
            first = false;
            Runnable closeTask = () -> {
                if (devService != null) {
                    shutdown();

                    log.info("Embedded PostgreSQL shut down.");
                }
                first = true;
                devService = null;
                cfg = null;
            };
            QuarkusClassLoader cl = (QuarkusClassLoader) Thread.currentThread().getContextClassLoader();
            ((QuarkusClassLoader) cl.parent()).addCloseTask(closeTask);
        }
        cfg = pgConfig;
        return devService.toBuildItem();
    }

    private DevServicesResultBuildItem.RunningDevService startPostgres(DataSourcesBuildTimeConfig dataSourcesBuildTimeConfig,
            EmbeddedPostgreSQLConfig postgreSQLConfig) throws IOException {

        if (postgreSQLConfig.port() <= 0) {
            // no mailer configured
            log.warn(
                    "Not starting Embedded PostgreSQL, as no 'quarkus.embedded.postgresql.port' has been configured.");
            return null;
        }

        EmbeddedPostgres.Builder builder = EmbeddedPostgres.builder();
        log.infov("PG port will be set to {0}", postgreSQLConfig.port());
        builder.setPort(postgreSQLConfig.port());

        postgreSQLConfig.startupWait().ifPresent(
                timeout -> {
                    log.infov("PG startup timeout set to {0}", timeout);
                    builder.setPGStartupWait(Duration.ofMillis(timeout));
                });

        postgreSQLConfig.dataDir().ifPresent(path -> {
            log.infov("Setting embedded postgresql data dir to {0}", path);
            builder.setDataDirectory(path);
            builder.setCleanDataDirectory(false);
        });

        EmbeddedPostgres pg = builder.start();
        log.infov(
                "Embedded Postgres started at port \"{0,number,#}\" with database \"{1}\", user \"{2}\" and password \"{3}\"",
                pg.getPort(), DEFAULT_DATABASE, DEFAULT_USERNAME, DEFAULT_PASSWORD);

        Map<String, String> devServerConfigMap = new LinkedHashMap<>();
        Config config = ConfigProvider.getConfig();
        for (String propertyName : config.getPropertyNames()) {
            if (propertyName.startsWith("quarkus.embedded.postgresql.")) {
                devServerConfigMap.put(propertyName, config.getConfigValue(propertyName).getValue());
            }
        }
        devServerConfigMap.putAll(createDatabases(pg, dataSourcesBuildTimeConfig, DEFAULT_USERNAME));

        return new DevServicesResultBuildItem.RunningDevService(FEATURE,
                null,
                pg,
                devServerConfigMap);
    }

    private void shutdown() {
        if (devService != null) {
            try {
                log.info("Embedded Postgres shutting down...");
                devService.close();
            } catch (Throwable e) {
                log.error("Failed to stop the Embedded Postgres server", e);
            } finally {
                devService = null;
            }
        }
    }

    @BuildStep
    void configureAgroalConnection(BuildProducer<AdditionalBeanBuildItem> additionalBeans, Capabilities capabilities) {
        if (capabilities.isPresent(Capability.AGROAL)) {
            additionalBeans
                    .produce(new AdditionalBeanBuildItem.Builder().addBeanClass(EmbeddedPostgreSQLConnectionConfigurer.class)
                            .setDefaultScope(BuiltinScope.APPLICATION.getName())
                            .setUnremovable()
                            .build());
        }
    }

    @BuildStep
    void registerDriver(BuildProducer<JdbcDriverBuildItem> jdbcDriver) {
        jdbcDriver.produce(new JdbcDriverBuildItem(DatabaseKind.POSTGRESQL, "org.postgresql.Driver",
                "org.postgresql.xa.PGXADataSource"));
    }

    @BuildStep
    void addDependencies(BuildProducer<IndexDependencyBuildItem> indexDependency) {
        indexDependency.produce(new IndexDependencyBuildItem("io.zonky.test",
                "embedded-postgres"));
    }

    @BuildStep(onlyIf = NativeOrNativeSourcesBuild.class)
    public void nativeResources(BuildProducer<NativeImageResourcePatternsBuildItem> resource) {
        resource.produce(NativeImageResourcePatternsBuildItem.builder().includeGlob("postgres-*.txz").build());
    }

    private Map<String, String> createDatabases(EmbeddedPostgres pg, DataSourcesBuildTimeConfig dataSourcesBuildTimeConfig,
            String userName) {
        pg.getDatabase(DEFAULT_USERNAME, DEFAULT_DATABASE);
        return dataSourcesBuildTimeConfig.dataSources().entrySet().stream()
                .filter(e -> !e.getKey().equals("<default>"))
                .filter(ds -> Objects.equals(ds.getValue().dbKind().orElse(""), "postgresql"))
                .map(Map.Entry::getKey)
                .map(ds -> Map.entry(ds, createDatabase(pg.getPostgresDatabase(), ds, userName)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private String createDatabase(final DataSource dataSource, final String dbName, final String userName) {
        Objects.requireNonNull(dbName);
        Objects.requireNonNull(userName);
        String sanitizedDbName = PostgreSQLSyntaxUtils.sanitizeDbName(dbName);
        String createDbStatement = String.format(
                "SELECT 'CREATE DATABASE %s OWNER %s' as createQuery WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '%s')",
                sanitizedDbName, userName, sanitizedDbName);
        try (Connection connection = dataSource.getConnection();
                Statement stmt = connection.createStatement()) {
            ResultSet result = stmt.executeQuery(createDbStatement);
            if (result.next()) {
                stmt.executeUpdate(result.getString("createQuery"));
            }
            return sanitizedDbName;
        } catch (SQLException e) {
            throw new IllegalStateException("Error creating DB " + dbName, e);
        }
    }
}
