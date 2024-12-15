package fr.mattmunich.civilisation_creatif.commands;

import com.google.common.collect.Lists;
import fr.mattmunich.civilisation_creatif.Main;
import fr.mattmunich.civilisation_creatif.helpers.Grades;
import fr.mattmunich.civilisation_creatif.helpers.PlayerData;
import fr.mattmunich.civilisation_creatif.helpers.Utility;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class XPCommand implements CommandExecutor, TabCompleter {
    private static Main main;
    public XPCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String l, String[] args) {
        if (args.length < 2 || args.length > 3) {
            s.sendMessage(main.wrongUsage + "/xp <player> <add/remove/reset> [xpAmount]");
            return true;
        }

        if(s instanceof Player p) {
            if(!main.dev.contains(p)) {
                p.sendMessage(main.noPermToExc);
                return true;
            }
        }

        PlayerData data = null;
        try {
            data = new PlayerData(Utility.getUUIDFromName(args[0]));
        } catch (Exception e) {
            s.sendMessage(main.prefix + "§4Impossible de trouver le joueur §c" + args[0]);
            return true;
        }

        if(args[1].equalsIgnoreCase("reset")) {
            data.resetXP();
            s.sendMessage(main.prefix + "§2L'§6XP§2 du joueur §6" + args[0] + "§2 a été remis à zéro !");

            if(Bukkit.getPlayer(args[0]) != null) {
                Player p = Bukkit.getPlayer(args[0]);
                assert p != null;
                p.sendMessage(main.prefix + "§4Votre XP a été remis à §czéro§4 !");
            }
            return true;
        }
        if(args[1].equalsIgnoreCase("get")) {
            s.sendMessage(main.prefix + "§2Le joueur §6" + args[0] + "§2 a §6" + data.XP() + "XP §2!");
            return true;
        }

        if(args.length != 3) {
            s.sendMessage(main.prefix + "§4Veuilliez indiquer le nombre d'XP !");
            s.sendMessage(main.wrongUsage + "/xp <player> <add/remove/reset> [xpAmount]");
            return true;
        }

        if(!(args[2].matches("^[0-9]+$"))) {
            s.sendMessage(main.prefix + "§4Veuilliez entrer un nombre (entre 1 et 1000000) !");
            s.sendMessage(main.wrongUsage + "/xp <player> <add/remove/reset> [xpAmount]");
            return true;
        }

        if(args[1].equalsIgnoreCase("add")) {
            data.addXP(Integer.parseInt(args[2]));
            s.sendMessage(main.prefix + "§2Vous avez §aajouté §6" + Integer.parseInt(args[2]) + "XP §2 à §6" + args[0] + "§2 !");

            if(Bukkit.getPlayer(args[0]) != null) {
                Player p = Bukkit.getPlayer(args[0]);
                assert p != null;
                p.sendMessage(main.prefix + "§6" + args[2] + "XP§2 vous ont été ajouté !");
            }
            return true;
        } else if(args[1].equalsIgnoreCase("remove")) {
            data.removeXP(Integer.parseInt(args[2]));
            s.sendMessage(main.prefix + "§2Vous avez §cretiré §6" + Integer.parseInt(args[2]) + "XP §2 à §6" + args[0] + "§2 !");

            if(Bukkit.getPlayer(args[0]) != null) {
                Player p = Bukkit.getPlayer(args[0]);
                assert p != null;
                p.sendMessage(main.prefix + "§6" + args[2] + "XP§4 vous ont été retiré !");
            }
            return true;
        } else if(args[1].equalsIgnoreCase("set")) {
            data.setXP(Integer.parseInt(args[2]));
            s.sendMessage(main.prefix + "§2Le nombre d'XP de §6" + args[0] + "§2 a été §edéfini à §6" + Integer.parseInt(args[2]) + "XP §2!");

            if(Bukkit.getPlayer(args[0]) != null) {
                Player p = Bukkit.getPlayer(args[0]);
                assert p != null;
                p.sendMessage(main.prefix + "Votre nombre d'XP à été §edéfini à §6" + args[2] + "XP §2!");
            }
            return true;
        } else {
            s.sendMessage(main.wrongUsage + "/xp <player> <add/remove/reset> [xpAmount]");
            return true;
        }
    }
    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String l, String[] args) {
        List<String> tabComplete = Lists.newArrayList();

        if(args.length == 1) {
            for(Player p : Bukkit.getOnlinePlayers()) {
                tabComplete.add(p.getName());
            }
        }

        if(args.length == 2) {
           tabComplete.add("add");
            tabComplete.add("remove");
            tabComplete.add("set");
            tabComplete.add("reset");
            tabComplete.add("get");
        }

        if(args.length == 3) {
            tabComplete.add("1");
            tabComplete.add("10");
            tabComplete.add("100");
            tabComplete.add("1000");
            tabComplete.add("10000");
        }

        return tabComplete;
    }
}
