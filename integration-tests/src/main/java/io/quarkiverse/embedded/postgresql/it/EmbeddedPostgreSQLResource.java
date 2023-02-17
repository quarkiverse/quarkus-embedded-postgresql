package io.quarkiverse.embedded.postgresql.it;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;

@Path("inmemory-postgresql")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmbeddedPostgreSQLResource {

    private final PgPool client;

    public EmbeddedPostgreSQLResource(PgPool client) {
        this.client = client;
    }

    @GET
    public Multi<EmbeddedPostgreSQL> get() {
        return EmbeddedPostgreSQL.findAll(client);
    }

    @GET
    @Path("{id}")
    public Uni<Response> getSingle(Long id) {
        return EmbeddedPostgreSQL.findById(client, id)
                .onItem()
                .transform(inmemoryPostgreSQL -> inmemoryPostgreSQL != null ? Response.ok(inmemoryPostgreSQL)
                        : Response.status(Status.NOT_FOUND))
                .onItem().transform(ResponseBuilder::build);
    }

    @POST
    public Uni<Response> create(EmbeddedPostgreSQL inmemoryPostgreSQL) {
        return inmemoryPostgreSQL.save(client)
                .onItem().transform(id -> URI.create("/inmemory_postgresql/" + id))
                .onItem().transform(uri -> Response.created(uri).build());
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(Long id, EmbeddedPostgreSQL inmemoryPostgreSQL) {
        return inmemoryPostgreSQL.update(client)
                .onItem().transform(updated -> updated ? Status.OK : Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(Long id) {
        return EmbeddedPostgreSQL.delete(client, id)
                .onItem().transform(deleted -> deleted ? Status.NO_CONTENT : Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }
}
