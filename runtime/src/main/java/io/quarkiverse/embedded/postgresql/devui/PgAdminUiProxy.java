package io.quarkiverse.embedded.postgresql.devui;

import org.jboss.logging.Logger;

import io.quarkus.vertx.http.runtime.RouteHandler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.WebClient;

/**
 * Builds the route handler that proxies {@code /q/pgadmin} and {@code /pgadmin} to the pgAdmin dev service container.
 */
public final class PgAdminUiProxy {

    private static final Logger log = Logger.getLogger(PgAdminUiProxy.class);

    private static final String MAIN_PAGE = "/q/pgadmin";

    private static final String RESOURCES = "/pgadmin";

    private PgAdminUiProxy() {
    }

    public static RouteHandler createHandler(Vertx vertx, int port) {
        final var client = WebClient.create(vertx);

        return event -> {
            String suffix;
            String rawUri = event.request().uri();
            if (rawUri.startsWith(MAIN_PAGE)) {
                suffix = rawUri.substring(MAIN_PAGE.length());
            } else if (rawUri.startsWith(RESOURCES)) {
                suffix = rawUri.substring(RESOURCES.length());
            } else {
                event.response().setStatusCode(404).end();
                return;
            }

            client.request(event.request().method(),
                    port,
                    "localhost",
                    "/pgadmin" + suffix)
                    .followRedirects(true)
                    .putHeaders(event.request().headers())
                    .sendBuffer(event.request().body().result()).onComplete(
                            resp -> {
                                if (resp.succeeded()) {
                                    event.response().setStatusCode(resp.result().statusCode());
                                    resp.result().headers().forEach(h -> event.response().putHeader(h.getKey(), h.getValue()));
                                    Buffer body = resp.result().body();
                                    if (body != null && body.length() > 0) {
                                        event.response().end(body);
                                    } else {
                                        event.response().end();
                                    }
                                } else {
                                    log.error("Failed to proxy request to PgAdmin UI", resp.cause());
                                    event.response().setStatusCode(500).end();
                                }
                            });
        };
    }
}
