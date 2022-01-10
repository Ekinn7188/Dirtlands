package net.dirtlands.commands;

import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.entity.Player;

public class GarpSlap extends PluginCommand {
    @Override
    public String getName() {
        return "garp";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("slap")) {
            player.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Correct Usage", Template.template("command", "/garp slap")));
            return;
        }
        player.chat("/warp shop");
    }
}
