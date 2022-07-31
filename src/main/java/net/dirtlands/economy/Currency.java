package net.dirtlands.economy;

import jeeper.utils.MessageTools;
import net.dirtlands.tools.ItemTools;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class Currency {
    public static final ItemStack DIAMOND_ITEM = new ItemStack(Material.DIAMOND, 1);
    public static final ItemStack TOKEN_ITEM = new ItemStack(Material.SUNFLOWER, 1);

    static {
        ItemMeta meta = DIAMOND_ITEM.getItemMeta();
        meta.displayName(MessageTools.parseText("<!italic><dark_aqua>Diamond"));
        meta.lore(List.of(MessageTools.parseText("<!italic><aqua>Shiny and Grindy")));
        meta.addEnchant(Enchantment.DURABILITY, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        DIAMOND_ITEM.setItemMeta(meta);

        meta = TOKEN_ITEM.getItemMeta();
        meta.displayName(MessageTools.parseText("<!italic><gold>Token"));
        meta.lore(List.of(MessageTools.parseText("<!italic><yellow>As cheap as it can get")));
        meta.addEnchant(Enchantment.DURABILITY, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        TOKEN_ITEM.setItemMeta(meta);
    }

    private int diamonds;
    private int tokens;

    public Currency(int diamonds, int tokens) {
        this.diamonds = diamonds;
        this.tokens = tokens;
    }

    public Currency(Currency currency) {
        this.diamonds = currency.diamonds;
        this.tokens = currency.tokens;
    }

    public Currency() {
        this.diamonds = 0;
        this.tokens = 0;
    }

    public Currency(Inventory inventory) {
        this.diamonds = ItemTools.countItems(inventory, DIAMOND_ITEM);
        this.tokens = ItemTools.countItems(inventory, TOKEN_ITEM);
    }

    public void convertTokensToDiamonds(){
        int extraDiamonds = tokens/64;
        this.diamonds += extraDiamonds;
        this.tokens = this.tokens - 64 * extraDiamonds;
    }

    public void convertDiamondsToTokens() {
        this.tokens = this.tokens + 64 * this.diamonds;
        this.diamonds = 0;
    }

    public Currency multiply(int factor) {
        Currency result = new Currency(this);
        result.setTokens(factor * (result.getTokens() + result.getDiamonds() * 64));
        result.setDiamonds(0);
        result.convertTokensToDiamonds();
        return result;
    }

    /**
     * Divides the balance, but drops any decimals
     */
    public Currency divide(int divisor) {
        Currency result = new Currency(this);
        result.setTokens((result.getTokens() + result.getDiamonds() * 64) /divisor);
        result.setDiamonds(0);
        result.convertTokensToDiamonds();
        return result;
    }

    public Currency add(Currency addend) {
        Currency result = new Currency(this);
        result.setTokens(addend.getTokens() + result.getTokens());
        result.setDiamonds(addend.getDiamonds() + result.getDiamonds());
        result.convertTokensToDiamonds();
        return result;
    }

    public int asTokens() {
        return diamonds*64+tokens;
    }

    public Currency copy() {
        return new Currency(this);
    }

    public int getDiamonds() {
        return diamonds;
    }

    public void setDiamonds(int diamonds) {
        this.diamonds = diamonds;
    }

    public int getTokens() {
        return tokens;
    }

    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    public ItemStack getDiamondItem() {
        return DIAMOND_ITEM.asQuantity(diamonds);
    }

    public ItemStack getTokenItem() {
        return TOKEN_ITEM.asQuantity(tokens);
    }

    /**
     * @return ItemStack[0] = diamonds, ItemStack[1] = tokens
     */
    public ItemStack[] itemsAsArray() {
        return List.of(getDiamondItem(), getTokenItem()).toArray(new ItemStack[2]);
    }
}
