package net.dirtlands.commands.essentials.admin;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.files.NpcInventory;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Metadata extends PluginCommand {
    NpcInventory npcInventory = Main.getPlugin().npcInventory();
    private static ConfigSetup config = Main.getPlugin().config();

    @Override
    public String getName() {
        return "metadata";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.METADATA;
    }

    @Override
    public void execute(Player player, String[] args) {
        ItemStack inHand = player.getInventory().getItemInMainHand();

        int metadataIndex = 0;
        ConfigurationSection metadataSection = npcInventory.get().getConfigurationSection("Metadata");
        if (metadataSection != null){
            metadataIndex = metadataSection.getKeys(false).size();
        }

        npcInventory.get().set("Metadata." + metadataIndex, inHand.serialize());
        npcInventory.save();
        npcInventory.reload();

        player.sendMessage(MessageTools.parseFromPath(config, "Metadata Message", Template.template("index", String.valueOf(metadataIndex))));
    }
}
