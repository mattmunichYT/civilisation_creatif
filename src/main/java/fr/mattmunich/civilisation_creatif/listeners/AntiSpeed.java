package fr.mattmunich.civilisation_creatif.listeners;

import fr.mattmunich.civilisation_creatif.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

public class AntiSpeed implements Listener {

    private final Main main;

    public AntiSpeed(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        double speed = e.getFrom().distance(Objects.requireNonNull(e.getTo()));

        if (speed > 5.0 && !(p.getWalkSpeed() > 2) && !(p.getFlySpeed() > 1) && !p.isGliding() && !main.dev.contains(p)) {
            p.sendMessage(main.prefix + "§eVous allez trop vite !");
            main.speeding.add(p);
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
                @Override
                public void run() {
                    main.speeding.remove(p);
                    for (Player all : Bukkit.getOnlinePlayers()) {
                    Bukkit.getConsoleSender().sendMessage(main.prefix + "The player §c" + p.getName()
                            + "§6was speeding with at §c" + speed * 20 + "§4 blocks/s§6!");
                    }
                }
            }, 60);
        }

        if (main.speeding.contains(p)) {
            e.setCancelled(true);
            p.sendTitle("§e§lVous allez trop vite !", "§6Vous serez unfreeze dans 3 secondes", 5, 60, 5);
        }
    }
}
