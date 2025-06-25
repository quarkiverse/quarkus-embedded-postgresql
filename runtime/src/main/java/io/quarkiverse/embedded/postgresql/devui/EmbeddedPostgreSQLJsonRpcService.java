package io.quarkiverse.embedded.postgresql.devui;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.devui.runtime.config.ConfigDescriptionBean;

public class EmbeddedPostgreSQLJsonRpcService {

    @Inject
    ConfigDescriptionBean configDescriptionBean;

    @ConfigProperty(name = "quarkus.datasource.jdbc.url")
    Optional<String> jdbcUrl;

    public int getDatasourcePort() {
        String port = jdbcUrl
                .orElseGet(() -> configDescriptionBean.getAllConfig().stream()
                        .filter(c -> c.getName().equalsIgnoreCase("quarkus.datasource.jdbc.url"))
                        .map(c -> c.getConfigValue().getValue())
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(
                                "No JDBC URL found in configuration. Please ensure 'quarkus.datasource.jdbc.url' is set.")));
        // Create a matcher with the input string
        Matcher matcher = Pattern.compile("\\d+").matcher(port);
        // Find and print all numbers in the input string
        return matcher.find() ? Integer.parseInt(matcher.group()) : 0;
    }
}
