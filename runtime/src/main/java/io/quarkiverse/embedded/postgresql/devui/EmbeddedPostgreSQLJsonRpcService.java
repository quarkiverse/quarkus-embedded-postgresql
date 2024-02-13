package io.quarkiverse.embedded.postgresql.devui;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.inject.Inject;

import io.quarkus.devui.runtime.config.ConfigDescription;
import io.quarkus.devui.runtime.config.ConfigDescriptionBean;

public class EmbeddedPostgreSQLJsonRpcService {

    @Inject
    ConfigDescriptionBean configDescriptionBean;

    public int getDatasourcePort() {
        Optional<ConfigDescription> config = configDescriptionBean.getAllConfig().stream()
                .filter(c -> c.getName().equalsIgnoreCase("quarkus.datasource.jdbc.url")).findFirst();
        // Define a regex pattern to match numbers
        Pattern pattern = Pattern.compile("\\d+");

        // Create a matcher with the input string
        Matcher matcher = pattern.matcher(config.get().getConfigValue().getValue());

        // Find and print all numbers in the input string
        while (matcher.find()) {
            String number = matcher.group();
            return Integer.parseInt(number);
        }
        return 0;
    }
}