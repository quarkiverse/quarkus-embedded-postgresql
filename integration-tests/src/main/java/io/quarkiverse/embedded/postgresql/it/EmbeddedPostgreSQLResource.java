package io.quarkiverse.embedded.postgresql.it;

import java.net.URI;
import java.util.Collection;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("inmemory-postgresql")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmbeddedPostgreSQLResource {

    private final EmbeddedRepository repository;

    public EmbeddedPostgreSQLResource(EmbeddedRepository repository) {
        this.repository = repository;
    }

    @GET
    public Collection<EmbeddedVO> get() {
        return repository.findAll();
    }

    @GET
    @Path("{id}")
    public Response getSingle(long id) {
        return repository.findById(id).map(Response::ok).orElse(Response.status(Status.NOT_FOUND)).build();
    }

    @PUT
    @Path("{id}")
    public Response put(@PathParam("id") long id, EmbeddedBody body) {
        boolean created = repository.createOrUpdate(new EmbeddedVO(id, body));
        return (created ? Response.status(Status.CREATED) : Response.ok()).build();
    }

    @POST
    public Response post(EmbeddedBody body) {
        return Response.created(URI.create("/inmemory_postgresql/" + repository.create(body))).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(long id) {
        boolean deleted = repository.delete(id);
        return Response.status(deleted ? Status.NO_CONTENT : Status.NOT_FOUND).build();

    }
}
