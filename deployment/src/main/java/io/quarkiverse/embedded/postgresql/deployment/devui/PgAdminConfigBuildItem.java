package io.quarkiverse.embedded.postgresql.deployment.devui;

import io.quarkus.builder.item.SimpleBuildItem;

public final class PgAdminConfigBuildItem extends SimpleBuildItem {

    private final String pgAdminHost;
    private final int pgAdminPort;

    public PgAdminConfigBuildItem(String pgAdminHost, int pgAdminPort) {
        this.pgAdminHost = pgAdminHost;
        this.pgAdminPort = pgAdminPort;
    }

    public String getPgAdminUrl() {
        return pgAdminHost + ":" + pgAdminPort;
    }

    public int getPgAdminPort() {
        return pgAdminPort;
    }
}
