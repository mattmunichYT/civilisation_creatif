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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RankCommand implements CommandExecutor, TabCompleter {

    private static Main main;
    public RankCommand(Main main) { this.main = main;}

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String l, String[] args) {

        if(s instanceof Player p) {
            if(!main.admin.contains(p)) {
                p.sendMessage(main.noPermToExc);
                return true;
            }
        }

        if(args.length != 2) {
            s.sendMessage(main.wrongUsage + "/rank <player> <rank>");
            return true;
        }

        PlayerData data = null;
        try {
            data = new PlayerData(Utility.getUUIDFromName(args[0]));
        } catch (Exception e) {
            s.sendMessage(main.playerNotFound(args[0]));
            return true;
        }

        if(data.getRank() == null) {
            s.sendMessage(main.prefix + "§4Impossible d'obtenir le grade de §c" + args[0] + "§4 !");
            return true;
        }

        if(args[1].equalsIgnoreCase("get")) {

            String gradePrefix = main.hex(data.getRank().getPrefix());

            s.sendMessage(main.prefix + "§2Le joueur §6" + args[0] + "§2 a le grade " + gradePrefix);
            return true;
        }

        String gradeName = args[1];

        if(!Arrays.toString(Grades.values()).contains(gradeName.toUpperCase())) {
            s.sendMessage(main.prefix + "§4Le grade à donner n'§cexiste pas §4!");
            return true;
        }

        Grades grade = Grades.valueOf(gradeName.toUpperCase());
        gradeName = grade.getName();
        String gradePrefix = main.hex(grade.getPrefix());

        data.setRank(gradeName);
        s.sendMessage(main.prefix + "§2Le grade " + gradePrefix + "§2a été donné à §6" + args[0] + " §2!");
        if(Bukkit.getPlayer(args[0]) != null) {
            Player p = Bukkit.getPlayer(args[0]);
            assert p != null;
            p.sendMessage(main.prefix + "§2Vous avez reçu le grade " + gradePrefix + "§2 !");
        }
        return true;
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
            for(Grades grades : Grades.values()) {
                if(grades.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    tabComplete.add(grades.getName().toLowerCase());
                }
            }
        }

        return tabComplete;
    }
}
