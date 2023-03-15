package io.quarkiverse.embedded.postgresql;

import java.util.Map;

public class StartupInfo {
    int port;
    Map<String, String> databases;

    public StartupInfo(int port, Map<String, String> databases) {
        this.port = port;
        this.databases = databases;
    }

    public int getPort() {
        return port;
    }

    public Map<String, String> getDatabases() {
        return databases;
    }
}
