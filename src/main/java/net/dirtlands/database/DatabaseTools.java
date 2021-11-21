package net.dirtlands.database;

import dirtlands.db.Tables;
import net.dirtlands.Main;
import org.jooq.DSLContext;
import org.jooq.Record1;

import java.util.UUID;

public class DatabaseTools {

    static DSLContext dslContext = Main.getPlugin().getDslContext();

    /**
     * Add a user to the database, providing player's UUID
     */
    public static void addUser(UUID uuid) {
        dslContext.insertInto(Tables.USERS, Tables.USERS.USERUUID).values(uuid.toString()).execute();
    }

    /**
     * Add a user to the database, providing player's UUID
     */
    public static void addUser(String uuid) {
        dslContext.insertInto(Tables.USERS, Tables.USERS.USERUUID).values(uuid).execute();
    }

    /**
     * Return the userid stored in the database, using the player's UUID
     * @return the id stored, or -1 if it does not exist
     */
    public static int getUserID(UUID uuid) {
        Record1<Integer> record = dslContext.select(Tables.USERS.USERID).from(Tables.USERS).where(Tables.USERS.USERUUID.eq(uuid.toString())).fetchAny();
        return firstInt(record);
    }

    /**
     * Return the userid stored in the database, using the player's UUID
     * @return the id stored, or -1 if it does not exist
     */
    public static int getUserID(String uuid) {
        Record1<Integer> record = dslContext.select(Tables.USERS.USERID).from(Tables.USERS).where(Tables.USERS.USERUUID.eq(uuid)).fetchAny();
        return firstInt(record);
    }

    /**
     * Return the first integer record
     * @return the first value, or -1 if it does not exist
     */
    public static int firstInt(Record1<Integer> record) {
        if (record == null) {
            return -1;
        }
        return record.value1();
    }

    /**
     * Return the first string record
     * @return the first string, or null if it does not exist
     */
    public static String firstString(Record1<String> record) {
        if (record == null) {
            return null;
        }
        return record.value1();
    }
}
