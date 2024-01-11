package de.unknowncity.ucbans.data.database;

import com.zaxxer.hikari.HikariDataSource;
import de.chojo.sadu.databases.MariaDb;
import de.chojo.sadu.databases.MySql;
import de.chojo.sadu.databases.SqLite;
import de.chojo.sadu.datasource.DataSourceCreator;
import de.chojo.sadu.jdbc.MariaDbJdbc;
import de.unknowncity.ucbans.configuration.settings.DataBaseSettings;
import org.mariadb.jdbc.Driver;

import java.nio.file.Path;

public class DataBaseProvider {
    private final DataBaseSettings dataBaseSettings;

    public DataBaseProvider(DataBaseSettings dataBaseSettings) {
        this.dataBaseSettings = dataBaseSettings;
    }

    public HikariDataSource createDataSource() {
        switch (dataBaseSettings.dbDriver()) {
            case MARIADB -> {
                return DataSourceCreator.create(MariaDb.get())
                        .configure(config -> config
                                .port(dataBaseSettings.port())
                                .user(dataBaseSettings.userName())
                                .password(dataBaseSettings.password())
                                .database(dataBaseSettings.database())
                                .host(dataBaseSettings.host())
                                .driverClass(Driver.class)
                        )
                        .create()
                        .withMaximumPoolSize(dataBaseSettings.maxPoolSize())
                        .withMinimumIdle(dataBaseSettings.minIdleConnections())
                        .build();
            }
            default -> {
                return DataSourceCreator.create(MySql.get())
                        .configure(config -> config
                                .port(dataBaseSettings.port())
                                .user(dataBaseSettings.userName())
                                .password(dataBaseSettings.password())
                                .database(dataBaseSettings.database())
                                .host(dataBaseSettings.host())
                                .driverClass()
                        )
                        .create()
                        .withMaximumPoolSize(dataBaseSettings.maxPoolSize())
                        .withMinimumIdle(dataBaseSettings.minIdleConnections())
                        .build();
            }
        }
    }
}
