package io.quarkiverse.embedded.postgresql.deployment;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Dummy {

    public String hello() {
        return "Hello World";
    }
}
