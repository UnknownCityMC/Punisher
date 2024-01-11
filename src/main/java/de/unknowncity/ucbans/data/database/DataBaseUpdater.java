package de.unknowncity.ucbans.data.database;

import de.chojo.sadu.databases.MariaDb;
import de.chojo.sadu.databases.MySql;
import de.chojo.sadu.updater.SqlUpdater;
import de.unknowncity.ucbans.configuration.settings.DataBaseSettings;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public class DataBaseUpdater {
    private final DataSource dataSource;
    private final DataBaseSettings dataBaseSettings;

    public DataBaseUpdater(DataSource dataSource, DataBaseSettings dataBaseSettings) {
        this.dataSource = dataSource;
        this.dataBaseSettings = dataBaseSettings;
    }

    public void update() throws IOException, SQLException {
        switch (dataBaseSettings.dbDriver()) {
            case MARIADB -> {
                SqlUpdater.builder(dataSource, MariaDb.get())
                        .setVersionTable("version")
                        .execute();
            }

            default -> {
                SqlUpdater.builder(dataSource, MySql.get())
                        .setVersionTable("version")
                        .execute();
            }
        }
    }
}
