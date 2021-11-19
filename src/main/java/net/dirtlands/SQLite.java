package net.dirtlands;

import org.flywaydb.core.Flyway;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.sqlite.SQLiteDataSource;

public class SQLite {

    protected static DSLContext databaseSetup() {
        String dbUrl = "jdbc:sqlite:dirtlands.db";
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl(dbUrl);

        Flyway flyway = Flyway.configure().dataSource(ds).load();
        flyway.migrate();

        Settings settings = new Settings()
                .withExecuteLogging(false); // Defaults to true

        Configuration configuration = new DefaultConfiguration()
                .set(ds)
                .set(SQLDialect.SQLITE)
                .set(settings);

        return DSL.using(configuration);
    }

}
