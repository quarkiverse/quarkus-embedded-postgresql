package io.quarkiverse.embedded.postgresql.deployment;

import java.io.IOException;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

@Disabled
public class WaitStartupWaitTest {

    // this should catch the `Caused by: java.io.IOException: Gave up waiting for server to start after 100ms` exception
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setExpectedException(IOException.class)
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .withConfigurationResource("startup-application.properties");

    @Test()
    public void waitStartup() {
    }
}