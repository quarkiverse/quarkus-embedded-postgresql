package io.quarkiverse.embedded.postgresql;

import java.util.Collections;
import java.util.Map;

public class StartupInfo {
    private final int port;
    private final Map<String, String> databases;

    protected StartupInfo(int port, Map<String, String> databases) {
        this.port = port;
        this.databases = Collections.unmodifiableMap(databases);
    }

    public int getPort() {
        return port;
    }

    public Map<String, String> getDatabases() {
        return databases;
    }
}
