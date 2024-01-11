package de.unknowncity.ucbans.configuration.settings;

import de.unknowncity.ucbans.data.database.DBDriver;

public record DataBaseSettings(
        DBDriver dbDriver,
        int port,
        String host,
        String database,
        String userName,
        String password,
        int maxPoolSize,
        int minIdleConnections
)
{}
