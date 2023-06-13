package io.quarkiverse.embedded.postgresql.it;

import java.util.Collection;
import java.util.Optional;

import javax.enterprise.context.Dependent;

import io.quarkus.arc.DefaultBean;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

@Dependent
@DefaultBean
public class ReactiveEmbeddedRepository implements EmbeddedRepository {

    private static final String FIND_ALL = "SELECT id, name FROM inmemory_postgresql ORDER BY name ASC";
    private static final String FIND_BY_ID = "SELECT id, name FROM inmemory_postgresql WHERE id = $1";
    private static final String SAVE = "INSERT INTO inmemory_postgresql (name) VALUES ($1) RETURNING id";
    private static final String SAVE_OR_UPDATE = "INSERT INTO inmemory_postgresql (id, name) VALUES ($1, $2)  ON CONFLICT(id) DO UPDATE SET name = $2 returning (xmax = 0) AS inserted";
    private static final String DELETE = "DELETE FROM inmemory_postgresql WHERE id = $1";

    private final PgPool client;

    public ReactiveEmbeddedRepository(PgPool client) {
        this.client = client;
    }

    private EmbeddedVO from(Row row) {
        return new EmbeddedVO(row.getLong("id"), row.getString("name"));
    }

    @Override
    public Collection<EmbeddedVO> findAll() {
        return client.query(FIND_ALL).execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(this::from).collect().asList().await().indefinitely();
    }

    @Override
    public Optional<EmbeddedVO> findById(Long id) {
        return Optional.of(client.preparedQuery(FIND_BY_ID).execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null).await().indefinitely());
    }

    @Override
    public long create(EmbeddedBody body) {
        return client.preparedQuery(SAVE).execute(Tuple.of(body.getName()))
                .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getLong("id")).await().indefinitely();
    }

    @Override
    public boolean createOrUpdate(EmbeddedVO vo) {
        return client
                .preparedQuery(
                        SAVE_OR_UPDATE)
                .execute(Tuple.of(vo.getId(), vo.getName()))
                .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getBoolean("inserted")).await().indefinitely();
    }

    @Override
    public boolean delete(long id) {
        return client.preparedQuery(DELETE).execute(Tuple.of(id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1).await().indefinitely();
    }
}
