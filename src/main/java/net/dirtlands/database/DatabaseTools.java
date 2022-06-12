package net.dirtlands.database;

import dirtlands.db.Tables;
import net.dirtlands.Main;
import org.jooq.DSLContext;

import java.time.LocalDateTime;
import java.util.UUID;

public class DatabaseTools {

    static DSLContext dslContext = Main.getPlugin().getDslContext();

    /**
     * Add a user to the database, providing player's UUID
     */
    public static void addUser(UUID uuid) {
        dslContext.insertInto(Tables.USERS, Tables.USERS.USERUUID, Tables.USERS.JOINDATE)
                .values(uuid.toString(), LocalDateTime.now()).execute();
    }

    /**
     * Add a user to the database, providing player's UUID
     */
    public static void addUser(String uuid) {
        dslContext.insertInto(Tables.USERS, Tables.USERS.USERUUID, Tables.USERS.JOINDATE)
                .values(uuid, LocalDateTime.now()).execute();
    }

    /**
     * Return the userid stored in the database, using the player's UUID
     * @return the id stored, or -1 if it does not exist
     */
    public static int getUserID(UUID uuid) {
        var record = dslContext.select(Tables.USERS.USERID)
                .from(Tables.USERS).where(Tables.USERS.USERUUID.eq(uuid.toString())).fetchAny();
        if (record == null) {
            return -1;
        }

        return record.get(Tables.USERS.USERID);
    }

    /**
     * Return the userid stored in the database, using the player's UUID
     * @return the id stored, or -1 if it does not exist
     */
    public static int getUserID(String uuid) {
        var record = dslContext.select(Tables.USERS.USERID)
                .from(Tables.USERS).where(Tables.USERS.USERUUID.eq(uuid)).fetchAny();
        if (record == null) {
            return -1;
        }

        return record.get(Tables.USERS.USERID);
    }
}
