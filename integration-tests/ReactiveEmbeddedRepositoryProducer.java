package io.quarkiverse.embedded.postgresql.it;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.quarkus.arc.DefaultBean;
import io.vertx.mutiny.pgclient.PgPool;

@Dependent
public class ReactiveEmbeddedRepositoryProducer {

    @Inject
    PgPool client;

    @Produces
    @DefaultBean
    public EmbeddedRepository reactive() {
        return new ReactiveEmbeddedRepository(client);
    }
}
