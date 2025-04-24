package fr.mattmunich.civilisation_creatif.helpers;
import fr.mattmunich.civilisation_creatif.Main;
import fr.mattmunich.civilisation_creatif.territories.TerritoryData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;

public class SidebarManager {
    private static Plugin plugin;
    private static Main main;
    private static TerritoryData territoryData;

    public SidebarManager(Plugin plugin,Main main, TerritoryData territoryData) {
        SidebarManager.plugin = plugin;
        SidebarManager.main = main;
        SidebarManager.territoryData = territoryData;
    }

    private static final Map<Player,Scoreboard> playerScoreboard = new HashMap<Player,Scoreboard>();


    public void setupPlayerObjective(Player p){
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) { return; }
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("sidebar", Criteria.DUMMY, main.hex("§6- " + main.fullName + " §6-"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        playerScoreboard.put(p,scoreboard);
        showScoreboard(p);
    }

    public static Scoreboard getPlayerScoreboard(Player p) {
        return playerScoreboard.get(p);
    }

    public void showScoreboard(Player player) {
        player.setScoreboard(getPlayerScoreboard(player));
        updateScoreboard(player);
    }

    public static void updateScoreboard(Player player) {
        Scoreboard playerScoreboard = getPlayerScoreboard(player);
        playerScoreboard.getEntries().forEach(playerScoreboard::resetScores);
        setValues(player);
        player.setScoreboard(playerScoreboard);
    }


    private static void setValues(Player p) {
        PlayerData playerData = new PlayerData(p);
        String territoryName = playerData.getTerritory();
        Team terr = playerData.getTerritory()!=null ? territoryData.getTerritoryTeam(territoryName) : null;
        setScore(p, "",8);
        setScore(p, "§aNom : " + p.getDisplayName(),7);
        setScore(p,"§aArgent : §6" + playerData.getMoneyScore() + main.moneySign, 6);
        setScore(p,"§bXP : §6" + playerData.getXPScore(), 5);
        setScore(p," ",4);
        setScore(p,"§6- §2§lTerritoire §6-", 3);
        setScore(p,"§a-> §2Nom : " + (terr!=null ? terr.getColor() + terr.getName() :"§8§oPas de territoire"), 2);
        if(terr!=null) {
            setScore(p,"§a-> §2Grade : " + (territoryData.isChief(p,territoryName) ? "§6§lChef" : (territoryData.isOfficer(p,territoryName) ? "§aOfficier" : "§7Membre")), 1);
        }
    }

    private static void setScore(Player p, String title, int value) {
        Objective sidebar = getPlayerScoreboard(p).getObjective("sidebar");
        if (sidebar == null) { p.sendMessage(main.prefix + "§4Erreur! §cImpossible de mettre à jour votre sidebar."); return; }
        Score score = sidebar.getScore(title);
        score.setScore(value);
    }
}

