package fr.mattmunich.civilisation_creatif;

import com.sk89q.worldedit.WorldEdit;
import fr.mattmunich.civilisation_creatif.commands.*;
import fr.mattmunich.civilisation_creatif.helpers.*;
import fr.mattmunich.civilisation_creatif.listeners.AntiSpeed;
import fr.mattmunich.civilisation_creatif.listeners.EventListener;
import fr.mattmunich.civilisation_creatif.listeners.JoinListener;
import fr.mattmunich.civilisation_creatif.listeners.WorldEditListener;
import fr.mattmunich.civilisation_creatif.territories.Command;
import fr.mattmunich.civilisation_creatif.territories.TerritoryData;
import fr.mattmunich.civilisation_creatif.territories.Listener;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Main extends JavaPlugin {

    public String version = "0.5";

    public String hex(String message) {
        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace("&#","x");

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        Pattern pattern1 = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher1 = pattern1.matcher(message);
        while (matcher1.find()) {
            String hexCode = message.substring(matcher1.start(), matcher1.end());
            String replaceSharp = hexCode.replace('#','x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher1 = pattern1.matcher(message);
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
    public String fullName = hex("#FCD05C§lC#FCD05C§li#FCD05C§lv#FCD05C§li#FCD05C§ll#E0CF56§li#C4CD50§ls#A8CC4A§la#8CCA44§lt#70C93D§li#54C737§lo#38C631§ln #00C325§lC#00CD38§lr#00D74B§lé#00E15F§la#00EB72§lt#00F585§li#00FF98§lf");
    public String shortName = hex("#FCD05C§lC#FCD05C§li#FCD05C§lv#D2CE53§li#A8CC4A§ll#7ECA41§li#54C737§ls#2AC52E§la#00C325§lt#00D74B§li#00EB72§lo#00FF98§ln");
    public String prefix = hex("§e[" + shortName + "] §2");
    public String makeItSafePrefix = "§1[§b§lMake It Safe§1] §a";
    public String vanishNamePrefix = hex("#C30000§lV#CF1D00§lA#DB3A00§lN#E75700§lI#F37400§lS#FF9100§lH ");
    public String playerToExc = prefix + "§4Vous devez être un joueur pour éxecuter cette commande !";
    public String noPermToExc = prefix + "§4Vous n'avez pas la permission d'éxecuter cette commande !";
    public String wrongUsage = prefix + "§4Utilisation : §c";
    public String moneySign = "¢";
    public String playerNotFound(String name) {return prefix + "§4Impossible de trouver le joueur §c" + name;}
    public void logError(String message,Exception error) {
        Bukkit.getConsoleSender().sendMessage(prefix + "§4" + hex(message) + " because of §eerror:");
        Bukkit.getLogger().severe(error + Arrays.toString(error.getStackTrace()).replace(",", ",\n"));
    }
    public String joinMessage(Player p) { return "§7[§a+§7] §e" + p.getDisplayName(); }
    public String leaveMessage(Player p) { return "§7[§c-§7] §e" + p.getDisplayName(); }
    //END OF PUBLIC UTILITIES
    //HELPERS GET
    PlayerData pdata;
    Backup backup;
    TerritoryData territoryData;
    Warp warp;
    SidebarManager sidebarManager;
    VersionChecker versionChecker;
    public double serverVersion = 0.0;
    SkinManager skinManager;
    //END OF HELPERS GET
    //OTHER ARRAY LISTS
    public ArrayList<Player> seeTerritoryBorders = new ArrayList<>();
    public ArrayList<Player> bypassClaims = new ArrayList<>();
    public ArrayList<Player> vanished = new ArrayList<>();
    //END OF OTHER ARRAY LISTS
    //SCOREBOARDS

    public void loadConfigs(){
        pdata = null;
        backup = null;
        territoryData = null;
        warp = null;
        sidebarManager = null;
        versionChecker = null;
        skinManager = null;

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
        sidebarManager = new SidebarManager(this, this,territoryData);
        versionChecker = new VersionChecker(this);
        serverVersion = versionChecker.getVersion();
        skinManager = new SkinManager(this);
    }
    @SuppressWarnings("DataFlowIssue")
    @Override
    public void onEnable() {

        //HELPERS INIT
        loadConfigs();

        //END OF HELPERS INIT

        getCommand("minijeuxentrepotes").setExecutor(new MJEPCommand(this));
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("xp").setExecutor(new XPCommand(this));
        getCommand("money").setExecutor(new MoneyCommand(this));
        getCommand("rank").setExecutor(new RankCommand(this));
        getCommand("civilisation").setExecutor(new CivlisationCommand(this));
        getCommand("backup").setExecutor(new BackupCommand(this, backup));
        getCommand("territoire").setExecutor(new Command(this,territoryData));
        getCommand("home").setExecutor(new HomeCommand(this));
        getCommand("sethome").setExecutor(new HomeCommand(this));
        getCommand("delhome").setExecutor(new HomeCommand(this));
        getCommand("warp").setExecutor(new WarpCommand(this,warp));
        getCommand("setwarp").setExecutor(new WarpCommand(this,warp));
        getCommand("delwarp").setExecutor(new WarpCommand(this,warp));
        getCommand("nick").setExecutor(new NickCommand(this,skinManager));
        getCommand("vanish").setExecutor(new VanishCommand(this, this));

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new JoinListener(this, territoryData, sidebarManager), this);
        pm.registerEvents(new EventListener(this), this);
        pm.registerEvents(new Listener(this,this, territoryData), this);
        pm.registerEvents(new AntiSpeed(this), this);
        pm.registerEvents(new VanishCommand(this,this), this);
        WorldEdit.getInstance().getEventBus().register(new WorldEditListener(this, territoryData));

        Bukkit.getServerLinks().addLink("§x§0§8§4§C§F§BD§x§0§7§6§5§F§Ci§x§0§5§7§D§F§Cs§x§0§4§9§6§F§Dc§x§0§3§A§E§F§Eo§x§0§1§C§7§F§Er§x§0§0§D§F§F§Fd",URI.create("https://dsc.gg/mjep"));
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for(Player all : Bukkit.getOnlinePlayers()) {
                all.setPlayerListHeader("§2§lBienvenue " + all.getDisplayName() +  "§2§l\n sur §6le serveur " + fullName + " !\n");
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
                String chunkOwner = territoryData.getChunkOwner(all.getLocation().getChunk());
                Team chunkOwnerTeam = null;
                ChatColor chunkOwnerColor = null;
                if(chunkOwner!=null) {
                    chunkOwnerTeam = territoryData.getTerritoryTeam(chunkOwner);
                    chunkOwnerColor = chunkOwnerTeam.getColor();
                }

                all.setPlayerListFooter(
                        "\n§2Nombre de joueurs en ligne : §6" + ndj +
                        "§r\n§2IP : §6§lcivilisation-mjep.mine.fun" + "§r\n§2Heure : §6" + h + "§e:§6" + m +
                        "§r\n§2Monde : §6" + worldname +
                        "§r§2,\n Position : §eX: §6" + all.getLocation().getBlockX() +
                        "§r§2, §eY: §6" + all.getLocation().getBlockY() +
                        "§r§2, §eZ: §6" + all.getLocation().getBlockZ() +
                        "§r§2, Territoire : " + (chunkOwnerTeam==null ? "§2§lWilderness" : chunkOwnerColor + chunkOwnerTeam.getName())
                );
            }
        }, 1, 1);

        backup.scheduleNextBackup();

        territoryData.programNextWorkerCheckup();
        Bukkit.getConsoleSender().sendMessage(prefix + "§eLoading spawn world...");
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
        Bukkit.getConsoleSender().sendMessage(prefix + "§2Spawn world loaded !");
    }

    private String getWorldname(Player all) {
        String worldname = Objects.requireNonNull(all.getLocation().getWorld()).getName();

        if(worldname.equalsIgnoreCase("world")) {
            worldname = this.shortName + " §e- §6Overworld";

        } else if(worldname.equalsIgnoreCase("world_nether")) {
            worldname = this.shortName + " §e- §cNether";

        } else if(worldname.equalsIgnoreCase("world_the_end")) {
            worldname = this.shortName + " §e- §5End";

        } else if(worldname.equalsIgnoreCase("spawn")) {
            worldname = this.shortName + " §e- §6Spawn";

        } else {
            worldname = this.shortName + " §e- " + all.getLocation().getWorld().getName();
        }
        return worldname;
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onDisable() {
        for(Player all : Bukkit.getOnlinePlayers()) {
            all.sendMessage("§e(§6!§e)§4 Le serveur " + fullName + " §credémarre§4 !");
            all.sendTitle("§4🚀 Redémarrage du serveur...","",20,100,20);
            all.transfer("91.197.6.60", 25599);
        }
        if (territoryData != null && territoryData.workerCheckupTask!=null) {
            territoryData.workerCheckupTask.cancel();
        }
        if(backup != null && backup.backupTask !=null){
            backup.backupTask.cancel();
        }
        super.onDisable();
    }

}
