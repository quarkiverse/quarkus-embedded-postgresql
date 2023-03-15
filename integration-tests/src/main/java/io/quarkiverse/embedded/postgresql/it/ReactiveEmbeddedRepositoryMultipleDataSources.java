package io.quarkiverse.embedded.postgresql.it;

import javax.enterprise.context.Dependent;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.reactive.datasource.ReactiveDataSource;
import io.vertx.mutiny.pgclient.PgPool;

@Dependent
@IfBuildProfile("multiple-datasources")
public class ReactiveEmbeddedRepositoryMultipleDataSources extends ReactiveEmbeddedRepository {

    public ReactiveEmbeddedRepositoryMultipleDataSources(@ReactiveDataSource("database2") PgPool client) {
        super(client);
    }
}
