package io.quarkiverse.embedded.postgresql.deployment.devui;

import io.quarkiverse.embedded.postgresql.devui.EmbeddedPostgreSQLJsonRpcService;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.devui.spi.page.PageBuilder;

/**
 * Dev UI card for displaying important details such EmbeddedPostgreSQL embedded UI.
 */
public class EmbeddedPostgreSQLDevUIProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    void createVersion(BuildProducer<CardPageBuildItem> cardPageBuildItemBuildProducer) {
        final CardPageBuildItem card = new CardPageBuildItem();

        final PageBuilder portPage = Page.externalPageBuilder("Port")
                .icon("font-awesome-solid:plug")
                .url("https://github.com/zonkyio/embedded-postgres")
                .doNotEmbed()
                .dynamicLabelJsonRPCMethodName("getDatasourcePort");
        card.addPage(portPage);

        card.setCustomCard("qwc-embedded-postgresql-card.js");
        cardPageBuildItemBuildProducer.produce(card);
    }

    @BuildStep
    JsonRPCProvidersBuildItem createJsonRPCService() {
        return new JsonRPCProvidersBuildItem(EmbeddedPostgreSQLJsonRpcService.class);
    }
}