package net.dirtlands.economy;

import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import net.dirtlands.tools.ItemTools;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class Currency {
    public static final char TOKEN_CHARACTER = '\uE000';
    public static final char EXPENSIVE_TOKEN_CHARACTER = '\uE001';
    public static final ItemStack TOKEN_ITEM = new ItemStack(Material.SUNFLOWER, 1);
    public static final ItemStack EXPENSIVE_TOKEN_ITEM = new ItemStack(Material.SUNFLOWER, 1);

    static {
        ItemMeta meta = EXPENSIVE_TOKEN_ITEM.getItemMeta();
        meta.displayName(MessageTools.parseText("<!italic><dark_aqua>Expensive Token"));
        meta.lore(List.of(MessageTools.parseText("<!italic><aqua>Shiny and Grindy")));
        meta.addEnchant(Enchantment.DURABILITY, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        NamespacedKey key = new NamespacedKey(Main.getPlugin(), "currency");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "expensive_token");

        EXPENSIVE_TOKEN_ITEM.setItemMeta(meta);

        meta = TOKEN_ITEM.getItemMeta();
        meta.displayName(MessageTools.parseText("<!italic><gold>Token"));
        meta.lore(List.of(MessageTools.parseText("<!italic><yellow>As cheap as it can get")));

        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "token");

        TOKEN_ITEM.setItemMeta(meta);
    }

    private int expensiveTokens;
    private int tokens;

    public Currency(int expensiveTokens, int tokens) {
        this.expensiveTokens = expensiveTokens;
        this.tokens = tokens;
    }

    public Currency(Currency currency) {
        this.expensiveTokens = currency.expensiveTokens;
        this.tokens = currency.tokens;
    }

    public Currency() {
        this.expensiveTokens = 0;
        this.tokens = 0;
    }

    public Currency(Inventory inventory) {
        this.expensiveTokens = ItemTools.countItems(inventory, EXPENSIVE_TOKEN_ITEM);
        this.tokens = ItemTools.countItems(inventory, TOKEN_ITEM);
    }

    public void convertTokensToExpensiveTokens(){
        int extraExpensiveTokens = tokens/64;
        this.expensiveTokens += extraExpensiveTokens;
        this.tokens = this.tokens - 64 * extraExpensiveTokens;
    }

    public void convertExpensiveTokensToTokens() {
        this.tokens = this.tokens + 64 * this.expensiveTokens;
        this.expensiveTokens = 0;
    }

    public Currency multiply(int factor) {
        Currency result = new Currency(this);
        result.setTokens(factor * (result.getTokens() + result.getExpensiveTokens() * 64));
        result.setExpensiveTokens(0);
        result.convertTokensToExpensiveTokens();
        return result;
    }

    /**
     * Divides the balance, but drops any decimals
     */
    public Currency divide(int divisor) {
        Currency result = new Currency(this);
        result.setTokens((result.getTokens() + result.getExpensiveTokens() * 64) /divisor);
        result.setExpensiveTokens(0);
        result.convertTokensToExpensiveTokens();
        return result;
    }

    public Currency add(Currency addend) {
        Currency result = new Currency(this);
        result.setTokens(addend.getTokens() + result.getTokens());
        result.setExpensiveTokens(addend.getExpensiveTokens() + result.getExpensiveTokens());
        result.convertTokensToExpensiveTokens();
        return result;
    }

    public int asTokens() {
        return expensiveTokens *64+tokens;
    }

    public Currency copy() {
        return new Currency(this);
    }

    public int getExpensiveTokens() {
        return expensiveTokens;
    }

    public void setExpensiveTokens(int expensiveTokens) {
        this.expensiveTokens = expensiveTokens;
    }

    public int getTokens() {
        return tokens;
    }

    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    public ItemStack getExpensiveTokenItem() {
        return EXPENSIVE_TOKEN_ITEM.asQuantity(expensiveTokens);
    }

    public ItemStack getTokenItem() {
        return TOKEN_ITEM.asQuantity(tokens);
    }

    /**
     * @return ItemStack[0] = expensive tokens, ItemStack[1] = tokens
     */
    public ItemStack[] itemsAsArray() {
        return List.of(getExpensiveTokenItem(), getTokenItem()).toArray(new ItemStack[2]);
    }
}
