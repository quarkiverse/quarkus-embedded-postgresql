package io.quarkiverse.embedded.postgresql.it;

import jakarta.enterprise.context.Dependent;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.reactive.datasource.ReactiveDataSource;
import io.vertx.mutiny.sqlclient.Pool;

@Dependent
@IfBuildProfile("multiple-datasources")
public class ReactiveEmbeddedRepositoryMultipleDataSources extends ReactiveEmbeddedRepository {

    public ReactiveEmbeddedRepositoryMultipleDataSources(@ReactiveDataSource("database-2") Pool client) {
        super(client);
    }
}
