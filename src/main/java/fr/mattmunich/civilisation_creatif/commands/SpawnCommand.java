package fr.mattmunich.civilisation_creatif.commands;

import com.google.common.collect.Lists;
import fr.mattmunich.civilisation_creatif.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.Objects;

public class SpawnCommand implements CommandExecutor, TabCompleter {
    private Main main;

    public SpawnCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String l, String[] args) {
        if(!(s instanceof Player)) {
            s.sendMessage(main.playerToExc);
            return true;
        }
        Player p = (Player)s;
        if(args.length == 1) {
            try {
                World world = Bukkit.getWorld(args[0]);
                if (world==null){
                    if(args[0].equalsIgnoreCase("overworld")) {
                        p.teleport(Objects.requireNonNull(Bukkit.getWorld("world")).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                        p.sendMessage(main.prefix + "§2Vous avez été téléporté au §6spawn§2 de l'§aOverworld§2 !");
                    } else if(args[0].equalsIgnoreCase("nether")) {
                        p.teleport(Objects.requireNonNull(Bukkit.getWorld("world_nether")).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                        p.sendMessage(main.prefix + "§2Vous avez été téléporté au §6spawn§2 du §cNether§2 !");
                    } else if(args[0].equalsIgnoreCase("end")) {
                        p.teleport(Objects.requireNonNull(Bukkit.getWorld("world_the_end")).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                        p.sendMessage(main.prefix + "§2Vous avez été téléporté au §6spawn§2 de l'§5End§2 !");
                    } else {
                        p.sendMessage(main.prefix + "§4Monde non trouvé !");
                    }
                }
                p.teleport(world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                p.sendMessage(main.prefix + "§2Vous avez été téléporté au §6spawn§2 du monde §6" + world.getName() + " §2!");
                return true;
            } catch (Exception e) {
                main.logError("Couldn't tp player to spawn of world" + args[0],e);
                p.sendMessage(main.prefix  + "§4Une erreur s'est produite.");
                return true;
            }
        } else if (args.length == 0) {
            p.teleport(Objects.requireNonNull(Bukkit.getWorld("spawn")).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            p.sendMessage(main.prefix + "§2Vous avez été téléporté au §6spawn§2 !");
            return true;
        } else {
            p.sendMessage(main.wrongUsage + "/spawn [world]");
            return true;
        }
    }
    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String l, String[] args) {
        List<String> tabComplete = Lists.newArrayList();

        if(args.length == 1) {
            tabComplete.add("spawn");
            tabComplete.add("overworld");
            tabComplete.add("nether");
            tabComplete.add("end");
        }

        return tabComplete;
    }
}
