package io.quarkiverse.embedded.postgresql.it;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

public class EmbeddedPostgreSQL {

    public Long id;

    public String name;

    public EmbeddedPostgreSQL() {
        // default constructor.
    }

    public EmbeddedPostgreSQL(String name) {
        this.name = name;
    }

    public EmbeddedPostgreSQL(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Multi<EmbeddedPostgreSQL> findAll(PgPool client) {
        return client.query("SELECT id, name FROM inmemory_postgresql ORDER BY name ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(EmbeddedPostgreSQL::from);
    }

    public static Uni<EmbeddedPostgreSQL> findById(PgPool client, Long id) {
        return client.preparedQuery("SELECT id, name FROM inmemory_postgresql WHERE id = $1").execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public Uni<Long> save(PgPool client) {
        return client.preparedQuery("INSERT INTO inmemory_postgresql (name) VALUES ($1) RETURNING id").execute(Tuple.of(name))
                .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getLong("id"));
    }

    public Uni<Boolean> update(PgPool client) {
        return client.preparedQuery("UPDATE inmemory_postgresql SET name = $1 WHERE id = $2").execute(Tuple.of(name, id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    public static Uni<Boolean> delete(PgPool client, Long id) {
        return client.preparedQuery("DELETE FROM inmemory_postgresql WHERE id = $1").execute(Tuple.of(id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    private static EmbeddedPostgreSQL from(Row row) {
        return new EmbeddedPostgreSQL(row.getLong("id"), row.getString("name"));
    }
}
