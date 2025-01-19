package fr.mattmunich.civilisation_creatif.commands;

import com.google.common.collect.Lists;
import fr.mattmunich.civilisation_creatif.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class CivlisationCommand implements CommandExecutor, TabCompleter {

    private static Main main;
    public CivlisationCommand(Main main) { this.main = main; }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String l, String[] args) {

        if(args.length == 0) {
            s.sendMessage(main.wrongUsage + "/civilisation [credits/reload]");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if(s instanceof Player p) {
                if(!main.admin.contains(p)) {
                    p.sendMessage(main.noPermToExc);
                    return true;
                }
            }
            s.sendMessage(main.prefix + "§2§oRechargement du plugin... §8Cela peut prendre un certain temps.");
//          main.onDisable(); -- NOT NEEDED (+ auto tranfer to mjep onDisable)
            main.onLoad();
            main.onEnable();
            s.sendMessage(main.prefix + "§2Plugin rechargé !");
        } else if(args[0].equalsIgnoreCase("credits") || args[0].equalsIgnoreCase("copyright")) {
            s.sendMessage(main.prefix + "§6§lCrédits : §2Made by §6mattmunich\n§aCopyright ©2024 mattmunich All rights reserved.");
        } else if(args[0].equalsIgnoreCase("version")) {
            s.sendMessage("§2§6Civlisation §6Créatif §2Version " + main.version +"\n§aMinecraft version 1.21.1 or later");
        } else {
            s.sendMessage(main.wrongUsage + "/civilisation [credits/reload]");
        }
        return true;
    }
    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String l, String[] args) {
        List<String> tabComplete = Lists.newArrayList();

        if(args.length == 1) {
            tabComplete.add("credits");
            tabComplete.add("version");
            if(s instanceof Player p) {
                if(main.admin.contains(p)) {
                    tabComplete.add("reload");
                }
            } else {
                tabComplete.add("reload");
            }
        }

        return tabComplete;
    }
}
