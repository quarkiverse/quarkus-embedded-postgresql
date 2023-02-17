package io.quarkiverse.embedded.postgresql.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

import java.io.IOException;

import io.quarkiverse.embedded.postgresql.EmbeddedPostgreSQLRecorder;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.RunTimeConfigurationSourceValueBuildItem;
import io.quarkus.deployment.builditem.ServiceStartBuildItem;
import io.quarkus.runtime.RuntimeValue;

class EmbeddedPostgreSQLProcessor {

    private static final String FEATURE = "embedded-postgres";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(RUNTIME_INIT)
    ServiceStartBuildItem startService(EmbeddedPostgreSQLRecorder recorder,
            BuildProducer<RunTimeConfigurationSourceValueBuildItem> configSourceValueBuildItem) throws IOException {
        RuntimeValue<Integer> port = recorder.startPostgres();
        configSourceValueBuildItem.produce(new RunTimeConfigurationSourceValueBuildItem(recorder.configSources(port)));
        return new ServiceStartBuildItem(FEATURE);
    }

    @BuildStep
    void addDependencies(BuildProducer<IndexDependencyBuildItem> indexDependency) {
        indexDependency.produce(new IndexDependencyBuildItem("io.zonky.test",
                "embedded-postgres"));
    }
}
