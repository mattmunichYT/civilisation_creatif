package fr.mattmunich.civilisation_creatif;

import fr.mattmunich.civilisation_creatif.commands.*;
import fr.mattmunich.civilisation_creatif.helpers.*;
import fr.mattmunich.civilisation_creatif.listeners.AntiSpeed;
import fr.mattmunich.civilisation_creatif.listeners.EventListener;
import fr.mattmunich.civilisation_creatif.listeners.JoinListener;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Main extends JavaPlugin {

    public String version = "0.5";

    public String hex(String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    //GRADES
    public ArrayList<Player> admin = new ArrayList<>();
    public ArrayList<Player> modo = new ArrayList<>();
    public ArrayList<Player> chef = new ArrayList<>();
    public ArrayList<Player> membre = new ArrayList<>();
    public ArrayList<Player> vagabond = new ArrayList<>();
    public ArrayList<Player> juge_builds = new ArrayList<>();
    //END OF GRADES
    //PUBLIC UTILITIES
    public ArrayList<Player> speeding = new ArrayList<>();
    public String prefix = "§e[§2Civilisation§e] §2";
    public String makeItSafePrefix = "§1[§b§lMake It Safe§1] §a";
    public String playerToExc = prefix + "§4Vous devez être un joueur pour éxecuter cette commande !";
    public String noPermToExc = prefix + "§4Vous n'avez pas la permission d'éxecuter cette commande !";
    public String wrongUsage = prefix + "§4Utilisation : §c";
    public String moneySign = "¢";
    public String playerNotFound(String name) {return prefix + "§4Impossible de trouver le joueur §c" + name;}
    public void logError(String message,Exception error) {
        Bukkit.getConsoleSender().sendMessage(prefix + "§4" + hex(message) + " because of §eerror: \n§r" + error + Arrays.toString(error.getStackTrace()).replace(",", ",\n"));
    }
    //END OF PUBLIC UTILITIES
    //HELPERS GET
    PlayerData pdata;
    Backup backup;
    TerritoryData territoryData;
    Warp warp;
    //END OF HELPERS GET
    //OTHER ARRAY LISTS
    public ArrayList<Player> seeTerritoryBorders = new ArrayList<>();
    //END OF OTHER ARRAY LISTS
    //SCOREBOARDS


    @SuppressWarnings("DataFlowIssue")
    @Override
    public void onEnable() {

        //HELPERS INIT

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard;
        if (manager != null) {
            scoreboard = manager.getMainScoreboard();
            if(scoreboard.getObjective("money") == null) {
                Objective money = scoreboard.registerNewObjective("money", Criteria.DUMMY, "Argent");
            } else {
                Objective money = scoreboard.getObjective("money");
            }

            if(scoreboard.getObjective("xp") == null) {
                Objective xp = scoreboard.registerNewObjective("xp", Criteria.DUMMY, "XP");
            } else {
                Objective xp = scoreboard.getObjective("xp");
            }
        } else {
            Bukkit.getConsoleSender().sendMessage(prefix + "Scoreboard manager is null !");
        }


        pdata = new PlayerData(this, this);
        backup = new Backup(this,this);
        territoryData = new TerritoryData(this, this);
        territoryData.initConfig();
        warp = new Warp(this);
        warp.initConfig();

        //END OF HELPERS INIT

        getCommand("minijeuxentrepotes").setExecutor(new MJEPCommand(this));
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("xp").setExecutor(new XPCommand(this));
        getCommand("money").setExecutor(new MoneyCommand(this));
        getCommand("rank").setExecutor(new RankCommand(this));
        getCommand("civilisation").setExecutor(new CivlisationCommand(this));
        getCommand("backup").setExecutor(new BackupCommand(this, backup));
        getCommand("territoire").setExecutor(new TerritoireCommand(this,territoryData));
        getCommand("home").setExecutor(new HomeCommand(this));
        getCommand("sethome").setExecutor(new HomeCommand(this));
        getCommand("delhome").setExecutor(new HomeCommand(this));
        getCommand("warp").setExecutor(new WarpCommand(this,warp));
        getCommand("setwarp").setExecutor(new WarpCommand(this,warp));
        getCommand("delwarp").setExecutor(new WarpCommand(this,warp));

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new JoinListener(this,territoryData), this);
        pm.registerEvents(new AntiSpeed(this), this);
        pm.registerEvents(new EventListener(this,territoryData), this);
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for(Player all : Bukkit.getOnlinePlayers()) {

                all.setPlayerListHeader("§2§lBienvenue " + all.getDisplayName() +  "§2§l\n sur §6le serveur §2§lCivilisation §6Créatif !\n");
                int ndj = Bukkit.getOnlinePlayers().size();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                int h = calendar.get(Calendar.HOUR_OF_DAY);
                int mInt = calendar.get(Calendar.MINUTE);
                String m = String.valueOf(mInt);
                if(mInt < 10) {
                    m = "0" + m;
                }

                String worldname = getWorldname(all);

                all.setPlayerListFooter("\n§2Nombre de joueurs en ligne : §6" + ndj + "§r\n§2IP : §6§lminijeux.mine.fun" + "§r\n§2Heure : §6" + h + "§e:§6" + m + "§r\n§2Monde : §6" + worldname + "§r§2,\n Position : §eX: §6" + all.getLocation().getBlockX() + "§r§2, §eY: §6" + all.getLocation().getBlockY() + "§r§2, §eZ: §6" + all.getLocation().getBlockZ());
            }
        }, 1, 1);

        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        if(cal.get(Calendar.HOUR_OF_DAY) >= 22)
            cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 22);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long offset = cal.getTimeInMillis() - now;
        long ticks = offset / 50L;
        try {
            Bukkit.getScheduler().runTaskTimer(this, () -> backup.run(), ticks,1728000);
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(prefix + "§4Coulnd't schedule backup : §r" + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()).replace(",",",\n"));
        }

        territoryData.programNextWorkerCheckup();
        try {
            Bukkit.getWorld("spawn").loadChunk(0,0);
        } catch (NullPointerException e) {
            try {
                WorldCreator wc = new WorldCreator("spawn");

                wc.type(WorldType.FLAT);
                wc.generator(new EmptyChunkGenerator());
                wc.keepSpawnInMemory(true);

                wc.createWorld();
                Bukkit.createWorld(wc);
            } catch (Exception ex) {
                logError("Couldn't create or load spawn world",e);
            }
        }
    }

    private static String getWorldname(Player all) {
        String worldname = Objects.requireNonNull(all.getLocation().getWorld()).getName();

        if(worldname.equalsIgnoreCase("world")) {
            worldname = "§2Civilisaton §e- §6Overworld";

        } else if(worldname.equalsIgnoreCase("world_nether")) {
            worldname = "§2pq Civilisation §e- §cNether";

        } else if(worldname.equalsIgnoreCase("world_the_end")) {
            worldname = "§2Civilisation §e- §5End";

        } else if(worldname.equalsIgnoreCase("spawn")) {
            worldname = "§2Civilisation §e- §6Spawn";

        } else {
            worldname = all.getLocation().getWorld().getName();
        }
        return worldname;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        //HELPERS INIT
        pdata = new PlayerData(this, this);
        territoryData = new TerritoryData(this, this);
        territoryData.initConfig();
        backup = new Backup(this,this);
        warp = new Warp(this);
        warp.initConfig();
        //END OF HELPERS INIT
    }

    @Override
    public void onDisable() {
        for(Player all : Bukkit.getOnlinePlayers()) {
            all.transfer("91.197.6.60", 25599);
            all.sendMessage("§e(§6!§e)§4 Le serveur Civilisation Créatif §credémarre§4 !");
        }
        super.onDisable();
    }

}
