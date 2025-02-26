package fr.mattmunich.civilisation_creatif.helpers;
import fr.mattmunich.civilisation_creatif.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.*;

public class SidebarManager {
    private static Plugin plugin;
    private static Main main;
    private static TerritoryData territoryData;

    private static Scoreboard scoreboard;
    private static Objective objective;

    public SidebarManager(Plugin plugin,Main main, TerritoryData territoryData) {
        SidebarManager.plugin = plugin;
        SidebarManager.main = main;
        SidebarManager.territoryData = territoryData;
    }

    public void setupScoreboard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager != null) {
            scoreboard = manager.getNewScoreboard();
        }

        assert scoreboard != null;
        objective = scoreboard.registerNewObjective("sidebar", Criteria.DUMMY, main.hex("§6- " + main.fullName + " §6-"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void showScoreboard(Player player) {
        player.setScoreboard(scoreboard);
        updateScoreboard(player);
    }

    public static void updateScoreboard(Player player) {
        if (scoreboard == null) { return;}
        scoreboard.getEntries().forEach(scoreboard::resetScores);
        setValues(player);
        player.setScoreboard(scoreboard);
    }


    private static void setValues(Player player) {
        PlayerData playerData = new PlayerData(player);
        String territoryName = playerData.getTerritory();
        Team terr = playerData.getTerritory()!=null ? territoryData.getTerritoryTeam(territoryName) : null;
        setScore("",7);
        setScore("§aArgent : §6" + playerData.getMoneyScore() + main.moneySign, 6);
        setScore("§bXP : §6" + playerData.getXPScore(), 5);
        setScore(" ",4);
        setScore("§6- §2§lTerritoire §6-", 3);
        setScore("§a-> §2Nom : " + (terr!=null ? terr.getColor() + terr.getName() :"§8§oPas de territoire"), 2);
        if(terr!=null) {
            setScore("§a-> §2Grade : " + (territoryData.isChief(player,territoryName) ? "§6§lChef" : (territoryData.isOfficer(player,territoryName) ? "§aOfficier" : "§7Membre")), 1);
        }
    }

    private static void setScore(String title, int value) {
        Score score = objective.getScore(title);
        score.setScore(value);
    }
}

