package fr.mattmunich.civilisation_creatif.listeners;

import fr.mattmunich.civilisation_creatif.Main;
import fr.mattmunich.civilisation_creatif.helpers.*;
import fr.mattmunich.civilisation_creatif.territories.TerritoryData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class EventListener implements Listener {


    private final Main main;

    public EventListener(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        e.setQuitMessage(main.leaveMessage(p));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        PlayerData data = new PlayerData(p);

        Grades pRank = data.getRank();
        if (pRank == null) {
            p.sendMessage(main.prefix + "§4Une erreur s'est produite lors de l'envoi de se message ! §cEssayez de vous déconnecter et de vous reconnecter.");
            e.setCancelled(true);
            return;
        }
        String chatSeparator = pRank.getChatSeparator();

        e.setFormat(main.hex(p.getDisplayName() + chatSeparator
                + ChatColor.translateAlternateColorCodes('&', String.join(" ", e.getMessage()))));
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        e.setCancelled(true);
        Player p = e.getPlayer();
        p.sendTitle("§4§lVous avez été kick", "§e§oTransfert vers §4M.§cJ.§6E.§eP.", 20, 100, 20);
        p.sendMessage("§e(§6!§e) §4Vous avez été kick du serveur Civlisation Créatif avec la raison : §c" + e.getReason());
        PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 120, 255, false, false, false);
        p.addPotionEffect(blindness);
        Bukkit.getScheduler().runTaskLater(main, () -> {
            p.transfer("91.197.6.60", 25599);
        }, 120);
    }

    @EventHandler
    public void onCristal(EntityExplodeEvent e) {
        for (Entity nEntity : e.getEntity().getNearbyEntities(10, 10, 10)) {
            if (nEntity instanceof Player p) {
                p.sendMessage(main.prefix + "§4Les explosions sont désactivées sur le serveur !");
                Bukkit.getConsoleSender()
                        .sendMessage(main.prefix + "§c" + p.getName()
                                + "§4 a essayé de faire exploser une entité aux coordonnées : §cX: "
                                + e.getEntity().getLocation().getBlockX() + "§4, §cY: "
                                + e.getEntity().getLocation().getBlockY() + "§4, §cZ: "
                                + e.getEntity().getLocation().getBlockZ());
            }
        }
        e.getEntity().remove();
        e.setCancelled(true);
    }

    @EventHandler
    public void onExplosion(ExplosionPrimeEvent e) {
        for (Entity nEntity : e.getEntity().getNearbyEntities(10, 10, 10)) {
            if (nEntity instanceof Player p) {
                p.sendMessage(main.prefix + "§4Les TNTs sont désactivées sur le serveur !");
                Bukkit.getConsoleSender()
                        .sendMessage(main.prefix + "§c" + p.getName()
                                + "§4 a essayé de faire exploser une TNT aux coordonnées : §cX: "
                                + e.getEntity().getLocation().getBlockX() + "§4, §cY: "
                                + e.getEntity().getLocation().getBlockY() + "§4, §cZ: "
                                + e.getEntity().getLocation().getBlockZ());
            }
        }
        e.setFire(false);
        e.getEntity().remove();
        e.setCancelled(true);
    }

    @EventHandler
    public void onRespawnAnchorExplosion(BlockExplodeEvent e) {
        Block block = e.getBlock();
        ArrayList<String> suspects = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(block.getLocation()) < 8) {
                suspects.add(player.getName());
            }
        }
        Bukkit.getConsoleSender()
                .sendMessage(main.prefix + "§cUn des joueurs dans la liste suivante "
                        + "§4 a essayé de faire exploser un block aux coordonnées : §cX: "
                        + e.getBlock().getLocation().getBlockX() + "§4, §cY: "
                        + e.getBlock().getLocation().getBlockY() + "§4, §cZ: "
                        + e.getBlock().getLocation().getBlockZ()
                        + " §4Suspects : §c" + suspects.stream());

        e.setCancelled(true);
        try {
            for (Block b : e.blockList()) {
                b.getWorld().setBlockData(b.getLocation(), b.getBlockData());
            }
        } catch (Exception err) {
            Bukkit.getConsoleSender().sendMessage("Couldn't replace blocks after explosion : " + err.fillInStackTrace());
        }
        e.setYield(0);
    }
}
