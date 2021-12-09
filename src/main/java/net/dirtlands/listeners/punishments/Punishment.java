package net.dirtlands.listeners.punishments;

public enum Punishment {
    BAN("Ban"),
    MUTE("Mute"),
    KICK("Kick"),
    WARN("Warn");

    private final String punishment;

    Punishment(String punishment) {
        this.punishment = punishment;
    }

    public String getPunishment() {
        return punishment;
    }

}
