package io.quarkiverse.embedded.postgresql.deployment.devui;

import io.quarkiverse.embedded.postgresql.devui.EmbeddedPostgreSQLJsonRpcService;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.devui.spi.page.PageBuilder;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.quarkus.vertx.http.runtime.management.ManagementInterfaceBuildTimeConfig;

/**
 * Dev UI card for displaying important details such EmbeddedPostgreSQL embedded UI.
 */
public class EmbeddedPostgreSQLDevUIProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    void createVersion(BuildProducer<CardPageBuildItem> cardPageBuildItemBuildProducer,
            NonApplicationRootPathBuildItem nonApp,
            ManagementInterfaceBuildTimeConfig mgmtConfig,
            LaunchModeBuildItem lm,
            PgAminUiConfig pgAppConfig) {
        final CardPageBuildItem card = new CardPageBuildItem();

        String managementBase = nonApp.resolveManagementPath("pgadmin", mgmtConfig, lm);

        final PageBuilder portPage = Page.externalPageBuilder("Port")
                .icon("font-awesome-solid:plug")
                .url("https://github.com/zonkyio/embedded-postgres")
                .doNotEmbed()
                .dynamicLabelJsonRPCMethodName("getDatasourcePort");
        card.addPage(portPage);
        if (pgAppConfig.enabled()) {
            final PageBuilder pgAdminPage = Page.externalPageBuilder("pgAdmin UI")
                    .icon("font-awesome-solid:database")
                    .url(managementBase, managementBase)
                    .isHtmlContent();
            card.addPage(pgAdminPage);
        }

        card.setCustomCard("qwc-embedded-postgresql-card.js");
        cardPageBuildItemBuildProducer.produce(card);
    }

    @BuildStep
    JsonRPCProvidersBuildItem createJsonRPCService() {
        return new JsonRPCProvidersBuildItem(EmbeddedPostgreSQLJsonRpcService.class);
    }
}
