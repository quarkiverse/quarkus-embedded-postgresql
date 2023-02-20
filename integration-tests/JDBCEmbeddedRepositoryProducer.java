package io.quarkiverse.embedded.postgresql.it;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.sql.DataSource;

import io.quarkus.arc.profile.IfBuildProfile;

@Dependent
public class JDBCEmbeddedRepositoryProducer {

    @Inject
    DataSource ds;

    @Produces
    @IfBuildProfile("jdbc")
    public EmbeddedRepository jdbc() {
        return new JDBCEmbeddedRepository(ds);
    }
}
