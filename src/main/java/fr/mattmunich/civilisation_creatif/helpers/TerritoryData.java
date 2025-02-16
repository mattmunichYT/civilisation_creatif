package fr.mattmunich.civilisation_creatif.helpers;

import fr.mattmunich.civilisation_creatif.Main;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TerritoryData {
    private final Plugin plugin;

    private Main main;

    public TerritoryData(Plugin plugin, Main main) {
        this.plugin = plugin;
        this.main = main;
    }

    private BukkitTask workerCheckupTask = null;

    public int baseChunkPrice = 300;
    public int changeBannerPrice = 300;

    public final Plugin getPlugin() {
        return plugin;
    }

    private FileConfiguration config;
    private File file;

    public FileConfiguration getConfig() {
        return config;
    }

    public void initConfig() {

        File f = new File("plugins/CivilisationCreatif/");
        if (!f.exists()) {
            f.mkdirs();
        }
        file = new File(f, "territoires.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);

    }

    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void createTerritory(Player chief, String territoryName, ChatColor territoryColor) {
        try {
            if(config.contains(territoryName)) {
                chief.sendMessage(main.prefix + "§4Impossible de créer le territoire, §cun territoire avec le même nom existe déjà !");
                return;
            }

            chief.sendMessage(main.prefix + "§a§oCréation du territoire...");

            Team territory = createTerritoryTeam(chief, territoryName, territoryColor);

            config.set("territories." + territoryName + ".chief.name", chief.getName());
            config.set("territories." + territoryName + ".chief.UUID", chief.getUniqueId().toString());
            config.set("territories." + territoryName + ".officers", new ArrayList<String>());
            config.set("territories." + territoryName + ".created", System.currentTimeMillis());
            config.set("territories." + territoryName + ".color", territoryColor.toString());
            config.set("territories." + territoryName + ".xp", 0);
            config.set("territories." + territoryName + ".money", 0);
            ItemStack banner = new ItemStack(Material.WHITE_BANNER);
            config.set("territories." + territoryName + ".banner", banner);

            addTerritoryToList(territoryName);

            PlayerData data = new PlayerData(chief.getUniqueId());
            data.setTerritory(territoryName);

            boolean check = updateMemberInConfig(territoryName);
            if(!check) {
                Bukkit.getConsoleSender().sendMessage(main.prefix + "§c§oUne erreur NON fatale s'est produite lors de la création du territoire " + territoryName);
            }
            saveConfig();
            chief.sendMessage(main.prefix + "§2Le territoire §6" + territory.getColor() + territory.getName() + "§2 a été créé avec succès !");
            Bukkit.broadcastMessage(main.prefix + "§6" + chief.getName() + "§a a créé le territoire " + territory.getColor() + territory.getName() + " §a!");
        } catch (Exception e) {
            chief.sendMessage(main.prefix + "§4Impossible de créer le territoire, §cveuillez signaler cela à un membre du staff.");
            main.logError("Couldn't create territory " + territoryName + " from list",e);
        }
    }

    public void deleteTerritory(Player sender, String territoryName) {
        //if(getTerritoriesList().contains(territoryName)) {
        try {
            config.set("territories." + territoryName, null);
            for(String entry : getTerritoryTeam(territoryName).getEntries()) {
                Player p = Bukkit.getPlayer(entry);
                if(p != null ) {
                    PlayerData pdata = new PlayerData(p.getUniqueId());
                    pdata.setTerritory(null);
                    if(p.isOnline()) {
                        pdata.setTerritory(null);
                        if(pdata.getRank() == null) {
                            p.kickPlayer("§cVotre territoire a été supprimé \n§e et une erreur s'est produite lors de l'obtention de votre grade!");
                            return;
                        }
                        p.setDisplayName(main.hex(pdata.getRank().getPrefix()) + p.getName() + pdata.getRank().getSuffix());
                        if(!p.getName().equals(sender.getName())) {
                            p.sendMessage(main.prefix + "§cVotre territoire a été supprimé !");
                        }
                    }
                }
            }
            removeTerritoryFromList(territoryName);
            deleteTerritoryTeam(territoryName);
            saveConfig();
        } catch (Exception e) {
            sender.sendMessage(main.prefix + "§4Impossible de supprimer le territoire, veuillez signaler cela à un membre du staff.");
            main.logError("Couldn't remove territory " + territoryName + " from list",e);
        }
//        } else {
//            sender.sendMessage(main.prefix + "§4Impossible de supprimer le territoire, §cterritoire non trouvé.");
//        }
    }

    public void leaveTerritory(Player sender) {
        Team sTeam = getTerritoryTeamOfPlayer(sender);
        if(isChief(sender,sTeam.getName())) {
            sender.sendMessage(main.prefix + "§cVous êtes le chef de ce territoire, §esi vous souhaitez le quitter, veuillez\n" +
                    "§4- §4§lSupprimer le territoire\n" +
                    "§r§eOU\n" +
                    "§a- §a§lDonner les permissions de chef à un autre joueur");
            return;
        }
        if(sTeam.getEntries().size() == 1) {
            sender.sendMessage(main.prefix + "§cVous êtes le seul membre de ce territoire, §eveuillez donc le supprimer pour le quitter !");
            return;
        }

        sTeam.removeEntry(sender.getName());
        List<String> membersUUID = getTerritoryMembersUUID(sTeam.getName());
        try {
            membersUUID.remove(sender.getUniqueId().toString());
            PlayerData pdata = new PlayerData(sender.getUniqueId());
            pdata.setTerritory(null);
            sendAnouncementToTerritory(sTeam.getName(), "§c" + sender.getName() + "§4 a quitté le territoire.");
        } catch (Exception ignored) {}
        config.set("territories." + sTeam.getName() + ".membersUUID", membersUUID);
        saveConfig();
        sender.sendMessage(main.prefix + "§eVous avez quitté votre territoire !");
    }

    public void joinTerritory(Player p, String territoryName) {
        Team territory = getTerritoryTeam(territoryName);
        territory.addEntry(p.getName());
        List<String> membersUUID = getTerritoryMembersUUID(territoryName);
        try {
            PlayerData pdata = new PlayerData(p.getUniqueId());
            pdata.setTerritory(territoryName);
            membersUUID.add(p.getUniqueId().toString());
            sendAnouncementToTerritory(territoryName, "§a" + p.getName() + "§2 a rejoin le territoire !");
        } catch (Exception ignored) {}
        config.set("territories." + territory.getName() + ".membersUUID", membersUUID);
        saveConfig();
        p.sendMessage(main.prefix + "§2Vous avez rejoint le territoire " + territory.getColor() + territory.getName() + "§2 !");
    }

    public List<String> getTerritoryMembersUUID(String territoryName) {
        return config.getStringList("territories." + territoryName + ".membersUUID");
    }

    public String getTerritoryChiefUUID(String territoryName) {
        return Objects.requireNonNull(config.getString("territories." + territoryName + ".chief.UUID"));
    }

    public List<String> getTerritoryOfficers(String territoryName){
        if(config.get("territories." + territoryName + ".officers") == null) {
            return new ArrayList<String>();
        } else {
            return config.getStringList("territories." + territoryName + ".officers");
        }
    }

    public void addOfficer(OfflinePlayer target, Player sender) {
        if(!target.hasPlayedBefore()){
            sender.sendMessage(main.prefix + "§4Le joueur ne s'est jamais connecté !");
            return;
        }
        if(getPlayerTerritory(sender)==null){
            sender.sendMessage(main.prefix + "§4Vous devez être dans un territoire pour faire cela !");
            return;
        }
        if(!isChief(sender,getPlayerTerritory(sender))){
            sender.sendMessage(main.prefix + "§4Vous devez être le chef de votre territoire pour faire cela !");
            return;
        }
        if(!getPlayerTerritory(target).equalsIgnoreCase(getPlayerTerritory(sender))){
            sender.sendMessage(main.prefix + "§4Vous devez être devez être dans le même territoire que la cible pour faire cela !");
            return;
        }
        List<String> officers = getTerritoryOfficers(getPlayerTerritory(sender));
        if(officers.contains(target.getUniqueId().toString())){
            sender.sendMessage(main.prefix + "§4La cible est déjà un officier dans votre territoire !");
            return;
        }
        if(target==sender) {
            sender.sendMessage(main.prefix + "§4Vous êtes déjà chef de votre territoire !");
            return;
        }
        officers.add(target.getUniqueId().toString());
        config.set("territories." + getPlayerTerritory(sender) + ".officers",officers);
        saveConfig();
        sender.sendMessage(main.prefix + "§2Le joueur §a" + target.getName() + "§2 a été ajouté aux officiers de votre territoire !");
        if(target.isOnline() && target.getPlayer() !=null){
            target.getPlayer().sendMessage(main.prefix + "§2Vous êtes désormais officier dans le territoire §a" + getPlayerTerritory(sender) + "§2 !");
        }
    }

    public void removeOfficer(OfflinePlayer target, Player sender){
        if(!target.hasPlayedBefore()){
            sender.sendMessage(main.prefix + "§4Le joueur ne s'est jamais connecté !");
            return;
        }
        if(getPlayerTerritory(sender)==null){
            sender.sendMessage(main.prefix + "§4Vous devez être dans un territoire pour faire cela !");
            return;
        }
        if(!isChief(sender,getPlayerTerritory(sender))){
            sender.sendMessage(main.prefix + "§4Vous devez être le chef de votre territoire pour faire cela !");
            return;
        }
        if(!getPlayerTerritory(target).equalsIgnoreCase(getPlayerTerritory(sender))){
            sender.sendMessage(main.prefix + "§4Vous devez être devez être dans le même territoire que la cible pour faire cela !");
            return;
        }
        List<String> officers = getTerritoryOfficers(getPlayerTerritory(sender));
        if(!officers.contains(target.getUniqueId().toString())){
            sender.sendMessage(main.prefix + "§4La cible n'est pas un officier dans votre territoire !");
            return;
        }
        officers.remove(target.getUniqueId().toString());
        config.set("territories." + getPlayerTerritory(sender) + ".officers",officers);
        saveConfig();
    }

    public boolean isInTerritory(OfflinePlayer player, String territoryName) {
        return getPlayerTerritory(player).equals(territoryName);
    }

    public boolean hasTerritory(OfflinePlayer player) {
        return getPlayerTerritory(player) !=null;
    }

    public boolean isOfficer(OfflinePlayer player, String territoryName){
        if(!hasTerritory(player) || !isInTerritory(player,territoryName)) {
            return false;
        }
        List<String> officers = getTerritoryOfficers(getPlayerTerritory(player));
        return officers.contains(player.getUniqueId().toString());
    }

    public boolean isChief(OfflinePlayer player, String territoryName){
        if(!hasTerritory(player) || !isInTerritory(player,territoryName)) {
            return false;
        }
        return getTerritoryChiefUUID(getPlayerTerritory(player)).equals(player.getUniqueId().toString());
    }

    public String getPlayerTerritory(OfflinePlayer player) {
        try {
            PlayerData data = new PlayerData(player.getUniqueId());
            return data.getTerritory();
        } catch (Exception e) {
            return null;
        }
    }

    public void setTerritoryTeamEntries(String territoryName) {
        Team territory = getTerritoryTeam(territoryName);
        List<String> membersUUID = getTerritoryMembersUUID(territoryName);
        for(String memberUUID : membersUUID) {
            try {
                String memberName = Utility.getNameFromUUID(UUID.fromString(memberUUID));
                territory.addEntry(memberName);
            } catch (Exception ignored){}
        }
    }

    public void setPlayerTerritoryTeam(Player player) {
        try {
            PlayerData data = new PlayerData(player.getUniqueId());
            Team territory = getTerritoryTeam(data.getTerritory());
            if(territory != null) {
                try {
                    territory.addEntry(player.getName());
                } catch (NullPointerException ignored) {}
            }
        } catch (Exception ignored) {}
    }

    public Team createTerritoryTeam(Player chief, String territoryName, ChatColor territoryColor) {
        ScoreboardManager scMan = Bukkit.getScoreboardManager();
        assert scMan != null;
        Scoreboard sc = scMan.getMainScoreboard();
        Team territory = sc.registerNewTeam(territoryName);
        territory.addEntry(chief.getName());
        territory.setAllowFriendlyFire(true);
        territory.setCanSeeFriendlyInvisibles(true);
        territory.setDisplayName(territoryColor + territoryName);
        territory.setPrefix("§7" + territoryName.toUpperCase() + " §r");
        territory.setColor(territoryColor);
        return territory;
    }

    public Team getTerritoryTeam(String territoryName) {
        try {
            ScoreboardManager scMan = Bukkit.getScoreboardManager();
            assert scMan != null;
            Scoreboard sc = scMan.getMainScoreboard();
            return sc.getTeam(territoryName);
        } catch (NullPointerException e) {
            throw new NullPointerException("Team not found");
        }
    }

    public @Nullable Team getTerritoryTeamFromItem(ItemStack it) {
        try {
            ScoreboardManager scMan = Bukkit.getScoreboardManager();
            if(scMan==null) {
                return null;
            }
            Scoreboard sc = scMan.getMainScoreboard();
            if(it.getItemMeta()==null) {
                return null;
            }
            return sc.getTeam(it.getItemMeta().getDisplayName().substring(2));
        } catch (NullPointerException e) {
            return null;
        }
    }

    public Team getTerritoryTeamOfPlayer(Player p) {
        try {
            ScoreboardManager scMan = Bukkit.getScoreboardManager();
            assert scMan != null;
            Scoreboard sc = scMan.getMainScoreboard();
            return sc.getEntryTeam(p.getName());
        } catch (NullPointerException e) {
            throw new NullPointerException("No territory found");
        }
    }
    public Team getTerritoryTeamOfEntry(String entryName) {
        try {
            ScoreboardManager scMan = Bukkit.getScoreboardManager();
            assert scMan != null;
            Scoreboard sc = scMan.getMainScoreboard();
            return sc.getEntryTeam(entryName);
        } catch (NullPointerException e) {
            throw new NullPointerException("No territory found");
        }
    }

    public void deleteTerritoryTeam(String territoryName) {
        try {
            ScoreboardManager scMan = Bukkit.getScoreboardManager();
            assert scMan != null;
            Scoreboard sc = scMan.getMainScoreboard();
            Team territory = sc.getTeam(territoryName);
            assert territory != null;
            territory.unregister();
        } catch (NullPointerException e) {
            throw new NullPointerException("Team not found");
        }
    }

    public boolean updateMemberInConfig(String territoryName) {
        try {
            Team territory = getTerritoryTeam(territoryName);
            ArrayList<String> membersUUID = new ArrayList<>();
            for (String entry : territory.getEntries()) {
                UUID entryUUID = Utility.getUUIDFromName(entry);
                membersUUID.add(entryUUID.toString());
            }
            config.set("territories." + territoryName + ".membersUUID", membersUUID);
            saveConfig();
            return true;
        } catch (Exception e) {
            main.logError("Couldn't update territory members in config from team for " + territoryName + " from list", e);
            return false;
        }
    }

    public List<String> getTerritoriesList() {
        return config.getStringList("list");
    }

    public void setTerritoriesList(List<String> territories) {
        config.set("list", territories);
        saveConfig();
    }

    public void addTerritoryToList(String territoryName) {
        List<String> territories = getTerritoriesList();
        territories.add(territoryName);
        setTerritoriesList(territories);
        saveConfig();
    }

    public void removeTerritoryFromList(String territoryName) {
        List<String> territories = getTerritoriesList();
        try {
            territories.remove(territoryName);
        } catch (Exception e) {
            main.logError("Couldn't remove territory " + territoryName + " from list",e);
        }
        setTerritoriesList(territories);
        saveConfig();
    }

    public List<Team> getTerritoriesTeam(){
        List<String> territoriesNames = config.getStringList("list");
        List<Team> territories = new ArrayList<Team>();
        for (String territoryName : territoriesNames) {
            Team territory = getTerritoryTeam(territoryName);
            territories.add(territory);
        }
        return territories;
    }

    public void invitePlayer(Player sender, OfflinePlayer target){
        if(target==null) {
            sender.sendMessage(main.prefix + "§4Joueur non trouvé !");
            return;
        }
        try {
            Team territory = getTerritoryTeamOfPlayer(sender);
            List<String> invitedPlayer = config.getStringList("territories." + territory.getName() + ".invites");
            invitedPlayer.add(target.getUniqueId().toString());
            config.set("territories." + getTerritoryTeamOfPlayer(sender).getName() + ".invites", invitedPlayer);
            saveConfig();
            PlayerData tdata = new PlayerData(target.getUniqueId());
            try {
                if(tdata.getInvitesToTerritory().stream().anyMatch(map -> map.containsKey(territory.getName()))){
                    sender.sendMessage(main.prefix + "§cLe joueur est déjà invité à rejoindre votre territoire !");
                    return;
                }
            } catch (NullPointerException ignored){}
            tdata.inviteToTerritory(territory.getName(), sender);
            if(target.getPlayer() != null) {
                target.getPlayer().sendMessage("\n\n" + main.prefix + "§2Vous avez été invité à rejoindre le territoire " + territory.getColor() + territory.getName() + "§2 !\n" + main.prefix + "§8§o/territoire §r§7pour rejoindre\n\n");
                target.getPlayer().playSound(target.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            }
            sender.sendMessage(main.prefix + "§2Le joueur a été invité à rejoindre votre territoire !");
        } catch (Exception e) {
            sender.sendMessage(main.prefix + "§4Une erreur s'est produite lors de l'invitation du joueur !");
            main.logError("Couldn't invite player to territory",e);
            
        }

    }
    public void removeInvite(String territoryName, OfflinePlayer target){
        try {
            assert target != null;
            Team territory = getTerritoryTeam(territoryName);
            List<String> invitedPlayer = config.getStringList("territories." + territory.getName() + ".invites");
            invitedPlayer.remove(target.getUniqueId().toString());
            config.set("territories." + territory.getName() + ".invites", invitedPlayer);
            saveConfig();
            PlayerData tdata = new PlayerData(target.getUniqueId());
            tdata.removeInviteToTerritory(territory.getName());
        } catch (Exception e) {
            main.logError("Couldn't remove invite to territory",e);
        }
    }

    public void removeInviteMsgPlayer(Player sender, String territoryName, Player target){
        if(target==null) {
            sender.sendMessage(main.prefix + "§4Joueur non trouvé !");
            return;
        }
        try {
            Team territory = getTerritoryTeam(territoryName);
            List<String> invitedPlayer = config.getStringList("territories." + territory.getName() + ".invites");
            invitedPlayer.remove(target.getUniqueId().toString());
            config.set("territories." + territory.getName() + ".invites", invitedPlayer);
            saveConfig();
            PlayerData tdata = new PlayerData(target.getUniqueId());
            tdata.removeInviteToTerritory(territory.getName());
            sender.sendMessage(main.prefix + "§2L'invitation à rejoindre votre territoire a été supprimée !");
        } catch (Exception e) {
            sender.sendMessage(main.prefix + "§4Une erreur s'est produite lors de la suppression de l'invitation du joueur !");
            main.logError("Couldn't remove invite to territory",e);
        }
    }

    public void claimChunk(Player sender, String territoryName, Map<Integer,Integer> chunk) {
        try {
            Team territory = getTerritoryTeam(territoryName);
            List<Map<?,?>> terrClaims = config.getMapList("territories." + territory.getName() + ".claims");
            List<Map<?,?>> globalClaims = config.getMapList("claims");
            if(globalClaims.contains(chunk)) {
                if(terrClaims.contains(chunk)) {
                    sender.sendMessage(main.prefix + "§eVous avez déjà claim ce chunk !");
                } else {
                    sender.sendMessage(main.prefix + "§cVous ne pouvez pas claim un chunk car il est §4déjà claim §cpar le territoire §4" + getChunkOwner(chunk) + " §c!");
                }
                return;
            }
            if(getTerritoryMoney(territoryName) < getChunkPrice(territoryName)){
                sender.sendMessage(main.prefix + "§4Iln'y a pas assez d'argent dans la banque du territoire !");
                return;
            }
            terrClaims.add(chunk);
            globalClaims.add(chunk);
            config.set("territories." + territory.getName() + ".claims", terrClaims);
            config.set("claims", globalClaims);
            saveConfig();
            removeTerritoryMoney(territoryName,getChunkPrice(territoryName));
            sender.sendMessage(main.prefix + "§aVous avez §2claim §ale chunk §e" + chunk.keySet().stream().findFirst().toString().replace("Optional[","").replace("]","") + "§a,§e " + chunk.values().stream().findFirst().toString().replace("Optional[","").replace("]","") + " §apour §e"  + getChunkPrice(territoryName) + main.moneySign + "§a!");
            addOneClaimedChunkToCount(territoryName);
            return;
        } catch (Exception e) {
            sender.sendMessage(main.prefix + "§4Une erreur s'est produite lors du claim du chunk !");
            main.logError("Couldn't claim chunk for territory " + territoryName,e);
            return;
        }
    }

    public int getChunkPrice(String territoryName){
        return baseChunkPrice + ((getClaimdChunkCount(territoryName)) * 50);
    }

    public void setClaimedChunkCount(String territoryName, int claimedChunkCount){
        config.set("territories." + territoryName + ".claimedChunkCount",claimedChunkCount);
        saveConfig();
    }

    public void addOneClaimedChunkToCount(String territoryName) {
        int currentClaimedChunkCount = getClaimdChunkCount(territoryName);
        setClaimedChunkCount(territoryName, currentClaimedChunkCount+1);
    }

    public void removeOneClaimedChunkFromCount(String territoryName) {
        int currentClaimedChunkCount = getClaimdChunkCount(territoryName);
        setClaimedChunkCount(territoryName, currentClaimedChunkCount-1);
    }

    public int getClaimdChunkCount(String territoryName){
        return config.get("territories." + territoryName + ".claimedChunkCount") == null ? 0 : config.getInt("territories." + territoryName + ".claimedChunkCount");
    }

    public void unclaimChunk(Player sender, String territoryName, Map<Integer,Integer> chunk) {
        try {
            Team territory = getTerritoryTeam(territoryName);
            List<Map<?,?>> claims = config.getMapList("territories." + territory.getName() + ".claims");
            if(claims.contains(chunk)) {
                claims.remove(chunk);
                config.set("territories." + territory.getName() + ".claims", claims);

                List<Map<?,?>> globalClaims = config.getMapList("claims");
                globalClaims.remove(chunk);
                config.set("claims", globalClaims);
                saveConfig();
                sender.sendMessage(main.prefix + "§aVous avez §cunclaim §ale chunk §e" + chunk.keySet().stream().findFirst().toString().replace("Optional[","").replace("]","") + "§a,§e " + chunk.values().stream().findFirst().toString().replace("Optional[","").replace("]",""));
                removeOneClaimedChunkFromCount(territoryName);
                return;
            } else {
                sender.sendMessage(main.prefix + "§cVous n'avez pas claim le chunk §e" + chunk.keySet().stream().findFirst().toString().replace("Optional[","").replace("]","") + "§a,§e " + chunk.values().stream().findFirst().toString().replace("Optional[","").replace("]",""));
            }

        } catch (Exception e) {
            sender.sendMessage(main.prefix + "§4Une erreur s'est produite lors de l'unclaim du chunk !");
            main.logError("Couldn't UNclaim chunk for territory " + territoryName,e);
            return;

        }
    }

    public List<Map<?, ?>> getTerritoryChunks(String territoryName) {
        try {
            Team territory = getTerritoryTeam(territoryName);
            try {
                return config.getMapList("territories." + territory.getName() + ".claims");
            } catch (ClassCastException e) {
                main.logError("Couldn't cast List<Map<?, ?>> for chunks of territory " + territoryName,e);
                return null;
            }
        } catch (Exception e) {
            main.logError("Couldn't get claimed chunks for territory " + territoryName,e);
            return null;

        }
    }

    public boolean chunkClaimed(Map<Integer,Integer> chunk) {
        try {
            List<Map<?,?>> globalClaims = config.getMapList("claims");
            return globalClaims.contains(chunk);
        } catch (Exception e) {
            main.logError("Couldn't check if chunk §c was claimed",e);
            return false;

        }
    }

    public String getChunkOwner(Map<Integer,Integer> chunk) {
        try {
            if(!chunkClaimed(chunk)) {
                return null;
            }
            try {
                for(String key : config.getConfigurationSection("territories").getKeys(false)) {
                    List<Map<?, ?>> territoryClaims = getTerritoryChunks(key);
                    try {
                        for (Map<?, ?> territoryClaim : territoryClaims) {
                            if(territoryClaim.equals(chunk)){
                                return key;
                            }
                        }
                    } catch (Exception e) {
                        main.logError("Couldn't check chunk owner",e);
                    }
                }
                return null;
            } catch (NullPointerException e) {
                main.logError("Couldn't check chunk owner",e);
                return null;
            }
        } catch (Exception e) {
            main.logError("Couldn't check chunk owner",e);
            return null;

        }
    }

    public Map<Integer,Integer> getChunkMap(Chunk chunk){
        int x = chunk.getX();
        int z = chunk.getZ();
        Map<Integer,Integer> chunkMap = new HashMap<>();
        chunkMap.put(x,z);
        return chunkMap;
    }

    public Chunk getChunkFromMap(Map<Integer,Integer> chunkMap, World world){
        int x = Integer.parseInt(chunkMap.entrySet().stream().findFirst().toString().replace("Optional[","").replace("]",""));
        int z = Integer.parseInt(chunkMap.values().stream().findFirst().toString().replace("Optional[","").replace("]",""));
        return world.getChunkAt(x,z);
    }

    public void showChunkBorder(Chunk chunk, ChatColor chatColor, Player player) {
        World world = chunk.getWorld();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        int minX = chunkX * 16;
        int minZ = chunkZ * 16;
        double y = player.getLocation().getBlockY() + 1.5;

        Particle.DustOptions dustOptions = getDustOptions(chatColor);
        Chunk north = world.getChunkAt(chunkX, chunkZ - 1);


        if (!Objects.equals(getChunkOwner(getChunkMap(north)), getChunkOwner(getChunkMap(chunk)))) {
            for (int x = minX; x < minX + 16; x++) {
                player.spawnParticle(Particle.DUST, x, y, minZ, 1, dustOptions);
            }
        }

        Chunk south = world.getChunkAt(chunkX, chunkZ + 1);
        if (!Objects.equals(getChunkOwner(getChunkMap(south)), getChunkOwner(getChunkMap(chunk)))) {
            for (int x = minX; x < minX + 16; x++) {
                player.spawnParticle(Particle.DUST, x, y, minZ + 16, 1, dustOptions);
            }
        }

        Chunk west = world.getChunkAt(chunkX - 1, chunkZ);
        if (!Objects.equals(getChunkOwner(getChunkMap(west)), getChunkOwner(getChunkMap(chunk)))) {
            for (int z = minZ; z < minZ + 16; z++) {
                player.spawnParticle(Particle.DUST, minX, y, z, 1, dustOptions);
            }
        }

        Chunk east = world.getChunkAt(chunkX + 1, chunkZ);
        if (!Objects.equals(getChunkOwner(getChunkMap(east)), getChunkOwner(getChunkMap(chunk)))) {
            for (int z = minZ; z < minZ + 16; z++) {
                player.spawnParticle(Particle.DUST, minX + 16, y, z, 1, dustOptions);
            }
        }
    }

    private static Particle.DustOptions getDustOptions(ChatColor chatColor) {
        Color color = Color.WHITE;
        return switch (chatColor) {
            case AQUA -> {
                color = Color.fromRGB(84,255,255);
                yield new Particle.DustOptions(color, 1);
            }
            case BLACK -> {
                color = Color.BLACK;
                yield new Particle.DustOptions(color, 1);
            }
            case BLUE -> {
                color = Color.fromRGB(85,85,255);
                yield new Particle.DustOptions(color, 1);
            }
            case DARK_AQUA -> {
                color = Color.fromRGB(0,170,170);
                yield new Particle.DustOptions(color, 1);
            }
            case DARK_BLUE -> {
                color = Color.fromRGB(2,0,170);
                yield new Particle.DustOptions(color, 1);
            }
            case GRAY -> {
                color = Color.fromRGB(170,170,170);
                yield new Particle.DustOptions(color, 1);
            }
            case DARK_GRAY -> {
                color = Color.fromRGB(85,85,85);
                yield new Particle.DustOptions(color, 1);
            }
            case DARK_GREEN-> {
                color = Color.fromRGB(2,170,1);
                yield new Particle.DustOptions(color, 1);
            }
            case GREEN-> {
                color = Color.fromRGB(86,255,84);
                yield new Particle.DustOptions(color, 1);
            }
            case DARK_PURPLE -> {
                color = Color.fromRGB(170,1,170);
                yield new Particle.DustOptions(color, 1);
            }
            case LIGHT_PURPLE -> {
                color = Color.fromRGB(255,85,255);
                yield new Particle.DustOptions(color, 1);
            }
            case DARK_RED -> {
                color = Color.fromRGB(170,0,1);
                yield new Particle.DustOptions(color, 1);
            }
            case RED -> {
                color = Color.fromRGB(255,85,85);
                yield new Particle.DustOptions(color, 1);
            }
            case YELLOW -> {
                color = Color.fromRGB(255,255,85);
                yield new Particle.DustOptions(color, 1);
            }
            case GOLD -> {
                color = Color.fromRGB(255,170,1);
                yield new Particle.DustOptions(color, 1);
            }
            case WHITE -> {
                color = Color.WHITE;
                yield new Particle.DustOptions(color, 1);
            }
            default -> new Particle.DustOptions(color, 1);
        };
    }

    public void sendAnouncementToTerritory(String territoryName, String message){
        Team terr = getTerritoryTeam(territoryName);
        for(String eName : terr.getEntries()) {
            if(Bukkit.getPlayer(eName) != null) {
                Player p = Bukkit.getPlayer(eName);
                assert p != null;
                p.sendMessage(main.prefix + " §6-> §" + terr.getColor() + terr.getName() + " §8§l>>> §6" + message);
            }
        }
        return;
    }

    public void setTerritoryBanner(String territoryName, ItemStack banner){
        config.set("territories." + territoryName + ".banner", banner);
        saveConfig();
    }

    public ItemStack getTerritoryBanner(String territoryName){
        if(config.get("territories." + territoryName + ".banner") == null) {
            return new ItemStack(Material.WHITE_BANNER);
        } else {
            return config.getItemStack("territories." + territoryName + ".banner");
        }
    }

    public int getTerritoryXP(String territoryName){
        return config.getInt("territories." + territoryName + ".xp");
    }

    public void setTerritoryXP(String territoryName, int xp){
        config.set("territories." + territoryName + ".xp", xp);
        saveConfig();
    }

    public void addTerritoryXP(String territoryName, int xp){
        config.set("territories." + territoryName + ".xp", config.getInt("territories." + territoryName + ".xp") + xp);
        saveConfig();
    }

    public void removeTerritoryXP(String territoryName, int xp){
        config.set("territories." + territoryName + ".xp", config.getInt("territories." + territoryName + ".xp") - xp);
        saveConfig();
    }

    public void resetTerritoryXP(String territoryName){
        config.set("territories." + territoryName + ".xp", 0);
        saveConfig();
    }

    public int getTerritoryMoney(String territoryName){
        return config.getInt("territories." + territoryName + ".money");
    }

    public void setTerritoryMoney(String territoryName, int money){
        config.set("territories." + territoryName + ".money", money);
        saveConfig();
    }

    public void addTerritoryMoney(String territoryName, int money){
        config.set("territories." + territoryName + ".money", config.getInt("territories." + territoryName + ".money") + money);
        saveConfig();
    }

    public void removeTerritoryMoney(String territoryName, int money){
        config.set("territories." + territoryName + ".money", config.getInt("territories." + territoryName + ".money") - money);
        saveConfig();
    }

    public void resetTerritoryMoney(String territoryName){
        config.set("territories." + territoryName + ".money", 0);
        saveConfig();
    }

    public Inventory getTerrListInv_Layout(Player p, int page, int pageNum) {
        Inventory terrListInv = Bukkit.createInventory(p,54,"§aListe des territoires §7- §ePage §6" + page);
        ItemStack none = ItemBuilder.getItem(Material.WHITE_STAINED_GLASS_PANE, null, false, false, null, null, null);
        for (int i = 0; i < 53; i++) {
            terrListInv.setItem(i, none);
        }
        //Borders
        terrListInv.setItem(0,ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE,"",false,false,null,null,null));
        terrListInv.setItem(1,ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE,"",false,false,null,null,null));
        terrListInv.setItem(7,ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE,"",false,false,null,null,null));
        terrListInv.setItem(8,ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE,"",false,false,null,null,null));
        terrListInv.setItem(9,ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE,"",false,false,null,null,null));
        terrListInv.setItem(17,ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE,"",false,false,null,null,null));
        terrListInv.setItem(36,ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE,"",false,false,null,null,null));
        terrListInv.setItem(44,ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE,"",false,false,null,null,null));
        terrListInv.setItem(45,ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE,"",false,false,null,null,null));
        terrListInv.setItem(46,ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE,"",false,false,null,null,null));
        terrListInv.setItem(52,ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE,"",false,false,null,null,null));
        terrListInv.setItem(53,ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE,"",false,false,null,null,null));
        //Borders
        //Inner part
        int[] ranges = {10, 16, 19, 25, 28, 34, 37, 43};

        for (int i = 0; i < ranges.length; i += 2) {
            for (int slot = ranges[i]; slot <= ranges[i + 1]; slot++) {
                terrListInv.setItem(slot, null);
            }
        }
        //Inner part
        //Navigation bar
        if(page!=1) {
            terrListInv.setItem(47,ItemBuilder.getItem(Material.RED_STAINED_GLASS_PANE,"§c§l←",false,false,null,null,null));
        }
        terrListInv.setItem(49,ItemBuilder.getItem(Material.BARRIER,"§c❌ Fermer le menu",false,false,null,null,null));
        if(page!=pageNum){
            terrListInv.setItem(51,ItemBuilder.getItem(Material.LIME_STAINED_GLASS_PANE,"§a§l→",false,false,null,null,null));
        }
        //Navigation bar
        return terrListInv;
    }

    public int extractTerrListPageNumber(String title) {
        // Regular expression to match the integer at the end of the string
        Pattern pattern = Pattern.compile("§6(\\d+)$");
        Matcher matcher = pattern.matcher(title);

        if (matcher.find()) {
            // Extract and return the integer as a number
            return Integer.parseInt(matcher.group(1));
        } else {
            // Default value if no number is found
            return -1;
        }
    }

    public Inventory getTerritoryListInventory(Player p, int page) {
        int itemsPerPage = 28; // Number of items per page
        List<String> territoriesList = getTerritoriesList();
        int totalPages = (int) Math.ceil((double) territoriesList.size() / itemsPerPage);

        // Ensure the page number is within valid range
        if (page < 1 || page > totalPages) {
            page = 1; // Default to page 1 if out of bounds
        }

        // Create the inventory for the specific page
        Inventory terrListInv_Layout = getTerrListInv_Layout(p, page, totalPages);

        // Calculate start and end index for the items on this page
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, territoriesList.size());

        // Populate the inventory with items for the current page
        for (int i = startIndex; i < endIndex; i++) {
            String terr = territoriesList.get(i);
            Team territory = getTerritoryTeam(terr);
            if(territory==null) {
                continue;
            }
            String chiefName = null;
            try {
                OfflinePlayer chief = Bukkit.getOfflinePlayer(UUID.fromString(getTerritoryChiefUUID(terr)));
                chiefName = chief.getName();
            } catch (NullPointerException e) {
                chiefName = "§c§oNon trouvé";
            }

            ItemStack banner = getTerritoryBanner(terr);
            ItemMeta bannerMeta = banner.getItemMeta();
            assert bannerMeta != null;
            bannerMeta.setDisplayName(territory.getColor() + territory.getName());
            bannerMeta.setLore(Arrays.asList("§2Chef: §a" + chiefName, "§2Officiers: §a" + getTerritoryOfficers(terr).size(),"§2Membres: §a" + getTerritoryMembersUUID(terr).size(), "§2XP:§a " + getTerritoryXP(terr), "§2Argent:§a " + getTerritoryMoney(terr)));
            banner.setItemMeta(bannerMeta);

            // Add the item to the next available slot
            terrListInv_Layout.addItem(banner);
        }

        // Return the populated inventory for the specified page
        return terrListInv_Layout;
    }


    public Inventory getTerrInv(Player p, Team territory) {
        Inventory terrInv = Bukkit.createInventory(p, 27, "§aTerritoire : " + territory.getColor() + territory.getName());
        ItemStack none = ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, null, false, false, null, null, null);
        for (int i = 0; i < 26; i++) {
            terrInv.setItem(i, none);
        }
        String terr = territory.getName();
        Player chief = Bukkit.getPlayer(UUID.fromString(getTerritoryChiefUUID(terr)));
        String chiefName = (chief == null) ? "§c§oNon trouvé" : chief.getName();

        ItemStack banner = getTerritoryBanner(terr);
        BannerMeta bannerMeta = (BannerMeta) banner.getItemMeta();
        assert bannerMeta != null;
        if (hasTerritory(p) && (isChief(p,terr))) {
            bannerMeta.setItemName("§r§dDéfinir la bannière du territoire");
            bannerMeta.setLore(Arrays.asList("§2Chef: §a" + chiefName, "§2Officiers: §a" + getTerritoryOfficers(terr).size(),"§2Membres: §a" + getTerritoryMembersUUID(terr).size(), "§2XP:§a " + getTerritoryXP(terr), "§2Argent:§a " + getTerritoryMoney(terr)));
            banner.setItemMeta(bannerMeta);
        } else {
            bannerMeta.setItemName(territory.getColor() + territory.getName());
            bannerMeta.setLore(Arrays.asList("§2Chef: §a" + chiefName, "§2Officiers: §a" + getTerritoryOfficers(terr).size(),"§2Membres: §a" + getTerritoryMembersUUID(terr).size(), "§2XP:§a " + getTerritoryXP(terr), "§2Argent:§a " + getTerritoryMoney(terr)));
            banner.setItemMeta(bannerMeta);
        }
        terrInv.setItem(4, banner);
        terrInv.setItem(13, ItemBuilder.getItem(Material.PAPER, "§a§oℹ Menu du territoire " + territory.getColor() + territory.getName(), Arrays.asList("§2Chef: §a" + chiefName, "§2Officiers: §a" + getTerritoryOfficers(terr).size(),"§2Membres: §a" + getTerritoryMembersUUID(terr).size(), "§2XP:§a " + getTerritoryXP(terr), "§2Argent:§a " + getTerritoryMoney(terr))));
        if (hasTerritory(p) && (isOfficer(p,terr) || isChief(p,terr))) {
            terrInv.setItem(12, ItemBuilder.getItem(Material.END_CRYSTAL, "§b\uD83D\uDC64➕ Inviter des joueurs", false, false, null, null, null));
        }
        if (hasTerritory(p) && (isChief(p,terr))) {
            terrInv.setItem(14, ItemBuilder.getItem(Material.CYAN_STAINED_GLASS, "§3Changer la couleur de votre territoire", false, false, null, null, null));
            terrInv.setItem(22, ItemBuilder.getItem(Material.RED_DYE, "§4❌ Supprimer le territoire", false, false, null, null, null));
        }
        terrInv.setItem(26, ItemBuilder.getItem(Material.BARRIER, "§c❌ Fermer le menu", false, false, null, null, null));
        return terrInv;
    }

    //WORKERS

    public List<String> getWorkerList() {
        return config.getStringList("workerList");
    }

    public void setWorkerList(List<String> workerList) {
        config.set("workerList", workerList);
        saveConfig();
    }

    public void addWorkerToList(UUID workerUUID) {
        List<String> workers = getWorkerList();
        workers.add(workerUUID.toString());
        setWorkerList(workers);
        saveConfig();
    }

    public void removeWorkerFromList(UUID workerUUID) {
        List<String> workers = getWorkerList();
        try {
            workers.remove(workerUUID.toString());
        } catch (Exception e) {
            main.logError("Couldn't remove worker " + workerUUID + " from workerList",e);
        }
        setWorkerList(workers);
        saveConfig();
    }

    public List<String> getTerritoryWorkerList(String territoryName) {
        return config.getStringList("territories." + territoryName + ".workerList");
    }

    public void setTerritoryWorkerList(List<String> workerList, String territoryName) {
        config.set("territories." + territoryName + ".workerList", workerList);
        saveConfig();
    }

    public void addWorkerToTerritoryList(UUID workerUUID, String territoryName) {
        List<String> workers = getTerritoryWorkerList(territoryName);
        workers.add(workerUUID.toString());
        setTerritoryWorkerList(workers,territoryName);
        saveConfig();
    }

    public void removeWorkerFromTerritoryList(UUID workerUUID, String territoryName) {
        List<String> workers = getTerritoryWorkerList(territoryName);
        try {
            workers.remove(workerUUID.toString());
        } catch (Exception e) {
            main.logError("Couldn't remove worker " + workerUUID + " from Territory workerList",e);
        }
        setTerritoryWorkerList(workers,territoryName);
        saveConfig();
    }

    public String formatType(String typeName) {
        String[] words = typeName.toLowerCase().split("_");
        StringBuilder formattedName = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                // Capitalize the first letter and add it to the result
                formattedName.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return formattedName.toString().trim();
    }

    public void openChooseTierInv(Player p, WorkerType type) {

        Inventory chooseTierInv = Bukkit.createInventory(p,27,"§6Choisir le tier du villageois");
        for (int slot = 0; slot < 27; slot++) {
            chooseTierInv.setItem(slot,ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE,""));
        }

        for (int tier = 0; tier <= 5; tier++) {
            int price = type.getPrice();
            int income = type.getIncome();
            ChatColor tierColor= ChatColor.DARK_GRAY;
            switch (tier){
                case 0:
                    chooseTierInv.setItem(10,ItemBuilder.getItem(Material.COAL_BLOCK,tierColor + "Tier 0",Arrays.asList("§aPrix : §6" + price + main.moneySign,"§aRevenus : §6" + income + main.moneySign + "§a/mois")));
                    break;
                case 1:
                    price= (int) (price+(price*0.1));//+10%
                    income= (int) (income+(income*0.1));//+10%
                    tierColor= ChatColor.GRAY;
                    chooseTierInv.setItem(11,ItemBuilder.getItem(Material.IRON_BLOCK,tierColor + "Tier 1",Arrays.asList("§aPrix : §6" + price + main.moneySign,"§aRevenus : §6" + income + main.moneySign + "§a/mois")));
                    break;
                case 2:
                    price= (int) (price+(price*0.25));//+25%
                    income= (int) (income+(income*0.25));//+25%
                    tierColor= ChatColor.YELLOW;
                    chooseTierInv.setItem(12,ItemBuilder.getItem(Material.GOLD_BLOCK,tierColor + "Tier 2",Arrays.asList("§aPrix : §6" + price + main.moneySign,"§aRevenus : §6" + income + main.moneySign + "§a/mois")));
                    break;
                case 3:
                    price= (int) (price+(price*0.45));//+45%
                    income= (int) (income+(income*0.45));//+45%
                    tierColor= ChatColor.GREEN;
                    chooseTierInv.setItem(14,ItemBuilder.getItem(Material.EMERALD_BLOCK,tierColor + "§lTier 3",Arrays.asList("§aPrix : §6" + price + main.moneySign,"§aRevenus : §6" + income + main.moneySign + "§a/mois")));
                    break;
                case 4:
                    price= (int) (price+(price*0.70));//+70%
                    income= (int) (income+(income*0.70));//+70%
                    tierColor= ChatColor.AQUA;
                    chooseTierInv.setItem(15,ItemBuilder.getItem(Material.DIAMOND_BLOCK,tierColor + "§lTier 4",Arrays.asList("§aPrix : §6" + price + main.moneySign,"§aRevenus : §6" + income + main.moneySign + "§a/mois")));
                    break;
                case 5:
                    price= (int) (price+(price*0.95));//+95%
                    income= (int) (income+(income*0.95));//+95%
                    tierColor= ChatColor.BLACK;
                    chooseTierInv.setItem(16,ItemBuilder.getItem(Material.NETHERITE_BLOCK,tierColor + "§lTier 5",Arrays.asList("§aPrix : §6" + price + main.moneySign,"§aRevenus : §6" + income + main.moneySign + "§a/mois")));
                    break;
            }
        }
        chooseTierInv.setItem(13,ItemBuilder.getItem(Material.PAPER,"§aℹ Choisissez le tier de votre villageois " + formatType(type.name())));
        p.openInventory(chooseTierInv);
        return;
    }

    public void buyWorker(Player p, WorkerType type, int tier) {
        String territoryName = getPlayerTerritory(p);
        int price = type.getPrice();
        ChatColor tierColor= ChatColor.DARK_GRAY;
        switch (tier){
            case 0:
                break;
            case 1:
                price= (int) (price+(price*0.1));//+10%
                tierColor= ChatColor.GRAY;
                break;
            case 2:
                price= (int) (price+(price*0.25));//+25%
                tierColor= ChatColor.YELLOW;
                break;
            case 3:
                price= (int) (price+(price*0.45));//+45%
                tierColor= ChatColor.GREEN;
                break;
            case 4:
                price= (int) (price+(price*0.70));//+70%
                tierColor= ChatColor.AQUA;
                break;
            case 5:
                price= (int) (price+(price*0.95));//+95%
                tierColor= ChatColor.BLACK;
                break;
        }
        if (getPlayerTerritory(p) == null) {
            p.sendMessage(main.prefix + "§4Vous devez être dans un territoire pour faire cela!");
            return;
        }
        if (!isChief(p, getPlayerTerritory(p)) && !isOfficer(p, getPlayerTerritory(p))) {
            p.sendMessage(main.prefix + "§4Vous devez être le chef/un officier de votre territoire pour faire cela!");
            return;
        }

        if (getTerritoryMoney(territoryName) < price) {
            p.sendMessage(main.prefix + "§4Il n'y a pas assez d'argent dans la banque de votre territoire !");
            return;
        }

        Villager villager = (Villager) Bukkit.getWorld(p.getWorld().getName()).spawnEntity(p.getLocation(), EntityType.VILLAGER);
        UUID workerUUID = UUID.randomUUID();
        villager.addScoreboardTag("workerUUID=" + workerUUID);
        villager.addScoreboardTag("workerType=" + type.name().toLowerCase());
        villager.addScoreboardTag("workerTerritory=" + getPlayerTerritory(p));
        String workerName = formatType(type.name());
        villager.setProfession(type.getProfession());
        villager.setCustomName(tierColor + workerName);
        villager.setCustomNameVisible(true);
        if(type.getLifespan()==-1){
            villager.setInvulnerable(true);
        }
        ItemStack spawnEgg = new ItemStack(Material.VILLAGER_SPAWN_EGG);
        SpawnEggMeta meta = (SpawnEggMeta) spawnEgg.getItemMeta();
        assert meta != null;
        meta.setSpawnedEntity(Objects.requireNonNull(villager.createSnapshot()));
        meta.setDisplayName("§a" + type.name().substring(0,1).toUpperCase() + type.name().substring(1).toLowerCase());
        villager.remove();
        spawnEgg.setItemMeta(meta);
        config.set("territories." + territoryName + ".villagers." + workerUUID + ".bought", System.currentTimeMillis());
        config.set("territories." + territoryName + ".villagers." + workerUUID + ".daysToLive", type.getLifespan());
        config.set("territories." + territoryName + ".villagers." + workerUUID + ".daysLived", 0);
        config.set("territories." + territoryName + ".villagers." + workerUUID + ".alive", false);
        config.set("territories." + territoryName + ".villagers." + workerUUID + ".hasEverBeenSpawned", false);
        config.set("territories." + territoryName + ".villagers." + workerUUID + ".type", type.name().toLowerCase());
        config.set("territories." + territoryName + ".villagers." + workerUUID + ".name", workerName);
        config.set("territories." + territoryName + ".villagers." + workerUUID + ".villagerUUID", null);
        config.set("territories." + territoryName + ".villagers." + workerUUID + ".tier", tier);
        config.set("territories." + territoryName + ".villagers." + workerUUID + ".spawnEgg", spawnEgg);
        addWorkerToList(workerUUID);
        addWorkerToTerritoryList(workerUUID,territoryName);
        p.getInventory().addItem(spawnEgg);
        removeTerritoryMoney(territoryName, price);
        p.playSound(p.getLocation(),type.getSound(),SoundCategory.NEUTRAL,1,type.getSoundPitch());
        sendAnouncementToTerritory(territoryName,"§6" + p.getName() + "§2 a acheté un villageois §6" + workerName + " §2de " + tierColor + (tier>=3 ? "§lTier " : "tier ") + tier + "§2 !");
        p.sendMessage(main.prefix + "§2Vous avez acheté un employé §a" + type.name().toLowerCase() + "§2 pour §a" + type.getPrice() + main.moneySign + "§2 !");
    }

    public void spawnWorker(Player p, SpawnEggMeta spawnEggMeta, Location spawnLocation, ItemStack it) {
        try {
            String territoryName = getPlayerTerritory(p);
            if (!spawnLocation.getBlock().getType().equals(Material.AIR)){
                spawnLocation.setY(spawnLocation.getY()+1);
            }
            if(spawnEggMeta==null || spawnEggMeta.getSpawnedEntity()==null) {
                return;
            }
            Villager villager = (Villager) spawnEggMeta.getSpawnedEntity().createEntity(spawnLocation);
            UUID workerUUID = null;
            for (String tag : villager.getScoreboardTags()) {
                if (tag.contains("workerUUID=")) {
                    workerUUID = UUID.fromString(tag.replace("workerUUID=",""));
                }
            }
            if(workerUUID == null || !getWorkerList().contains(workerUUID.toString())) {
                return;
            }
            if(config.getBoolean("territories." + territoryName + ".villagers." + workerUUID + ".alive")) {
                villager.remove();
                p.sendMessage(main.prefix + "§4L'employé existe déjà !");
                return;
            }
            WorkerType workerType = null;
            try {
                workerType = WorkerType.valueOf(config.getString("territories." + territoryName + ".villagers." + workerUUID + ".type").toUpperCase());
            } catch (IllegalArgumentException | NullPointerException e) {
                p.sendMessage(main.prefix + "§4Une erreur s'est produite.");
                main.logError("Couldn't get workerType when spawning it",e);
                return;
            }
            int tier = config.getInt("territories." + territoryName + ".villagers." + workerUUID + ".tier");
            config.set("territories." + territoryName + ".villagers." + workerUUID + ".alive", true);
            config.set("territories." + territoryName + ".villagers." + workerUUID + ".hasEverBeenSpawned", true);
            config.set("territories." + territoryName + ".villagers." + workerUUID + ".villagerUUID", villager.getUniqueId().toString());
            saveConfig();
            switch (tier){
                case 0:
                    p.getWorld().playSound(p.getLocation(),Sound.ENTITY_VILLAGER_YES,SoundCategory.NEUTRAL,1,1);
                    Objects.requireNonNull(villager.getLocation().getWorld()).spawnParticle(Particle.HAPPY_VILLAGER,villager.getLocation(),10,2,2,2);
                    villager.setVillagerLevel(1);
                    break;
                case 1:
                    Objects.requireNonNull(villager.getLocation().getWorld()).spawnParticle(Particle.SCRAPE,villager.getLocation(),100,1,1,1,1);
                    p.getWorld().playSound(p.getLocation(),Sound.ENTITY_PLAYER_LEVELUP,SoundCategory.NEUTRAL,1,1);
                    villager.setVillagerLevel(2);
                    break;
                case 2:
                    Objects.requireNonNull(villager.getLocation().getWorld()).spawnParticle(Particle.FIREWORK,villager.getLocation(),100,1,1,1,0.1);
                    p.getWorld().playSound(p.getLocation(),Sound.BLOCK_BEACON_ACTIVATE,SoundCategory.NEUTRAL,1,1);
                    villager.setVillagerLevel(3);
                    break;
                case 3:
                    Objects.requireNonNull(villager.getLocation().getWorld()).spawnParticle(Particle.FLASH,villager.getLocation(),100,1,1,1,0.1);
                    p.getWorld().playSound(p.getLocation(),Sound.BLOCK_ENCHANTMENT_TABLE_USE,SoundCategory.NEUTRAL,1,1);
                    villager.setVillagerLevel(4);
                    break;
                case 4:
                    Objects.requireNonNull(villager.getLocation().getWorld()).spawnParticle(Particle.POOF,villager.getLocation(),100,1,1,1,0.1);
                    p.getWorld().playSound(p.getLocation(),Sound.BLOCK_ANVIL_USE,SoundCategory.NEUTRAL,1,1);
                    p.getWorld().playSound(p.getLocation(),Sound.BLOCK_ANVIL_USE,SoundCategory.NEUTRAL,1,0.1f);
                    villager.setVillagerLevel(5);
                    break;
                case 5:
                    Objects.requireNonNull(villager.getLocation().getWorld()).spawnParticle(Particle.EXPLOSION,villager.getLocation(),100,1,1,1,0.1);
                    p.getWorld().playSound(p.getLocation(),Sound.ITEM_TOTEM_USE,SoundCategory.NEUTRAL,0.5f,0.5f);
                    p.getWorld().playSound(p.getLocation(),Sound.UI_TOAST_CHALLENGE_COMPLETE,SoundCategory.NEUTRAL,1,1);
                    villager.setVillagerLevel(5);
                    villager.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,PotionEffect.INFINITE_DURATION,1,false,true));
                    break;
            }
            addOneAliveWorkerToCount(territoryName,workerType);
            p.getInventory().remove(it);
            p.sendMessage(main.prefix + "§2L'employé a bien été spawn !");
        } catch (Exception e) {
            p.sendMessage(main.prefix + "§4Une erreur s'est produite lors du spawn de cet employé !");
            main.logError("An error encourred while spawning a worker",e);
        }
    }

    public void spawnWorker(Villager villager, Location spawnLocation) {
        try {
            if (!spawnLocation.getBlock().getType().equals(Material.AIR)){
                spawnLocation.setY(spawnLocation.getY()+1);
            }
            if(villager==null) {
                return;
            }
            UUID workerUUID = null;
            for (String tag : villager.getScoreboardTags()) {
                if (tag.contains("workerUUID=")) {
                    workerUUID = UUID.fromString(tag.replace("workerUUID=",""));
                }
            }
            String territoryName = getWorkerTerritory(villager);
            if(workerUUID == null || !getWorkerList().contains(workerUUID.toString()) || territoryName == null || spawnLocation.getWorld() == null) {
                return;
            }
            if(config.getBoolean("territories." + territoryName + ".villagers." + workerUUID + ".alive")) {
                villager.remove();
                return;
            }
            villager.setHealth(20);
//            villager = (Villager) villager.createSnapshot().createEntity(spawnLocation); not needed
            config.set("territories." + territoryName + ".villagers." + workerUUID + ".alive", true);
            config.set("territories." + territoryName + ".villagers." + workerUUID + ".hasEverBeenSpawned", true);
            config.set("territories." + territoryName + ".villagers." + workerUUID + ".villagerUUID", villager.getUniqueId().toString());
            saveConfig();

            spawnLocation.getWorld().spawnParticle(Particle.ASH,villager.getLocation(),100,2,2,2);
            spawnLocation.getWorld().playSound(spawnLocation,Sound.BLOCK_FIRE_EXTINGUISH,1,1);
        } catch (Exception e) {
            main.logError("An error encourred while spawning a worker",e);
        }
    }

    public String getWorkerTerritory(Villager villager) {
        String workerTerritory = null;
        for (String tag : villager.getScoreboardTags()) {
            if (tag.contains("workerTerritory=")) {
                workerTerritory = tag.replace("workerTerritory=","");
            }
        }
        return workerTerritory;
    }

    public void runWorkerCheckup(){
        Bukkit.getConsoleSender().sendMessage(main.prefix + "§eRunning daily worker checkup...");
        for (String territoryName : getTerritoriesList()) {
            int territorySumMoney=0;
            for (String workerUUID : getTerritoryWorkerList(territoryName)){
                String pathToWorker = "territories." + territoryName + ".villagers." + workerUUID;
                boolean workerAlive = config.getBoolean(pathToWorker + ".alive");
                int tier = config.getInt(pathToWorker + ".tier");
                WorkerType workerType = WorkerType.valueOf(Objects.requireNonNull(config.getString(pathToWorker + ".type")).toUpperCase());
                if(!workerAlive){
                    continue;
                }
                if(!(Bukkit.getEntity(UUID.fromString(Objects.requireNonNull(config.getString(pathToWorker + ".villagerUUID")))) instanceof Villager worker)){
                    continue;
                }
                int daysToLive = config.getInt(pathToWorker + ".daysToLive");
                if (daysToLive!=-1){
                    daysToLive=daysToLive-1;
                    config.set(pathToWorker + ".daysToLive",daysToLive);
                }
                int daysLived = config.getInt(pathToWorker + ".daysLived")+1;
                config.set(pathToWorker + ".daysLived",daysLived);
                if(Math.floor((double) daysLived /30)== (double) daysLived /30) {
                    int income = workerType.getIncome();
                    switch (tier){
                        case 0:
                            break;
                        case 1:
                            income= (int) (income+(income*0.1));//+10%
                            break;
                        case 2:
                            income= (int) (income+(income*0.25));//+25%
                            break;
                        case 3:
                            income= (int) (income+(income*0.45));//+45%
                            break;
                        case 4:
                            income= (int) (income+(income*0.70));//+70%
                            break;
                        case 5:
                            income= (int) (income+(income*0.95));//+95%
                            break;
                    }
                    addTerritoryMoney(territoryName,income);
                    territorySumMoney=income;
                }
//                addTerritoryMoney(territoryName,workerType.getIncome()); // FOR TESTING
                if(daysToLive == 0){
                    sendAnouncementToTerritory(territoryName,"§eUn de vos villageois §c" + formatType(workerType.toString()) + "§e est mort de viellesse !");
                    worker.remove();
                    removeWorkerFromTerritoryList(UUID.fromString(workerUUID),territoryName);
                    removeWorkerFromList(UUID.fromString(workerUUID));
                    removeOneAliveWorkerFromCount(territoryName,workerType);
                    config.set(pathToWorker,null);
                }
                saveConfig();
            }
            if(territorySumMoney!=0){
                sendAnouncementToTerritory(territoryName,"§2Vous avez gagné §6" + territorySumMoney + main.moneySign + " grâce à vos villageois ce mois-ci");
            }
        }
        programNextWorkerCheckup();
        Bukkit.getConsoleSender().sendMessage(main.prefix + "§aDone running daily worker checkup !");
    }

    public void programNextWorkerCheckup(){
        // Cancel any existing scheduled task before scheduling a new one
        if (workerCheckupTask != null && !workerCheckupTask.isCancelled()) {
            Bukkit.getLogger().warning("Duplicate WorkerCheckup detected! Cancelling previous one.");
            workerCheckupTask.cancel();
        }

        // Calculate ticks until midnight
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long offset = cal.getTimeInMillis() - now;
        long ticks = offset / 50L;

        try {
            // Schedule the task and store the reference
            workerCheckupTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                runWorkerCheckup();
                workerCheckupTask = null; // Clear reference after execution
            }, ticks);
        } catch (Exception e) {
            main.logError("§4Couldn't schedule next WorkerCheckup",e);
        }
    }

    public void cancelWorkerCheckup() {
        if (workerCheckupTask != null && !workerCheckupTask.isCancelled()) {
            workerCheckupTask.cancel();
            workerCheckupTask = null;
        }
    }

    public String getWorkerTerritory(String workerUUID) {
        try {
            if(!getWorkerList().contains(workerUUID)) {
                return null;
            }
            try {
                for(String key : config.getConfigurationSection("territories").getKeys(false)) {
                    List<String> territoryWorkerList = getTerritoryWorkerList(key);
                    try {
                        for (String worker : territoryWorkerList) {
                            if(worker.equals(workerUUID)){
                                return key;
                            }
                        }
                    } catch (Exception e) {
                        main.logError("Couldn't check worker territory",e);
                    }
                }
                return null;
            } catch (NullPointerException e) {
                main.logError("Couldn't check worker territory",e);
                return null;
            }
        } catch (Exception e) {
            main.logError("Couldn't check worker territory",e);
            return null;

        }
    }

    public void showBuyWorkerInv(Player p){
        Inventory inv = Bukkit.createInventory(p,45,"§6Acheter un villegeois");
        for (int slot = 0; slot < 45; slot++) {
            inv.setItem(slot,ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE,""));
        }

        int[] slots = {11,12,13,14,15,19,20,21,22,23,24,25,29,30,31,32,33};
        for (int slot : slots){
            inv.setItem(slot,null);
        }

        for (WorkerType workerType : WorkerType.values()){
            Material item = workerType.getItem();
            ChatColor typeColor = workerType.getColor();
            String typeName = formatType(workerType.toString());
            String price = "§aPrix : " + typeColor + workerType.getPrice() + main.moneySign;
            String income = "§aRevenus : §6" + workerType.getIncome() + main.moneySign + "§a/mois";
            String lifespan = "§aDurée de vie : " + (workerType.getLifespan()==-1 ? "§b§oInvincible" : "§6" + workerType.getLifespan()/30 + "§a mois");
            inv.addItem(ItemBuilder.getItem(item, typeColor+typeName,false,false,price,income,lifespan));
        }
        inv.setItem(44,ItemBuilder.getItem(Material.BARRIER,"§c❌ Fermer le menu"));

        p.openInventory(inv);
    }

    public void setAliveWorkerCount(String territoryName, int workerCount, WorkerType workerType){
        config.set("territories." + territoryName + ".aliveWorkerCount." + workerType.name().toLowerCase(),workerCount);
        saveConfig();
    }

    public void addOneAliveWorkerToCount(String territoryName, WorkerType workerType) {
        int currentWorkerCount = getAliveWorkerCount(territoryName,workerType);
        setAliveWorkerCount(territoryName, currentWorkerCount+1, workerType);
    }

    public void removeOneAliveWorkerFromCount(String territoryName, WorkerType workerType) {
        int currentWorkerCount = getAliveWorkerCount(territoryName,workerType);
        setAliveWorkerCount(territoryName, currentWorkerCount-1, workerType);
    }

    public int getAliveWorkerCount(String territoryName, WorkerType workerType){
        return (config.get("territories." + territoryName + ".aliveWorkerCount." + workerType.name().toLowerCase()) == null ||  config.getInt("territories." + territoryName + ".aliveWorkerCount." + workerType.name().toLowerCase()) < 0) ? 0 : config.getInt("territories." + territoryName + ".aliveWorkerCount." + workerType.name().toLowerCase());
    }
}