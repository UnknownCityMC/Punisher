package de.unknowncity.punisher.configuration.settings;

import de.unknowncity.punisher.data.database.DBDriver;

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
