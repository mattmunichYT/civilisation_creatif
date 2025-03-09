package fr.mattmunich.civilisation_creatif.commands;

import fr.mattmunich.civilisation_creatif.helpers.PlayerData;
import fr.mattmunich.civilisation_creatif.helpers.SidebarManager;
import org.bukkit.Bukkit;
import org.bukkit.block.sign.Side;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import fr.mattmunich.civilisation_creatif.Main;
import fr.mattmunich.civilisation_creatif.helpers.Grades;

public class VanishCommand implements CommandExecutor, Listener {

    private final Main main;
    private final Plugin plugin;

    public VanishCommand(Main main, Plugin plugin) {
        this.main = main;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(sender instanceof BlockCommandSender) {
            sender.sendMessage("§4Utilisation de Command Blocks désactivée !");
            return true;
        }

        if(sender instanceof Player p) {

            if(!(main.admin.contains(p))) {
                p.sendMessage(main.noPermToExc);
                return true;
            }

            if(args.length == 0) {
                if(!main.vanished.contains(p)) {
                    vanish(p);
                } else {
                    unvanish(p);
                }
                return true;
            }else if(args.length == 1 || args.length == 2) {
                if(args[0].equalsIgnoreCase("on")) {
                    if(main.vanished.contains(p)) {
                        p.sendMessage(main.prefix + "§4Vous êtes déjà §6invisible aux yeux des autres joueurs !");
                        return true;
                    }
                    vanish(p);
                    return true;
                } else if(args[0].equalsIgnoreCase("off")) {
                    if(!main.vanished.contains(p)) {
                        p.sendMessage(main.prefix + "§cVous êtes déjà §4visible aux yeux des autres joueurs.");
                        return true;
                    }

                    unvanish(p);
                    return true;
                }

                String targetName = args[0];

                Player target = Bukkit.getPlayer(targetName);

                if(target == null) {
                    p.sendMessage(main.playerNotFound(targetName));
                    return true;
                }


                
                if(args.length == 1) {
                    if(!main.vanished.contains(target)) {
                        vanish(p,target);
                    } else {
                        unvanish(p,target);
                    }
                    return true;
                }
                
                if (args[1].equalsIgnoreCase("on")) {
                    if (main.vanished.contains(target)) {
                        p.sendMessage(main.prefix + "§6" + target.getName() + "§4 est déjà §6invisible aux yeux des autres joueurs !");
                        return true;
                    }
                    vanish(p,target);
                    return true;
                } else if (args[1].equalsIgnoreCase("off")) {
                    if (!main.vanished.contains(target)) {
                        p.sendMessage(main.prefix + "§6" + target.getName() + "§4 est déjà §cvisible aux yeux des autres joueurs !");
                        return true;
                    }
                    unvanish(p,target);
                    return true;
                } else {
                    p.sendMessage(main.wrongUsage + "/vanish [on/off/target] [on/off]");
                    return true;
                }
            } else {
                p.sendMessage(main.wrongUsage + "/vanish [on/off/target] [on/off]");
                return true;
            }

        }else {
            sender.sendMessage(main.playerToExc);
            return true;
        }
    }

    public void vanish(Player sender) {
        main.vanished.add(sender);

        for(Player players : Bukkit.getOnlinePlayers()) {
            if (!(main.admin.contains(players) && main.vanished.contains(players))) {
                players.hidePlayer(plugin, sender);
            } else {
                players.showPlayer(plugin, sender);
            }
        }

        sender.setCanPickupItems(false);
        sender.setInvulnerable(true);

        Bukkit.broadcastMessage(main.leaveMessage(sender));

        sender.setPlayerListName(main.vanishNamePrefix + sender.getName());
        sender.setDisplayName(main.vanishNamePrefix + sender.getName());
        sender.setCustomName(main.vanishNamePrefix + sender.getName());

        SidebarManager.updateScoreboard(sender);
        sender.sendMessage(main.prefix + "§2Vous êtes désormais §6invisible aux yeux des autres joueurs !");
    }

    public void vanish(Player sender, Player target) {
        main.vanished.add(target);

        for(Player players : Bukkit.getOnlinePlayers()) {
            if (!(main.admin.contains(players) && main.vanished.contains(players))) {
                players.hidePlayer(plugin, target);
            } else {
                players.showPlayer(plugin, target);
            }
        }

        target.setCanPickupItems(false);
        target.setInvulnerable(true);

        Bukkit.broadcastMessage(main.leaveMessage(target));

        target.setPlayerListName(main.vanishNamePrefix + target.getName());
        target.setDisplayName(main.vanishNamePrefix + target.getName());
        target.setCustomName(main.vanishNamePrefix + target.getName());


        SidebarManager.updateScoreboard(target);
        target.sendMessage(main.prefix + "§2Vous êtes désormais §6invisible aux yeux des autres joueurs !");
        sender.sendMessage(main.prefix + "§6" + target.getName() + "§2 est désormais §6invisible aux yeux des autres joueurs !");
    }

    public void unvanish(Player sender) {
        PlayerData playerData = new PlayerData(sender);
        main.vanished.remove(sender);

        for(Player players : Bukkit.getOnlinePlayers()) {
            players.showPlayer(plugin, sender);
        }

        Grades grade = playerData.getRank();
        if(grade == null) {
            sender.sendMessage(main.prefix + "§4Une erreur s'est produite");
            return;
        }

        String tPrefix = main.hex(grade.getPrefix());
        String tSuffix = main.hex(grade.getSuffix());

        sender.setCanPickupItems(true);
        sender.setInvulnerable(false);

        sender.setPlayerListName(tPrefix + sender.getName() + tSuffix);
        sender.setDisplayName(tPrefix + sender.getName() + tSuffix);
        sender.setCustomName(sender.getPlayerListName());

        Bukkit.broadcastMessage(main.joinMessage(sender));

        SidebarManager.updateScoreboard(sender);
        sender.sendMessage(main.prefix + "§cVous êtes désormais §6visible aux yeux des autres joueurs !");
    }

    public void unvanish(Player sender, Player target) {
        PlayerData playerData = new PlayerData(target);
        main.vanished.remove(target);

        for(Player players : Bukkit.getOnlinePlayers()) {
            players.showPlayer(plugin, target);
        }

        Grades grade = playerData.getRank();
        if(grade == null) {
            sender.sendMessage(main.prefix + "§4Une erreur s'est produite");
            return;
        }

        target.setCanPickupItems(true);
        target.setInvulnerable(false);

        String tPrefix = main.hex(grade.getPrefix());
        String tSuffix = main.hex(grade.getSuffix());

        target.setPlayerListName(tPrefix + target.getName() + tSuffix);
        target.setDisplayName(tPrefix + target.getName() + tSuffix);

        Bukkit.broadcastMessage(main.joinMessage(target));

        SidebarManager.updateScoreboard(target);

        target.setDisplayName(target.getPlayerListName());
        target.sendMessage(main.prefix + "§cVous êtes désormais §6visible aux yeux des autres joueurs !");
        sender.sendMessage(main.prefix + "§6" + target.getName() + "§c est désormais §6visible aux yeux des autres joueurs !");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if(!main.admin.contains(player)) {
            player.hidePlayer(plugin, player);
        } else {
            player.showPlayer(plugin, player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        unvanish(e.getPlayer());
    }
}

