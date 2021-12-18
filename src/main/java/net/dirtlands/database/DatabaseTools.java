package net.dirtlands.database;

import dirtlands.db.Tables;
import net.dirtlands.Main;
import org.jooq.DSLContext;
import org.jooq.Record1;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss.SSS a");

    /**
     * Takes a localdatetime and turns it into a string, in the format of mm/dd/yyyy hh:mm:ss.SSS am/pm
     * @param localDateTime the localdatetime to convert
     * @return the string representation of the localdatetime
     */
    public static String localDateTimeToString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return formatter.format(localDateTime) + " PST";
    }

    /**
     * Takes a string and turns it into a localdatetime
     * @param time the string to convert to a localdatetime. In the format of mm/dd/yyyy hh:mm:ss.SSS am/pm
     * @return the localdatetime
     */
    public static LocalDateTime stringToLocalDateTime(String time) {
        if (time == null) {
            return null;
        }
        return LocalDateTime.from(formatter.parse(time.substring(0, time.length() - 3).trim()));
    }
}
