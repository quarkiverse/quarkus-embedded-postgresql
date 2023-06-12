package io.quarkiverse.embedded.postgresql.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class EmbeddedPostgreSQLProcessor extends AbstractEmbeddedPostgreSQLProcessor {

    private static final String FEATURE = "embedded-postgres-arm64";

    EmbeddedPostgreSQLProcessor() {
        super(FEATURE);
    }

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
}
