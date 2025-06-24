package io.quarkiverse.embedded.postgresql.deployment.devui;

import io.quarkus.builder.item.SimpleBuildItem;

public final class PgAdminConfigBuildItem extends SimpleBuildItem {

    private final String pgAdminUrl;
    private final String pgAdminPort;

    public PgAdminConfigBuildItem(String pgAdminUrl, String pgAdminPort) {
        this.pgAdminUrl = pgAdminUrl;
        this.pgAdminPort = pgAdminPort;
    }

    public String getPgAdminUrl() {
        return pgAdminUrl;
    }

    public String getPgAdminPort() {
        return pgAdminPort;
    }
}
