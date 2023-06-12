package io.quarkiverse.embedded.postgresql.it;

import java.net.URI;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
