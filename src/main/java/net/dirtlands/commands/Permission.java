package net.dirtlands.commands;

public enum Permission {
    NICKNAME("dirtlands.nickname"),
    DIRTLANDS("dirtlands.dirtlands"),
    BROADCAST("dirtlands.broadcast"),
    SETWARP("dirtlands.warps.set"),
    SETSPAWN("dirtlands.setspawn"),
    SETHOME("dirtlands.sethome"),
    DELETEWARP("dirtlands.warps.delete"),
    CLEARCHAT("dirtlands.chat.clear"),
    MUTECHAT("dirtlands.chat.mute"),
    BYPASSCHAT("dirtlands.chat.bypass"),
    NPC("dirtlands.npc"),
    CHATCOLOR("dirtlands.chat.color"),
    ECONOMY("dirtlands.economy"),
    METADATA("dirtlands.metadata"),
    AFK("dirtlands.afk");

    private final String name;

    Permission(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
