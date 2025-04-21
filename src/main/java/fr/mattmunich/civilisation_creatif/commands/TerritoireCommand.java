package fr.mattmunich.civilisation_creatif.commands;

import com.google.common.collect.Lists;
import fr.mattmunich.civilisation_creatif.Main;
import fr.mattmunich.civilisation_creatif.helpers.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class TerritoireCommand implements CommandExecutor, TabCompleter {

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

        PlayerData playerData = new PlayerData(p);

        if(args.length>=1 && args.length<=4) {
            int mapRenderRange = 4;
//            if(args.length==2){
//                if(args[0].matches("1-9")){
////                    mapRenderRange = Integer.parseInt(args[0]);
//                }
//            }
            if(args[0].equalsIgnoreCase("map") || args[0].equalsIgnoreCase("showClaimsMap")) {

                int x = p.getLocation().getChunk().getX();
                int z = p.getLocation().getChunk().getZ();
                int ownerCount = 0;
                List<String> owners = new ArrayList<>();
                List<String> chunksOwners = new ArrayList<>();
                Map<String, Integer> ownerID = new HashMap<>();
                for (int chunkX = x- mapRenderRange; chunkX < x+ mapRenderRange; chunkX++) {
                    for (int chunkZ = z-mapRenderRange; chunkZ < z+ mapRenderRange; chunkZ++) {
//                        p.sendMessage("Checking for chunk " + chunkX + "," + chunkZ);
                        Map<Integer,Integer> chunk = new HashMap<>();
                        chunk.put(chunkX,chunkZ);
                        String chunkOwner = territoryData.getChunkOwner(chunk);
//                        p.sendMessage("chunk owner:" + chunkOwner);
                        if(chunkOwner==null) {
                            chunksOwners.add("§r- ");
//                            p.sendMessage("chunk not owned");
                            continue;
                        }
                        if(!owners.contains(chunkOwner)) {
                            owners.add(chunkOwner);
                            ownerCount+=1;
                            ownerID.put(chunkOwner,ownerCount);
                            chunksOwners.add("§" + territoryData.getTerritoryTeam(chunkOwner).getColor() + ownerID.get(chunkOwner) + " ");
//                            p.sendMessage("chunk owned by created profile " + chunkOwner + " with id " + ownerCount);
                        } else {
                            chunksOwners.add("§" + territoryData.getTerritoryTeam(chunkOwner).getColor() + ownerID.get(chunkOwner) + " ");
//                            p.sendMessage("chunk was added to profile " + chunkOwner + " ; ID : " + ownerID.get(chunkOwner));
                        }
                    }
                }
                int columsNum = 8;
//                p.sendMessage(chunksOwners.toString());
                //MAP EXAMPLE =
                /*
                *     A B C D E F G
                *     - - - - - - -
                * a | - - - - 1 1 -
                * b | 2 2 o 1 1 1 1
                * c | 2 2 2 - 1 1 o
                * d | 2 2 2 X o 1 1
                * e | 2 2 o 1 1 1 1
                * f | 2 - 3 - - - -
                *
                * o=not owned
                * 1=terrExample1
                * 2=terrExample2
                * 3=terrExample3
                * X = position //TODO (mark player pos on nearbyTerrClaimsMap)
                * */
                chunksOwners.add(0, """
                            §eA B C D E F G H
                            - - - - - - - -
                        a |\s""");
                chunksOwners.add(columsNum+1, "\n§eb | ");  // V FOR columsNum 6 V
                chunksOwners.add(columsNum*2+2, "\n§ec | ");// == 12+2 : not 12 bc we added an elements in previous lines
                chunksOwners.add(columsNum*3+3, "\n§ed | ");// == 18+3
                chunksOwners.add(columsNum*4+4, "\n§ee | ");// == 24+4
                chunksOwners.add(columsNum*5+5, "\n§ef | ");// == 30+5
                chunksOwners.add(columsNum*6+6, "\n§eg | ");// == 36+6
                chunksOwners.add(columsNum*7+7, "\n§eh | ");// == 42+7
                chunksOwners.add(columsNum*8+8, "\n\n\n"); // == 48+8
                StringBuilder formattedMap = new StringBuilder(chunksOwners.toString().replace("[", "").replace("\"", "").replace(", ", "").replace("]", "") + "\n\n§aLégende:\n");
                for (String owner : owners) {
                    int id = ownerID.get(owner);
                    formattedMap.append("§a- §").append(territoryData.getTerritoryTeam(owner).getColor()).append(id).append("§2 : ").append(owner).append("\n");
                }
                p.sendMessage(main.prefix + "§6Voici la carte des claims à proximité de vous :\n" + formattedMap);
                return true;
            }
            if(args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("showClaims") || args[0].equalsIgnoreCase("showNearbyClaims")) {
                if(main.seeTerritoryBorders.contains(p)) {
                    main.seeTerritoryBorders.remove(p);
                    p.sendMessage(main.prefix + "§cVous ne pouvez désormais plus voir la délimitation des territoires à proximité de vous !");
                } else {
                    main.seeTerritoryBorders.add(p);
                    p.sendMessage(main.prefix + "§2Vous pouvez désormais voir la délimitation des territoires à proximité de vous !");
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("buyWorker")) {
                if (!territoryData.isChief(p, territoryData.getPlayerTerritory(p)) && !territoryData.isOfficer(p, territoryData.getPlayerTerritory(p))) {
                    p.sendMessage(main.prefix + "§4Vous devez être le chef/un officier de votre territoire pour faire cela!");
                    return true;
                }
                territoryData.showBuyWorkerInv(p);
                return true;
            }


            if(args[0].equalsIgnoreCase("gui") || args[0].equalsIgnoreCase("menu")) {
                if(territoryData.getTerritoryTeamOfPlayer(p) == null) {
                    p.openInventory(hasNoTerritory_Menu(p));
                } else {
                    p.openInventory(hasTerritory_Menu(p));
                }
            } else if (args[0].equalsIgnoreCase("territory-menu") || args[0].equalsIgnoreCase("terrMenu")) {
                if(territoryData.getTerritoryTeamOfPlayer(p) == null){
                    p.sendMessage(main.prefix + "§4Vous n'avez pas de territoire !");
                }
                p.openInventory(territoryData.getTerrInv(p,territoryData.getTerritoryTeamOfPlayer(p)));
                return true;
            } else if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("liste")) {
                p.openInventory(territoryData.getTerritoryListInventory(p,1));
                return true;

            } else if(args[0].equalsIgnoreCase("withdrawMoney") || args[0].equalsIgnoreCase("retirerArgent")) {
                String terr = territoryData.getPlayerTerritory(p);
                if(!territoryData.isOfficer(p, terr) && !territoryData.isChief(p, terr)) {
                    p.sendMessage(main.prefix + "§cVous n'avez pas §4la permission §cde §4récupérer de l'argent §cde la banque de votre territoire !");
                    return true;
                }
                if(args[1]==null) {
                    p.sendMessage(main.wrongUsage + "/territoire withdrawMoney <moneyAmount>");
                    return true;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    p.sendMessage(main.wrongUsage + "/territoire withdrawMoney <moneyAmount>");
                    return true;
                }
                if(territoryData.getTerritoryMoney(terr) < amount) {
                    p.sendMessage(main.prefix + "§4Il n'y a pas assez d'argent dans la banque de votre territoire !");
                    p.sendMessage(main.prefix + "§cIl y a §e" + territoryData.getTerritoryMoney(terr) + main.moneySign + "§c dans la banque de votre territoire.");
                    return true;
                }
                try {
                    territoryData.removeTerritoryMoney(terr,amount);
                    playerData.addMoney(amount);
                    p.sendMessage(main.prefix + "§a" + amount + main.moneySign + "§2 ont été transféré de la banque de votre territoire à votre compte !");
                    return true;
                } catch (Exception e) {
                    p.sendMessage(main.prefix + "§4Une erreur s'est produite...");
                    main.logError("Couldn't withdraw money from territory",e);
                    return true;
                }
            } else if(args[0].equalsIgnoreCase("depositMoney") || args[0].equalsIgnoreCase("deposerArgent")) {
                String terr = territoryData.getPlayerTerritory(p);
                if(!territoryData.isOfficer(p, terr) && !territoryData.isChief(p, terr)) {
                    p.sendMessage(main.prefix + "§cVous n'avez pas §4la permission §cde §4déposer de l'argent §cdans la banque de votre territoire !");
                    return true;
                }
                if(args[1]==null) {
                    p.sendMessage(main.wrongUsage + "/territoire depositMoney <moneyAmount>");
                    return true;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    p.sendMessage(main.wrongUsage + "/territoire depositMoney <moneyAmount>");
                    return true;
                }
                try {
                    if(playerData.money() < amount) {
                        p.sendMessage(main.prefix + "§4Il n'y a pas assez d'argent dans votre compte !");
                        p.sendMessage(main.prefix + "§cIl y a §e" + playerData.money() + main.moneySign + "§c dans dans votre compte.");
                        return true;
                    }

                    territoryData.addTerritoryMoney(terr,amount);
                    playerData.removeMoney(amount);
                    p.sendMessage(main.prefix + "§a" + amount + main.moneySign + "§2 ont été transféré de votre compte à la banque de votre territoire !");
                    return true;
                } catch (Exception e) {
                    p.sendMessage(main.prefix + "§4Une erreur s'est produite...");
                    main.logError("Couldn't deposit money to territory",e);
                    return true;
                }
            } else if(args[0].equalsIgnoreCase("admin") && playerData.getRank().equals(Grades.ADMIN)) {
                if(args.length == 1) {
                    p.sendMessage(main.wrongUsage + "/territoires admin <runWorkerCheckup/bypassClaims>");
                    return true;
                }
                if(args[1].equalsIgnoreCase("runWorkerCheckup")){
                    if(!playerData.getRank().equals(Grades.ADMIN)) { return true;}
                    if(args.length==2) {
                        try {
                            territoryData.runWorkerCheckup();
                            p.sendMessage(main.prefix + "§2Success!");
                            return true;

                        } catch (Exception e) {
                            p.sendMessage(main.prefix + "§4Une erreur s'est produite");
                            return true;
                        }
                    } else {
                        int nOfRun;
                        try {
                            nOfRun = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            p.sendMessage(main.prefix + "Veuillez entrer un nombre !");
                            return true;
                        }
                        try {
                            for (int i = 0 ; i < nOfRun; i++) {
                                territoryData.runWorkerCheckup();
                            }
                            p.sendMessage(main.prefix + "§2Success §6- §aran WorkerCheckup §6"  + nOfRun + "§a times ");
                        } catch (Exception e) {
                            p.sendMessage(main.prefix + "§4Une erreur s'est produite");
                            return true;
                        }
                    }
                }
                if (args[1].equalsIgnoreCase("bypassClaims")){
                    if(!Objects.equals(playerData.getRank(), Grades.ADMIN)) { return true;}
                    if(main.bypassClaims.contains(p)){
                        main.bypassClaims.remove(p);
                        p.sendTitle("§eBypass Claims : §c§lOFF","",20,60,20);
                        p.sendMessage(main.prefix + "§cVous n'ignorez plus protections des claims !");
                    } else {
                        main.bypassClaims.add(p);
                        p.sendTitle("§eBypass Claims : §a§lON","",20,60,20);
                        p.sendMessage(main.prefix + "§aVous ignorez désormais les protections des claims !");
                    }
                    return true;

                }
                if (args[1].equalsIgnoreCase("setTerritory")) {
                    if(!Objects.equals(playerData.getRank(), Grades.ADMIN)) { return true;}
                    if(args.length!=4) {
                        p.sendMessage(main.wrongUsage + "/territoire admin setTerritory <target> <territory>");
                        return true;
                    }

                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[3]);

                    String territoryName = "";
                    for (String terr : territoryData.getTerritoriesList()) {
                        if(terr.equalsIgnoreCase(args[3])) {
                            territoryName=terr;
                        }
                    }
                    if(territoryName.isEmpty()) {
                        p.sendMessage(main.prefix + "§4Territoire non trouvé !");
                        return true;
                    }

                    territoryData.joinTerritory(target,territoryName);
                    if(target.getPlayer() != null && target.getPlayer().equals(p)) {
                        p.sendMessage(main.prefix + "§2Votre territoire a été défini à " + territoryName + "§2 !");
                    } else {
                        p.sendMessage(main.prefix + "§2Le territoire de §6" + target.getName() + "§2 a été défini à §6" + territoryName + "§2 !");
                    }
                    return true;
                }
                if (args[1].equalsIgnoreCase("makeOfficer")) {
                    if(!Objects.equals(playerData.getRank(), Grades.ADMIN)) { return true;}
                    if(args.length!=4) {
                        p.sendMessage(main.wrongUsage + "/territoire admin makeOfficer <target>");
                        return true;
                    }

                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[3]);
                    territoryData.ADMIN_makeOfficer(target,p,args[2]);
                    return true;
                }
            }
        }

        //END OF ARGUMENTS!!!

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
            p.openInventory(hasNoTerritory_Menu(p));
        } else {
            if(args.length==1){
                if(args[0].equalsIgnoreCase("claim") || args[0].equalsIgnoreCase("claimChunk")) {
                    String territoryName = territoryData.getPlayerTerritory(p);
                    if(!territoryData.isChief(p,territoryName) && !territoryData.isOfficer(p,territoryName)) {
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
                    String territoryName = territoryData.getPlayerTerritory(p);
                    if(!territoryData.isChief(p,territoryName) && !territoryData.isOfficer(p,territoryName)) {
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

            p.openInventory(hasTerritory_Menu(p));
        }

        return true;
    }

    public Inventory hasNoTerritory_Menu(Player p) {
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
        return hasNoTerrMenu;
    }

    public Inventory hasTerritory_Menu(Player p) {
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
        return hasTerrMenu;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String l, String[] args) {
        List<String> tabComplete = Lists.newArrayList();
        if(args.length == 1) {
            tabComplete.add("gui");
            tabComplete.add("showClaimsMap");
            tabComplete.add("showClaims");
            tabComplete.add("list");
            if(s instanceof Player && territoryData.getPlayerTerritory((Player)s) !=null){
                tabComplete.add("territory-menu");
            }
            if(s instanceof Player && (territoryData.isChief((Player) s, territoryData.getPlayerTerritory((Player) s)) || territoryData.isOfficer((Player) s, territoryData.getPlayerTerritory((Player) s)))) {
                tabComplete.add("claim");
                tabComplete.add("unclaim");
                tabComplete.add("depositMoney");
                tabComplete.add("withdrawMoney");
                tabComplete.add("buyWorker");
            }
            if(s instanceof Player p) {
                PlayerData playerData = new PlayerData(p);
                if(playerData.getRank() != null && playerData.getRank().equals(Grades.ADMIN)){
                    tabComplete.add("admin");
                }
            }
        }
        if(args.length == 2) {
            if(s instanceof Player p && (territoryData.isChief(p, territoryData.getPlayerTerritory(p)) || territoryData.isOfficer(p, territoryData.getPlayerTerritory(p)))) {
                if (args[0].equalsIgnoreCase("depositMoney") || args[0].equalsIgnoreCase("withdrawMoney")) {
                    tabComplete.add("10");
                    tabComplete.add("100");
                    tabComplete.add("1000");
                    tabComplete.add("10000");
                    tabComplete.add("100000");
                }
                if (args[0].equalsIgnoreCase("buyWorker")) {
                    for (WorkerType type : WorkerType.values()) {
                        tabComplete.add(type.name().toLowerCase());
                    }
                }

            }
            if(s instanceof Player p) {
                PlayerData playerData = new PlayerData(p);
                if(playerData.getRank() != null && playerData.getRank().equals(Grades.ADMIN)){
                    tabComplete.add("bypassClaims");
                    tabComplete.add("runWorkerCheckup");
                }
            }
        }

        return tabComplete;
    }
}