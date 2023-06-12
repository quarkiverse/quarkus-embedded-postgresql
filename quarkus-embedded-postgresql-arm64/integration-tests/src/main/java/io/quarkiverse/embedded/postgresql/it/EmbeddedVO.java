package io.quarkiverse.embedded.postgresql.it;

public class EmbeddedVO {

    private final long id;

    private final String name;

    public EmbeddedVO(long id, EmbeddedBody body) {
        this.id = id;
        this.name = body.getName();
    }

    public EmbeddedVO(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
