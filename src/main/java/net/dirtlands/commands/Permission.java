package net.dirtlands.commands;

public enum Permission {
    DIRTLANDS("dirtlands.dirtlands"),
    ECONOMY("dirtlands.economy"),
    SHOPKEEPER("dirtlands.shopkeeper"),
    BYPASS_COMBAT("dirtlands.combat.bypass"),
    DURABILITY("dirtlands.durability");

    private final String name;

    Permission(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
