package fr.mattmunich.civilisation_creatif.commands;

import java.util.List;

import fr.mattmunich.civilisation_creatif.helpers.SidebarManager;
import fr.mattmunich.civilisation_creatif.helpers.SkinManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import fr.mattmunich.civilisation_creatif.Main;
import fr.mattmunich.civilisation_creatif.helpers.Grades;

public class NickCommand implements CommandExecutor, TabCompleter {

    private final Main main;
    
    public NickCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] args) {

        if(s instanceof BlockCommandSender) {
            s.sendMessage("§4Utilisation de Command Blocks désactivée !");
            return true;
        }


        if(!(s instanceof Player p)) {
            s.sendMessage(main.playerToExc);
            return true;
        }

        if (!main.admin.contains(p)) {
            p.sendMessage(main.noPermToExc);
            return true;
        }

        if(args.length < 2 || args.length > 4) {
            p.sendMessage(main.wrongUsage + "/nick <name/target> <grade/name> [grade] [!changeSkin]");
            return true;
        }

        if(args.length == 2 || (args.length == 3 && args[2].contains("!changeSkin"))) {
            Grades grade;

            try {
                grade = Grades.getGradeById(Integer.parseInt(args[1]));
            } catch(NumberFormatException nbe){
                try {
                    grade = Grades.valueOf(args[1].toUpperCase());
                }catch(IllegalArgumentException e) {
                    p.sendMessage(main.prefix + "§4Grade non trouvé !");
                    return true;
                }
            }

            String gPrefix = grade.getPrefix();
            String gSuffix = grade.getSuffix();
            String name = main.hex(gPrefix + args[0] + gSuffix);

            p.setDisplayName(name);
            p.setPlayerListName(name);
            p.setCustomName(name);
            p.setCustomNameVisible(true);

            //Change name + skin
            if(args.length == 3 && args[2].contains("!changeSkin")) {
                p.sendMessage(main.prefix + "§e§oChangement de skin en cours...");
                SkinManager.changeSkin(p,p,args[1]);
                p.sendMessage(main.prefix + "§2Votre nom a été changé en \"§6" + name + "§2\" §5avec changement de skin §2!");
                return true;
            }

            p.sendMessage(main.prefix + "§2Votre nom a été changé en \"§6" + name + "§2\" !");
            //Update name on sidebar
            SidebarManager.updateScoreboard(p);
            return true;
        } else if (args.length == 3 || args[3].contains("!changeSkin")) {
            String targetName = args[0];
            Player target = Bukkit.getPlayer(targetName);

            if(target== null) {
                p.sendMessage(main.playerNotFound(targetName));
                return true;
            }


            Grades grade = null;

            try {
                grade = Grades.getGradeById(Integer.parseInt(args[2]));
            } catch(NumberFormatException nbe){
                try {
                    grade = Grades.valueOf(args[2].toUpperCase());
                }catch(IllegalArgumentException e) {
                    p.sendMessage(main.prefix + "§4Grade non trouvé !");
                    return true;
                }
            }

            String gPrefix = grade.getPrefix();
            String gSuffix = grade.getSuffix();
            String name = main.hex(gPrefix + args[1] + gSuffix);


            target.setDisplayName(name);
            target.setPlayerListName(name);
            target.setCustomName(name);
            target.setCustomNameVisible(true);

            //Change name + skin
            if(args.length == 4 && args[3].contains("!changeSkin")) {
                p.sendMessage(main.prefix + "§e§oChangement de skin en cours...");
                SkinManager.changeSkin(p,target,args[1]);
                p.sendMessage(main.prefix + "§2Le nom de §6" + target.getName() + "§2 a été changé en \"§6" + name + "§2\" §5§avec changement de skin §2!");
                return true;
            }

            p.sendMessage(main.prefix + "§2Le nom de §6" + target.getName() + "§2 a été changé en \"§6" + name + "§2\" !");
            //Update name on sidebar
            SidebarManager.updateScoreboard(target);
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> tabComplete = Lists.newArrayList();

        if(args.length == 1) {
            for(Player p : Bukkit.getOnlinePlayers()) {
                tabComplete.add(p.getName());
            }
        }

        if(args.length == 2) {
            for(Grades grade : Grades.values()) {
                if(grade.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                    tabComplete.add(grade.getName().toLowerCase());
                }
            }
        }

        if(args.length == 3) {
            for(Grades grade : Grades.values()) {
                if(grade.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                    tabComplete.add(grade.getName().toLowerCase());
                }
            }

            tabComplete.add("!changeSkin");
        }

        if(args.length == 4) tabComplete.add("!changeSkin");

        if (args.length > 4) tabComplete.add("⚠️ Trop d'arguments !");

        return tabComplete;
    }

}

