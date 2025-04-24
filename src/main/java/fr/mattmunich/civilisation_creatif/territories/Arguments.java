package fr.mattmunich.civilisation_creatif.territories;

import fr.mattmunich.civilisation_creatif.helpers.Grades;
import fr.mattmunich.civilisation_creatif.helpers.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class Arguments {
    private final Command territoireCommand;

    public Arguments(Command territoireCommand) {
        this.territoireCommand = territoireCommand;
    }

    boolean arguments(String[] args, Player p, int mapRenderRange, PlayerData playerData) {
        if (args[0].equalsIgnoreCase("map") || args[0].equalsIgnoreCase("showClaimsMap")) {
            return claimsMapArgument(p, mapRenderRange);
        } else if (args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("showClaims") || args[0].equalsIgnoreCase("showNearbyClaims")) {
            if (territoireCommand.getMain().seeTerritoryBorders.contains(p)) {
                territoireCommand.getMain().seeTerritoryBorders.remove(p);
                p.sendMessage(territoireCommand.getMain().prefix + "§cVous ne pouvez désormais plus voir la délimitation des territoires à proximité de vous !");
            } else {
                territoireCommand.getMain().seeTerritoryBorders.add(p);
                p.sendMessage(territoireCommand.getMain().prefix + "§2Vous pouvez désormais voir la délimitation des territoires à proximité de vous !");
            }
            return true;
        } else if (args[0].equalsIgnoreCase("buyWorker")) {
            if (!territoireCommand.getTerritoryData().isChief(p, territoireCommand.getTerritoryData().getPlayerTerritory(p)) && !territoireCommand.getTerritoryData().isOfficer(p, territoireCommand.getTerritoryData().getPlayerTerritory(p))) {
                p.sendMessage(territoireCommand.getMain().prefix + "§4Vous devez être le chef/un officier de votre territoire pour faire cela!");
                return true;
            }
            territoireCommand.getTerritoryData().showBuyWorkerInv(p);
            return true;
        } else if (args[0].equalsIgnoreCase("gui") || args[0].equalsIgnoreCase("menu")) {
            if (territoireCommand.getTerritoryData().getTerritoryTeamOfPlayer(p) == null) {
                p.openInventory(territoireCommand.getTerritoryData().getTerritoryInventories().hasNoTerritory_Menu(p));
            } else {
                p.openInventory(territoireCommand.getTerritoryData().getTerritoryInventories().hasTerritory_Menu(p));
            }
        } else if (args[0].equalsIgnoreCase("territory-menu") || args[0].equalsIgnoreCase("terrMenu")) {
            if (territoireCommand.getTerritoryData().getTerritoryTeamOfPlayer(p) == null) {
                p.sendMessage(territoireCommand.getMain().prefix + "§4Vous n'avez pas de territoire !");
            }
            p.openInventory(territoireCommand.getTerritoryData().getTerrInv(p, territoireCommand.getTerritoryData().getTerritoryTeamOfPlayer(p)));
            return true;
        } else if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("liste")) {
            p.openInventory(territoireCommand.getTerritoryData().getTerritoryListInventory(p, 1));
            return true;

        } else if (args[0].equalsIgnoreCase("withdrawMoney") || args[0].equalsIgnoreCase("retirerArgent")) {
            String terr = territoireCommand.getTerritoryData().getPlayerTerritory(p);
            if (!territoireCommand.getTerritoryData().isOfficer(p, terr) && !territoireCommand.getTerritoryData().isChief(p, terr)) {
                p.sendMessage(territoireCommand.getMain().prefix + "§cVous n'avez pas §4la permission §cde §4récupérer de l'argent §cde la banque de votre territoire !");
                return true;
            }
            if (args[1] == null) {
                p.sendMessage(territoireCommand.getMain().wrongUsage + "/territoire withdrawMoney <moneyAmount>");
                return true;
            }
            int amount;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                p.sendMessage(territoireCommand.getMain().wrongUsage + "/territoire withdrawMoney <moneyAmount>");
                return true;
            }
            if (territoireCommand.getTerritoryData().getTerritoryMoney(terr) < amount) {
                p.sendMessage(territoireCommand.getMain().prefix + "§4Il n'y a pas assez d'argent dans la banque de votre territoire !");
                p.sendMessage(territoireCommand.getMain().prefix + "§cIl y a §e" + territoireCommand.getTerritoryData().getTerritoryMoney(terr) + territoireCommand.getMain().moneySign + "§c dans la banque de votre territoire.");
                return true;
            }
            try {
                territoireCommand.getTerritoryData().removeTerritoryMoney(terr, amount);
                playerData.addMoney(amount);
                p.sendMessage(territoireCommand.getMain().prefix + "§a" + amount + territoireCommand.getMain().moneySign + "§2 ont été transféré de la banque de votre territoire à votre compte !");
                return true;
            } catch (Exception e) {
                p.sendMessage(territoireCommand.getMain().prefix + "§4Une erreur s'est produite...");
                territoireCommand.getMain().logError("Couldn't withdraw money from territory", e);
                return true;
            }
        } else if (args[0].equalsIgnoreCase("depositMoney") || args[0].equalsIgnoreCase("deposerArgent")) {
            String terr = territoireCommand.getTerritoryData().getPlayerTerritory(p);
            if (!territoireCommand.getTerritoryData().isOfficer(p, terr) && !territoireCommand.getTerritoryData().isChief(p, terr)) {
                p.sendMessage(territoireCommand.getMain().prefix + "§cVous n'avez pas §4la permission §cde §4déposer de l'argent §cdans la banque de votre territoire !");
                return true;
            }
            if (args[1] == null) {
                p.sendMessage(territoireCommand.getMain().wrongUsage + "/territoire depositMoney <moneyAmount>");
                return true;
            }
            int amount;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                p.sendMessage(territoireCommand.getMain().wrongUsage + "/territoire depositMoney <moneyAmount>");
                return true;
            }
            try {
                if (playerData.money() < amount) {
                    p.sendMessage(territoireCommand.getMain().prefix + "§4Il n'y a pas assez d'argent dans votre compte !");
                    p.sendMessage(territoireCommand.getMain().prefix + "§cIl y a §e" + playerData.money() + territoireCommand.getMain().moneySign + "§c dans dans votre compte.");
                    return true;
                }

                territoireCommand.getTerritoryData().addTerritoryMoney(terr, amount);
                playerData.removeMoney(amount);
                p.sendMessage(territoireCommand.getMain().prefix + "§a" + amount + territoireCommand.getMain().moneySign + "§2 ont été transféré de votre compte à la banque de votre territoire !");
                return true;
            } catch (Exception e) {
                p.sendMessage(territoireCommand.getMain().prefix + "§4Une erreur s'est produite...");
                territoireCommand.getMain().logError("Couldn't deposit money to territory", e);
                return true;
            }
        } else if (args[0].equalsIgnoreCase("admin") && playerData.getRank().equals(Grades.ADMIN)) {
            if (adminArguments(args, p, playerData)) return true;
        }
        return false;
    }

    private boolean claimsMapArgument(Player p, int mapRenderRange) {
        int x = p.getLocation().getChunk().getX();
        int z = p.getLocation().getChunk().getZ();
        int ownerCount = 0;
        List<String> owners = new ArrayList<String>();
        List<String> chunksOwners = new ArrayList<String>();
        Map<String, Integer> ownerID = new HashMap<String, Integer>();
        for (int chunkX = x - mapRenderRange; chunkX < x + mapRenderRange; chunkX++) {
            for (int chunkZ = z - mapRenderRange; chunkZ < z + mapRenderRange; chunkZ++) {
//                        p.sendMessage("Checking for chunk " + chunkX + "," + chunkZ);
                Chunk chunk = p.getWorld().getChunkAt(chunkX, chunkZ);
                String chunkOwner = territoireCommand.getTerritoryData().getChunkOwner(chunk);
//                        p.sendMessage("chunk owner:" + chunkOwner);
                if (chunkOwner == null) {
                    chunksOwners.add("§r- ");
//                            p.sendMessage("chunk not owned");
                    continue;
                }
                if (!owners.contains(chunkOwner)) {
                    owners.add(chunkOwner);
                    ownerCount += 1;
                    ownerID.put(chunkOwner, ownerCount);
                    chunksOwners.add("§" + territoireCommand.getTerritoryData().getTerritoryTeam(chunkOwner).getColor() + ownerID.get(chunkOwner) + " ");
//                            p.sendMessage("chunk owned by created profile " + chunkOwner + " with id " + ownerCount);
                } else {
                    chunksOwners.add("§" + territoireCommand.getTerritoryData().getTerritoryTeam(chunkOwner).getColor() + ownerID.get(chunkOwner) + " ");
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
        chunksOwners.add(columsNum + 1, "\n§eb | ");  // V FOR columsNum 6 V
        chunksOwners.add(columsNum * 2 + 2, "\n§ec | ");// == 12+2 : not 12 bc we added an elements in previous lines
        chunksOwners.add(columsNum * 3 + 3, "\n§ed | ");// == 18+3
        chunksOwners.add(columsNum * 4 + 4, "\n§ee | ");// == 24+4
        chunksOwners.add(columsNum * 5 + 5, "\n§ef | ");// == 30+5
        chunksOwners.add(columsNum * 6 + 6, "\n§eg | ");// == 36+6
        chunksOwners.add(columsNum * 7 + 7, "\n§eh | ");// == 42+7
        chunksOwners.add(columsNum * 8 + 8, "\n\n\n"); // == 48+8
        StringBuilder formattedMap = new StringBuilder(chunksOwners.toString().replace("[", "").replace("\"", "").replace(", ", "").replace("]", "") + "\n\n§aLégende:\n");
        for (String owner : owners) {
            int id = ownerID.get(owner);
            formattedMap.append("§a- §").append(territoireCommand.getTerritoryData().getTerritoryTeam(owner).getColor()).append(id).append("§2 : ").append(owner).append("\n");
        }
        p.sendMessage(territoireCommand.getMain().prefix + "§6Voici la carte des claims à proximité de vous :\n" + formattedMap);
        return true;
    }

    private boolean adminArguments(String[] args, Player p, PlayerData playerData) {
        if (args.length == 1) {
            p.sendMessage(territoireCommand.getMain().wrongUsage + "/territoires admin <runWorkerCheckup/bypassClaims>");
            return true;
        }
        if (args[1].equalsIgnoreCase("runWorkerCheckup")) {
            if (!playerData.getRank().equals(Grades.ADMIN)) {
                return true;
            }
            if (args.length == 2) {
                try {
                    territoireCommand.getTerritoryData().runWorkerCheckup();
                    p.sendMessage(territoireCommand.getMain().prefix + "§2Success!");
                    return true;

                } catch (Exception e) {
                    p.sendMessage(territoireCommand.getMain().prefix + "§4Une erreur s'est produite");
                    return true;
                }
            } else {
                int nOfRun;
                try {
                    nOfRun = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    p.sendMessage(territoireCommand.getMain().prefix + "Veuillez entrer un nombre !");
                    return true;
                }
                try {
                    for (int i = 0; i < nOfRun; i++) {
                        territoireCommand.getTerritoryData().runWorkerCheckup();
                    }
                    p.sendMessage(territoireCommand.getMain().prefix + "§2Success §6- §aran WorkerCheckup §6" + nOfRun + "§a times ");
                } catch (Exception e) {
                    p.sendMessage(territoireCommand.getMain().prefix + "§4Une erreur s'est produite");
                    return true;
                }
            }
        } else if (args[1].equalsIgnoreCase("bypassClaims")) {
            if (!Objects.equals(playerData.getRank(), Grades.ADMIN)) {
                return true;
            }
            if (territoireCommand.getMain().bypassClaims.contains(p)) {
                territoireCommand.getMain().bypassClaims.remove(p);
                p.sendTitle("§eBypass Claims : §c§lOFF", "", 20, 60, 20);
                p.sendMessage(territoireCommand.getMain().prefix + "§cVous n'ignorez plus protections des claims !");
            } else {
                territoireCommand.getMain().bypassClaims.add(p);
                p.sendTitle("§eBypass Claims : §a§lON", "", 20, 60, 20);
                p.sendMessage(territoireCommand.getMain().prefix + "§aVous ignorez désormais les protections des claims !");
            }
            return true;

        } else if (args[1].equalsIgnoreCase("setTerritory")) {
            if (!Objects.equals(playerData.getRank(), Grades.ADMIN)) {
                return true;
            }
            if (args.length != 4) {
                p.sendMessage(territoireCommand.getMain().wrongUsage + "/territoire admin setTerritory <target> <territory>");
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);

            String territoryName = "";
            for (String terr : territoireCommand.getTerritoryData().getTerritoriesList()) {
                if (terr.equalsIgnoreCase(args[3])) {
                    territoryName = terr;
                }
            }
            if (territoryName.isEmpty()) {
                p.sendMessage(territoireCommand.getMain().prefix + "§4Territoire non trouvé !");
                return true;
            }
            if (territoireCommand.getTerritoryData().getPlayerTerritory(target) != null) {
                territoireCommand.getTerritoryData().ADMIN_leaveTerritory(target);
            }
            territoireCommand.getTerritoryData().joinTerritory(target, territoryName);
            if (target.getPlayer() != null && target.getPlayer().equals(p)) {
                p.sendMessage(territoireCommand.getMain().prefix + "§2Votre territoire a été défini à " + territoryName + "§2 !");
            } else {
                p.sendMessage(territoireCommand.getMain().prefix + "§2Le territoire de §6" + target.getName() + "§2 a été défini à §6" + territoryName + "§2 !");
            }
            return true;
        } else if (args[1].equalsIgnoreCase("makeOfficer")) {
            if (!Objects.equals(playerData.getRank(), Grades.ADMIN)) {
                return true;
            }
            if (args.length != 3) {
                p.sendMessage(territoireCommand.getMain().wrongUsage + "/territoire admin makeOfficer <target>");
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
            territoireCommand.getTerritoryData().ADMIN_makeOfficer(target, p);
            return true;
        } else if (args[1].equalsIgnoreCase("removeOfficer")) {
            if (!Objects.equals(playerData.getRank(), Grades.ADMIN)) {
                return true;
            }
            if (args.length != 3) {
                p.sendMessage(territoireCommand.getMain().wrongUsage + "/territoire admin removeOfficer <target>");
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
            territoireCommand.getTerritoryData().ADMIN_removeOfficer(target, p);
            return true;
        }
        return false;
    }
}