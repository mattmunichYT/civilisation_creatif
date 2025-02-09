package fr.mattmunich.civilisation_creatif.commands;

import com.google.common.collect.Lists;
import fr.mattmunich.civilisation_creatif.Main;
import fr.mattmunich.civilisation_creatif.helpers.PlayerData;
import fr.mattmunich.civilisation_creatif.helpers.Utility;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class MoneyCommand implements CommandExecutor, TabCompleter {
    private static Main main;
    public MoneyCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String l, String[] args) {
        if (args.length == 0 || args.length > 3) {
            if(s instanceof Player p) {
                if(!main.modo.contains(p)) {
                    s.sendMessage(main.wrongUsage + "/money <player> [get]");
                    return true;
                }
            }
            s.sendMessage(main.wrongUsage + "/money <player> <add/remove/reset/get> [moneyAmount]");
            return true;
        }
        PlayerData data = null;
        try {
            data = new PlayerData(Utility.getUUIDFromName(args[0]));
        } catch (Exception e) {
            s.sendMessage(main.playerNotFound(args[0]));
            return true;
        }
        if(args.length==1) {
            s.sendMessage(main.prefix + "§2Le joueur §6" + args[0] + "§2 a §6" + data.Money() + main.moneySign + "§2 !");
            return true;
        }



        if(args[1].equalsIgnoreCase("reset")) {
            if(s instanceof Player p) {
                if(!main.modo.contains(p)) {
                    p.sendMessage(main.noPermToExc);
                    return true;
                }
            }
            data.resetMoney();
            s.sendMessage(main.prefix + "§2L'§6argent§2 du joueur §6" + args[0] + "§2 a été remis à zéro !");

            if(Bukkit.getPlayer(args[0]) != null) {
                Player p = Bukkit.getPlayer(args[0]);
                assert p != null;
                p.sendMessage(main.prefix + "§4Votre argent a été remis à §czéro§4 !");
            }
            return true;
        }
        if(args[1].equalsIgnoreCase("get")) {
            s.sendMessage(main.prefix + "§2Le joueur §6" + args[0] + "§2 a §6" + data.Money() + main.moneySign + "§2 !");
            return true;
        }
        if(s instanceof Player p) {
            if(!main.modo.contains(p)) {
                p.sendMessage(main.noPermToExc);
                return true;
            }
        }

        if(args.length != 3) {
            s.sendMessage(main.prefix + "§4Veuilliez indiquer le nombre d'argent !");
            s.sendMessage(main.wrongUsage + "/money <player> <add/remove/reset/get> [moneyAmount]");
            return true;
        }

        if(!(args[2].matches("^[0-9]+$"))) {
            s.sendMessage(main.prefix + "§4Veuilliez entrer un nombre (entre 1 et 1000000) !");
            s.sendMessage(main.wrongUsage + "/money <player> <add/remove/reset/get> [moneyAmount]");
            return true;
        }

        if(args[1].equalsIgnoreCase("add")) {
            data.addMoney(Integer.parseInt(args[2]));
            s.sendMessage(main.prefix + "§2Vous avez §aajouté §6" + Integer.parseInt(args[2]) + main.moneySign + " §2 à §6" + args[0] + "§2 !");

            if(Bukkit.getPlayer(args[0]) != null) {
                Player p = Bukkit.getPlayer(args[0]);
                assert p != null;
                p.sendMessage(main.prefix + "§6" + args[2] + main.moneySign + "§2 ont été ajouté à votre compte !");
            }
            return true;
        } else if(args[1].equalsIgnoreCase("remove")) {
            data.removeMoney(Integer.parseInt(args[2]));
            s.sendMessage(main.prefix + "§2Vous avez §cretiré §6" + Integer.parseInt(args[2]) + main.moneySign + " §2 à §6" + args[0] + "§2 !");

            if(Bukkit.getPlayer(args[0]) != null) {
                Player p = Bukkit.getPlayer(args[0]);
                assert p != null;
                p.sendMessage(main.prefix + "§6" + args[2] + main.moneySign + "§4 ont été retiré de votre compte !");
            }
            return true;
        } else if(args[1].equalsIgnoreCase("set")) {
            data.setMoney(Integer.parseInt(args[2]));
            s.sendMessage(main.prefix + "§2L'argent de §6" + args[0] + "§2 a été §edéfini à §6" + Integer.parseInt(args[2]) + main.moneySign + " §2!");

            if(Bukkit.getPlayer(args[0]) != null) {
                Player p = Bukkit.getPlayer(args[0]);
                assert p != null;
                p.sendMessage(main.prefix + "Votre somme d'argent à été §edéfinie à §6" + args[2] + main.moneySign + " §2!");
            }
            return true;
        } else {
            s.sendMessage(main.wrongUsage + "/money <player> <add/remove/reset/get> [moneyAmount]");
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
