package fr.mattmunich.civilisation_creatif.territories;

import com.google.common.collect.Lists;
import fr.mattmunich.civilisation_creatif.Main;
import fr.mattmunich.civilisation_creatif.helpers.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class Command implements CommandExecutor, TabCompleter {

    private final Main main;

    private final TerritoryData territoryData;
    private final Arguments arguments = new Arguments(this);

    public Command(Main main, TerritoryData territoryData) {
        this.main = main;
        this.territoryData = territoryData;
    }
    @Override
    public boolean onCommand(CommandSender s, org.bukkit.command.Command cmd, String l, String[] args) {
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
            if (arguments.arguments(args, p, mapRenderRange, playerData)) return true;
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
            p.openInventory(territoryData.getTerritoryInventories().hasNoTerritory_Menu(p));
        } else {
            if(args.length==1){
                if(args[0].equalsIgnoreCase("claim") || args[0].equalsIgnoreCase("claimChunk")) {
                    String territoryName = territoryData.getPlayerTerritory(p);
                    if(!territoryData.isChief(p,territoryName) && !territoryData.isOfficer(p,territoryName)) {
                        p.sendMessage(main.prefix + "§cVous n'avez pas §4la permission §cde §4claim des chunk §cpour votre territoire !");
                        return true;
                    }
                    territoryData.claimChunk(p, territoryData.getPlayerTerritory(p), p.getLocation().getChunk());

                    return true;
                }
                if(args[0].equalsIgnoreCase("unclaim") || args[0].equalsIgnoreCase("unclaimChunk")) {
                    String territoryName = territoryData.getPlayerTerritory(p);
                    if(!territoryData.isChief(p,territoryName) && !territoryData.isOfficer(p,territoryName)) {
                        p.sendMessage(main.prefix + "§cVous n'avez pas §4la permission §cde §4unclaim des chunk §cpour votre territoire !");
                        return true;
                    }
                    territoryData.unclaimChunk(p, territoryData.getPlayerTerritory(p), p.getLocation().getChunk());
                    return true;
                }
            }

            p.openInventory(territoryData.getTerritoryInventories().hasTerritory_Menu(p));
        }

        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender s, org.bukkit.command.Command cmd, String l, String[] args) {
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
                    if(args[0].equalsIgnoreCase("admin")) {
                        tabComplete.add("bypassClaims");
                        tabComplete.add("runWorkerCheckup");
                        tabComplete.add("setTerritory");
                        tabComplete.add("makeOfficer");
                        tabComplete.add("removeOfficer");
                        tabComplete.add("setMoney");
                        tabComplete.add("addMoney");
                        tabComplete.add("removeMoney");
                    }
                }
            }
        }
        if (args.length == 3) {
            if(s instanceof Player p) {
                PlayerData playerData = new PlayerData(p);
                if(playerData.getRank() != null && playerData.getRank().equals(Grades.ADMIN)){
                    if(args[0].equalsIgnoreCase("admin") && (
                            args[1].equalsIgnoreCase("setTerritory")
                            || args[1].equalsIgnoreCase("makeOfficer")
                            || args[1].equalsIgnoreCase("removeOfficer")
                    )) {
                        for (Player online : Bukkit.getOnlinePlayers()) {
                            tabComplete.add(online.getName());
                        }
                        for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
                            if(offline.getName()!=null && offline.hasPlayedBefore()) {
                                tabComplete.add(offline.getName());
                            }
                        }
                    }
                    if(args[0].equalsIgnoreCase("admin") && (
                            args[1].equalsIgnoreCase("setMoney")
                            || args[1].equalsIgnoreCase("addMoney")
                            || args[1].equalsIgnoreCase("removeMoney")
                    )) {
                        tabComplete.add("1");
                        tabComplete.add("10");
                        tabComplete.add("100");
                        tabComplete.add("1000");
                        tabComplete.add("10000");
                        tabComplete.add("100000");
                        tabComplete.add("1000000");
                    }
                }
            }
        }
        if (args.length == 4) {
            if(s instanceof Player p) {
                PlayerData playerData = new PlayerData(p);
                if(playerData.getRank() != null && playerData.getRank().equals(Grades.ADMIN)){
                    if(args[0].equalsIgnoreCase("admin") && (
                            args[1].equalsIgnoreCase("setTerritory")
                            || args[1].equalsIgnoreCase("setMoney")
                            || args[1].equalsIgnoreCase("addMoney")
                            || args[1].equalsIgnoreCase("removeMoney")
                    )) {
                        tabComplete.addAll(territoryData.getTerritoriesList());
                    }
                }
            }
        }

        return tabComplete;
    }

    public TerritoryData getTerritoryData() {
        return territoryData;
    }

    public Main getMain() {
        return main;
    }
}