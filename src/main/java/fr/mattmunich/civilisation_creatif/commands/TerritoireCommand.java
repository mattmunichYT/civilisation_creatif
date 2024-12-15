package fr.mattmunich.civilisation_creatif.commands;

import fr.mattmunich.civilisation_creatif.Main;
import fr.mattmunich.civilisation_creatif.helpers.TerritoryData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class TerritoireCommand implements CommandExecutor {

    private final Main main;

    private final TerritoryData territoryData;

    public TerritoireCommand(Main main, TerritoryData territoryData) {
        this.main = main;
        this.territoryData = territoryData;
    }
    @Override
    public boolean onCommand(CommandSender s, Command cmd, String l, String[] args) {
        if(!(s instanceof Player p)) {
            s.sendMessage(main.playerToExc);
            return true;
        }

        if(args.length==1) {
            if(args[0].equalsIgnoreCase("map") || args[0].equalsIgnoreCase("nearbyClaims")) {
                int x = p.getLocation().getChunk().getX();
                int z = p.getLocation().getChunk().getZ();
                int ownerCount = 0;
                List<String> owners = new ArrayList<>();
                List<String> chunksOwners = new ArrayList<>();
                Map<String, Integer> ownerID = new HashMap<>();
                for (int chunkX = x-3; chunkX < x+3; chunkX++) {
                    for (int chunkZ = z-3; chunkZ < z+3; chunkZ++) {
                        p.sendMessage("Checking for chunk " + chunkX + "," + chunkZ);
                        Map<Integer,Integer> chunk = new HashMap<>();
                        chunk.put(chunkX,chunkZ);
                        String chunkOwner = territoryData.getChunkOwner(chunk);
                        p.sendMessage("chunk owner:" + chunkOwner);
                        if(chunkOwner==null) {
                            chunksOwners.add("o");
                            p.sendMessage("chunk not owned");
                            continue;
                        }
                        if(!owners.contains(chunkOwner)) {
                            owners.add(chunkOwner);
                            ownerCount+=1;
                            ownerID.put(chunkOwner,ownerCount);
                            chunksOwners.add(String.valueOf(ownerID.get(chunkOwner)));
                            p.sendMessage("chunk owned by created profile " + chunkOwner + " with id " + ownerCount);
                        } else {
                            chunksOwners.add(String.valueOf(ownerID.get(chunkOwner)));
                            p.sendMessage("chunk was added to profile " + chunkOwner + " ; ID : " + ownerID.get(chunkOwner));
                        }
                    }
                }
                p.sendMessage(chunksOwners.toString());
                //MAP EXAMPLE =
                /*
                * 0 o o o 1 1 o
                * 2 2 o 1 1 1 1
                * 2 2 2 o 1 1 o
                * 2 2 2 X o 1 1
                * 2 2 o 1 1 1 1
                * 2 o 3 o o o o
                * o 3 3 3 o o o
                *
                * o=not owned
                * 1=terrExample1
                * 2=terrExample2
                * 3=terrExample3
                * X = position //TODO (mark player pos on nearbyTerrClaimsMap)
                * */
                chunksOwners.add(6, "\n");
                chunksOwners.add(13, "\n");// == 12+1 : not 12 bc we added an element in previous line
                chunksOwners.add(20, "\n");// == 18+2
                chunksOwners.add(27, "\n");// == 24+3
                chunksOwners.add(34, "\n");// == 30+4
                chunksOwners.add(41, "\n");// == 36+5
                StringBuilder formattedMap = new StringBuilder(chunksOwners.toString().replace("[", "").replace("\"", "").replace(",", "").replace("]", "").replace(" ","") + "\n\n§aLégende:\n");
                for (String owner : owners) {
                    int id = ownerID.get(owner);
                    formattedMap.append("§a- ").append(id).append("§2 : ").append(owner).append("\n");
                }
                p.sendMessage(main.prefix + "§6Voici la carte des claims à proximité de vous :\n" + formattedMap);
                return true;
            }
        }


        if(territoryData.getTerritoryTeamOfPlayer(p) == null) {
            if(args.length==1) {
                if(args[0].equalsIgnoreCase("claim") || args[0].equalsIgnoreCase("claimChunk")) {
                    p.sendMessage(main.prefix + "§cVous ne pouvez pas §4claim de chunk §ecar vous ne faites partie d'aucun territoire !");
                    return true;
                }
                if(args[0].equalsIgnoreCase("unclaim") || args[0].equalsIgnoreCase("unclaimChunk")) {
                    p.sendMessage(main.prefix + "§cVous ne pouvez pas §4unclaim de chunk §ecar vous ne faites partie d'aucun territoire !");
                    return true;
                }
            }

            Inventory hasNoTerrMenu = Bukkit.createInventory(p, 27, "§6Menu §7- §a/territoire §7(§8§oAucun§7)");
            for (int i = 0; i <= 26; i++) {
                ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta paneMeta = pane.getItemMeta();
                assert paneMeta != null;
                paneMeta.setItemName("");
                paneMeta.setHideTooltip(true);
                pane.setItemMeta(paneMeta);
                hasNoTerrMenu.setItem(i, pane);
            }
            ItemStack joinItem = new ItemStack(Material.END_CRYSTAL);
            ItemMeta joinMeta = joinItem.getItemMeta();
                assert joinMeta != null;
                joinMeta.setItemName("§b\uD83D\uDC64➕ Rejoindre un territoire");
            joinItem.setItemMeta(joinMeta);
            hasNoTerrMenu.setItem(14, joinItem);

            ItemStack createTerrItem = new ItemStack(Material.CRAFTING_TABLE);
            ItemMeta createTerrMeta = createTerrItem.getItemMeta();
                assert createTerrMeta != null;
                createTerrMeta.setItemName("§2➕ Créer son territoire");
            createTerrItem.setItemMeta(createTerrMeta);
            hasNoTerrMenu.setItem(13, createTerrItem);

            ItemStack viewTerrsItem = new ItemStack(Material.SPYGLASS);
            ItemMeta viewTerrsMeta = viewTerrsItem.getItemMeta();
                assert viewTerrsMeta != null;
                viewTerrsMeta.setItemName("§5\uD83D\uDD0E Voir la liste des territoires");
            viewTerrsItem.setItemMeta(viewTerrsMeta);
            hasNoTerrMenu.setItem(12, viewTerrsItem);

            ItemStack closeItem = new ItemStack(Material.BARRIER);
            ItemMeta closeItemMeta = closeItem.getItemMeta();
                assert closeItemMeta != null;
                closeItemMeta.setItemName("§4❌ Fermer le menu");
            closeItem.setItemMeta(closeItemMeta);
            hasNoTerrMenu.setItem(26, closeItem);

            p.openInventory(hasNoTerrMenu);
        } else {
            if(args.length==1){
                if(args[0].equalsIgnoreCase("claim") || args[0].equalsIgnoreCase("claimChunk")) {
                    if(!Objects.equals(territoryData.getTerritoryChiefUUID(territoryData.getPlayerTerritory(p)), p.getUniqueId().toString())) {
                        p.sendMessage(main.prefix + "§cVous n'avez pas §4la permission §cde §4claim des chunk §cpour votre territoire !");
                        return true;
                    }
                    int x = p.getLocation().getChunk().getX();
                    int z = p.getLocation().getChunk().getZ();
                    Map<Integer,Integer> chunk = new HashMap<>();
                    chunk.put(x,z);
                    territoryData.claimChunk(p, territoryData.getPlayerTerritory(p), chunk);
                    return true;
                }
                if(args[0].equalsIgnoreCase("unclaim") || args[0].equalsIgnoreCase("unclaimChunk")) {
                    if(!Objects.equals(territoryData.getTerritoryChiefUUID(territoryData.getPlayerTerritory(p)), p.getUniqueId().toString())) {
                        p.sendMessage(main.prefix + "§cVous n'avez pas §4la permission §cde §4unclaim des chunk §cpour votre territoire !");
                        return true;
                    }
                    int x = p.getLocation().getChunk().getX();
                    int z = p.getLocation().getChunk().getZ();
                    Map<Integer,Integer> chunk = new HashMap<>();
                    chunk.put(x,z);
                    territoryData.unclaimChunk(p, territoryData.getPlayerTerritory(p), chunk);
                    return true;
                }
            }


            Inventory hasTerrMenu = Bukkit.createInventory(p, 27, "§6Menu §7- §a/territoire §7(" + territoryData.getTerritoryTeamOfPlayer(p).getColor() + territoryData.getTerritoryTeamOfPlayer(p).getName() + "§7)");
            for (int i = 0; i <= 26; i++) {
                ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta paneMeta = pane.getItemMeta();
                assert paneMeta != null;
                paneMeta.setHideTooltip(true);
                pane.setItemMeta(paneMeta);
                hasTerrMenu.setItem(i, pane);
            }
            ItemStack quitTerrItem = new ItemStack(Material.RED_DYE);
            ItemMeta quitTerrMeta = quitTerrItem.getItemMeta();
                assert quitTerrMeta != null;
                quitTerrMeta.setItemName("§c\uD83D\uDC64❌ Quitter son territoire");
            quitTerrItem.setItemMeta(quitTerrMeta);
            hasTerrMenu.setItem(14, quitTerrItem);

            ItemStack modifyTerrItem = new ItemStack(Material.PAPER);
            ItemMeta modifyTerrMeta = modifyTerrItem.getItemMeta();
                assert modifyTerrMeta != null;
                modifyTerrMeta.setItemName("§e✎ Voir son territoire");
            modifyTerrItem.setItemMeta(modifyTerrMeta);
            hasTerrMenu.setItem(13, modifyTerrItem);

            ItemStack viewTerrsItem2 = new ItemStack(Material.SPYGLASS);
            ItemMeta viewTerrsMeta2 = viewTerrsItem2.getItemMeta();
                assert viewTerrsMeta2 != null;
                viewTerrsMeta2.setItemName("§5\uD83D\uDD0E Voir la liste des territoires");
            viewTerrsItem2.setItemMeta(viewTerrsMeta2);
            hasTerrMenu.setItem(12, viewTerrsItem2);

            ItemStack closeItem2 = new ItemStack(Material.BARRIER);
            ItemMeta closeItemMeta2 = closeItem2.getItemMeta();
                assert closeItemMeta2 != null;
                closeItemMeta2.setItemName("§4❌ Fermer le menu");
            closeItem2.setItemMeta(closeItemMeta2);
            hasTerrMenu.setItem(26, closeItem2);

            p.openInventory(hasTerrMenu);
        }

        return true;
    }
}
