package io.quarkiverse.embedded.postgresql;

import java.util.Set;

public class StartupInfo {
    int port;
    Set<String> databases;

    public StartupInfo(int port, Set<String> databases) {
        this.port = port;
        this.databases = databases;
    }

    public int getPort() {
        return port;
    }

    public Set<String> getDatabases() {
        return databases;
    }
}
