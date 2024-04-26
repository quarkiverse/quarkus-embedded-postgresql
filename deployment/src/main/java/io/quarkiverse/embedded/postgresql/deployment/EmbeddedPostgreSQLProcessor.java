package io.quarkiverse.embedded.postgresql.deployment;

import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.DEFAULT_DATABASE;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.DEFAULT_JDBC_URL;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.DEFAULT_PASSWORD;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.DEFAULT_REACTIVE_URL;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.DEFAULT_USERNAME;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.QUARKUS_DATASOURCE_JDBC_URL;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.QUARKUS_DATASOURCE_PASSWORD;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.QUARKUS_DATASOURCE_REACTIVE_URL;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.QUARKUS_DATASOURCE_USERNAME;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.QUARKUS_NAMED_DATASOURCE_JDBC_URL;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.QUARKUS_NAMED_DATASOURCE_PASSWORD;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.QUARKUS_NAMED_DATASOURCE_REACTIVE_URL;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.QUARKUS_NAMED_DATASOURCE_USERNAME;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.getConfig;
import static io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils.getDBNames;
import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfig;
import io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConfigUtils;
import io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLConnectionConfigurer;
import io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLDBUtils;
import io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLRecorder;
import io.quarkus.agroal.spi.JdbcDriverBuildItem;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.processor.BuiltinScope;
import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.datasource.common.runtime.DatabaseKind;
import io.quarkus.datasource.runtime.DataSourcesBuildTimeConfig;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.builditem.RunTimeConfigurationDefaultBuildItem;
import io.quarkus.deployment.builditem.ServiceStartBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourcePatternsBuildItem;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.deployment.pkg.steps.NativeOrNativeSourcesBuild;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

class EmbeddedPostgreSQLProcessor {

    private static final Logger log = Logger.getLogger(EmbeddedPostgreSQLProcessor.class);

    private static final String FEATURE = "embedded-postgres";

    static volatile DevServicesResultBuildItem.RunningDevService devService;
    static volatile EmbeddedPostgreSQLConfig cfg;
    static volatile boolean first = true;

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep(onlyIfNot = IsDevelopment.class)
    @Record(RUNTIME_INIT)
    ServiceStartBuildItem startService(EmbeddedPostgreSQLRecorder recorder, ShutdownContextBuildItem shutdown,
            DataSourcesBuildTimeConfig dataSourcesBuildTimeConfig, EmbeddedPostgreSQLConfig postgreSQLConfig,
            BuildProducer<RunTimeConfigurationDefaultBuildItem> configProducer) {
        final int port = postgreSQLConfig.port().orElseGet(EmbeddedPostgreSQLConfigUtils::getDefaultPort);
        Map<String, String> dbNames = getDBNames(dataSourcesBuildTimeConfig);
        recorder.startPostgres(shutdown, port, dbNames);
        getConfig(port, dbNames).forEach((k, v) -> configProducer.produce(new RunTimeConfigurationDefaultBuildItem(k, v)));
        return new ServiceStartBuildItem(FEATURE);
    }

    /**
     * DevService modeled after core Quarkus DataSource processor:
     *
     * @see <a href=
     *      "https://github.com/quarkusio/quarkus/blob/main/extensions/datasource/deployment/src/main/java/io/quarkus/datasource/deployment/devservices/DevServicesDatasourceProcessor.java">...</a>
     */
    @BuildStep(onlyIf = IsDevelopment.class)
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

        EmbeddedPostgres.Builder builder = EmbeddedPostgres.builder();
        postgreSQLConfig.port().ifPresent(
                port -> {
                    log.infov("PG port will be set to {0}", port);
                    builder.setPort(port);
                });

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

        builder.setConnectConfig("stringtype", postgreSQLConfig.stringType());

        EmbeddedPostgres pg = builder.start();
        log.infov(
                "Embedded Postgres started at port \"{0,number,#}\" with database \"{1}\", user \"{2}\" and password \"{3}\"",
                pg.getPort(), DEFAULT_DATABASE, DEFAULT_USERNAME, DEFAULT_PASSWORD);

        Map<String, String> dbNames = getDBNames(dataSourcesBuildTimeConfig);

        Map<String, String> devServerConfigMap = new LinkedHashMap<>();
        Config config = ConfigProvider.getConfig();
        for (String propertyName : config.getPropertyNames()) {
            if (propertyName.startsWith("quarkus.embedded.postgresql.")) {
                devServerConfigMap.put(propertyName, config.getConfigValue(propertyName).getValue());
            }
        }
        EmbeddedPostgreSQLDBUtils.createDatabases(pg, dbNames.values(), DEFAULT_USERNAME);
        dbNames.forEach((k, v) -> devServerConfigMap.putAll(
                Map.of(
                        String.format(QUARKUS_NAMED_DATASOURCE_JDBC_URL, k),
                        String.format(DEFAULT_JDBC_URL, pg.getPort(), v),
                        String.format(QUARKUS_NAMED_DATASOURCE_REACTIVE_URL, k),
                        String.format(DEFAULT_REACTIVE_URL, pg.getPort(), v),
                        String.format(QUARKUS_NAMED_DATASOURCE_USERNAME, k), DEFAULT_USERNAME,
                        String.format(QUARKUS_NAMED_DATASOURCE_PASSWORD, k), DEFAULT_PASSWORD)));

        devServerConfigMap.putAll(Map.of(
                QUARKUS_DATASOURCE_JDBC_URL, String.format(DEFAULT_JDBC_URL, pg.getPort(), DEFAULT_DATABASE),
                QUARKUS_DATASOURCE_REACTIVE_URL, String.format(DEFAULT_REACTIVE_URL, pg.getPort(), DEFAULT_DATABASE),
                QUARKUS_DATASOURCE_USERNAME, DEFAULT_USERNAME,
                QUARKUS_DATASOURCE_PASSWORD, DEFAULT_PASSWORD));

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

}
