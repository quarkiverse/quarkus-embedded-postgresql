package io.quarkiverse.embedded.postgresql.it;

import javax.sql.DataSource;

import jakarta.enterprise.context.Dependent;

import io.quarkus.arc.profile.IfBuildProfile;

@Dependent
@IfBuildProfile("jdbc-multiple-datasources")
public class JDBCEmbeddedRepositoryMultipleDataSources extends JDBCEmbeddedRepository {
    public JDBCEmbeddedRepositoryMultipleDataSources(@io.quarkus.agroal.DataSource("database-2") DataSource dataSource) {
        super(dataSource);
    }
}
