package net.dirtlands.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

public class LogFilter extends AbstractFilter {

    @Override
    public Result filter(LogEvent event) {
        return event == null ? Result.NEUTRAL : isLoggable(event.getMessage().getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return isLoggable(msg.getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return isLoggable(msg);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return msg == null ? Result.NEUTRAL : isLoggable(msg.toString());
    }

    private Result isLoggable(String msg) {
        if (msg != null) {

            if (msg.contains("issued server command:")) {
                if (msg.contains("/warn") || msg.contains("/unmute") || msg.contains("/unban") || msg.contains("/ban")
                        || msg.contains("/mute") || msg.contains("/kick")) {
                    return Result.DENY;
                }
            }


            if (msg.contains("You have been banned") || msg.contains("You have been kicked") || msg.contains("lost connection: ")
            || msg.contains("logged in with entity id") || msg.contains("UUID of")) {
                return Result.DENY;
            }
        }
        return Result.NEUTRAL;
    }


}
