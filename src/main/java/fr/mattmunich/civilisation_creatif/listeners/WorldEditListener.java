package fr.mattmunich.civilisation_creatif.listeners;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import fr.mattmunich.civilisation_creatif.Main;
import fr.mattmunich.civilisation_creatif.helpers.TerritoryData;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

public class WorldEditListener implements Listener {

    private final Main main;
    private final TerritoryData territoryData;

    public WorldEditListener(Main main, TerritoryData territoryData) {
        this.main=main;
        this.territoryData=territoryData;
    }

    @Subscribe
    public void onEdit(EditSessionEvent event) {
        if (event.getStage() != EditSession.Stage.BEFORE_CHANGE) return;
        event.setCancelled(false);

        Actor actor = event.getActor();
        if (actor == null || actor.getName() == null) {
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Couldn't get actor for WorldEdit event.");
            return;
        }

        Player player = Bukkit.getPlayerExact(actor.getName());

        Extent extent = event.getExtent();
        BlockVector3 min = extent.getMinimumPoint();
        BlockVector3 max = extent.getMaximumPoint();

        if (player == null) {
            Bukkit.getLogger().severe("IllegalEditCheckError at 45 in WorldEditListener.java:");
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Couldn't find Bukkit player for actor: " + actor.getName());
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§c=> Allowed edit from §e" + min.x() + ", " + min.y() + ", " + min.z() + " §4to §e" + max.x() + ", " + max.y() + ", " + max.z() + "§4 !");
            return;
        }

        String playerTerritory = territoryData.getPlayerTerritory(player);

        Set<Chunk> checkedChunks = new HashSet<>();

        if(!main.bypassClaims.contains(player)) {
            for (int x = min.x(); x <= max.x(); x++) {
                for (int z = min.y(); z <= max.y(); z++) {
                    Chunk chunk = player.getWorld().getChunkAt(x >> 4, z >> 4);
                    if (checkedChunks.contains(chunk)) continue;
                    checkedChunks.add(chunk);

                    String chunkOwner = territoryData.getChunkOwner(chunk);
                    if (!playerTerritory.equals(chunkOwner) && !main.bypassClaims.contains(player)) {
                        player.sendMessage(main.prefix + "§4Vous n'avez pas la permission d'utiliser WorldEdit dans un territoire autre que le vôtre !");
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
}
