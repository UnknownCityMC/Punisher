package de.unknowncity.ucvelocity.core.data.database;

import de.chojo.sadu.databases.SqLite;
import de.chojo.sadu.updater.SqlUpdater;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public class DataBaseUpdater {
    private final DataSource dataSource;

    public DataBaseUpdater(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update() throws IOException, SQLException {
        SqlUpdater.builder(dataSource, SqLite.get())
                .setVersionTable("version")
                .execute();
    }
}
