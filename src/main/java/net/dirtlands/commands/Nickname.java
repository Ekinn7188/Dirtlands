package net.dirtlands.commands;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.files.Playerdata;
import net.dirtlands.tabscoreboard.TabMenu;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.entity.Player;

public class Nickname extends PluginCommand {
    Playerdata playerdata = Main.getPlugin().playerdata();
    private static ConfigSetup config = Main.getPlugin().config();

    @Override
    public String getName() {
        return "nickname";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.NICKNAME;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length > 0){
            if (args[0].equals("reset")){
                player.displayName(MessageTools.parseText(player.getName()));
                playerdata.get().set(player.getUniqueId() + ".nickname", null);
                playerdata.save();
                playerdata.reload();
                TabMenu.updateTab();
                player.sendMessage(MessageTools.parseFromPath(config, "Nickname Change", Template.template("Name", player.getName())));
                return;
            }
            player.displayName(MessageTools.parseText(args[0]));
            playerdata.get().set(player.getUniqueId() + ".nickname", args[0]);
            playerdata.save();
            playerdata.reload();
            TabMenu.updateTab();
            player.sendMessage(MessageTools.parseFromPath(config,"Nickname Change", Template.template("Name", player.displayName())));
        }
    }
}
