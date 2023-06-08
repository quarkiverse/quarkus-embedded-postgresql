package io.quarkiverse.embedded.postgresql.deployment;

import java.io.IOException;

import io.quarkus.test.QuarkusUnitTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class WaitStartupWaitTest {

    // this should catch the `Caused by: java.io.IOException: Gave up waiting for server to start after 100ms` exception
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setExpectedException(IOException.class)
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .withConfigurationResource("startup-application.properties");

    @Test()
    public void waitStartup() {
        Assertions.fail("Expected failure to check startup timeout exception");
    }
}
