package net.dirtlands.commands;

public enum Permission {
    DIRTLANDS("dirtlands.dirtlands"),
    ECONOMY("dirtlands.economy"),
    SHOPKEEPER("dirtlands.shopkeeper");

    private final String name;

    Permission(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
