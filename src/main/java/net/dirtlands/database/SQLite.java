package net.dirtlands.database;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.sqlite.SQLiteDataSource;

public class SQLite {

    public static DSLContext databaseSetup(String dirtlandsDbPath) {
        String dbUrl = "jdbc:sqlite:"+dirtlandsDbPath+"/dirtlands.db";

        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl(dbUrl);

        Flyway flyway = Flyway.configure(SQLite.class.getClassLoader())
                .dataSource(ds)
                .load();

        try{
            flyway.migrate();
        } catch (FlywayException e) {
            e.printStackTrace();
        }


        Settings settings = new Settings()
                .withExecuteLogging(false); // Defaults to true

        Configuration configuration = new DefaultConfiguration()
                .set(ds)
                .set(SQLDialect.SQLITE)
                .set(settings);

        return DSL.using(configuration);
    }

}
