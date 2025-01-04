package fr.mattmunich.civilisation_creatif.helpers;

import fr.mattmunich.civilisation_creatif.Main;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TerritoryData {
    private final Plugin plugin;

    private Main main;

    public TerritoryData(Plugin plugin, Main main) {
        this.plugin = plugin;
        this.main = main;
    }

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
            config.set("territories." + territoryName + ".created", System.currentTimeMillis());
            config.set("territories." + territoryName + ".color", territoryColor.toString());
            config.set("territories." + territoryName + ".xp", 0);

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
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Couldn't create territory " + territoryName + " from list because of " + e);
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
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Couldn't delete territory " + territoryName + " from list because of " + e);
        }
//        } else {
//            sender.sendMessage(main.prefix + "§4Impossible de supprimer le territoire, §cterritoire non trouvé.");
//        }
    }

    public void leaveTerritory(Player sender) {
        Team sTeam = getTerritoryTeamOfPlayer(sender);
        if(Objects.equals(getTerritoryChiefUUID(sTeam.getName()), sender.getUniqueId().toString())) {
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
            for (String entry : territory.getEntries()){
                UUID entryUUID = Utility.getUUIDFromName(entry);
                membersUUID.add(entryUUID.toString());
            }
            config.set("territories." + territoryName + ".membersUUID", membersUUID);
            saveConfig();
            return true;
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Update territory members in config from team for " + territoryName + " from list because of " + e);
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
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Couldn't remove territory " + territoryName + " from list because of " + e);
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
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Couldn't invite player to territory because of §r" + e + Arrays.toString(e.getStackTrace()).replace(",", ",\n"));
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
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Couldn't remove invite player to territory because of §r" + e + Arrays.toString(e.getStackTrace()).replace(",", ",\n"));
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
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Couldn't remove invite player to territory because of §r" + e + Arrays.toString(e.getStackTrace()).replace(",", ",\n"));
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
            terrClaims.add(chunk);
            globalClaims.add(chunk);
            config.set("territories." + territory.getName() + ".claims", terrClaims);
            config.set("claims", globalClaims);
            saveConfig();
            sender.sendMessage(main.prefix + "§aVous avez §2claim §ale chunk §e" + chunk.keySet().stream().findFirst().toString().replace("Optional[","").replace("]","") + "§a,§e " + chunk.values().stream().findFirst().toString().replace("Optional[","").replace("]","") + " §apour §e300¢ §a!");
            return;
        } catch (Exception e) {
            sender.sendMessage(main.prefix + "§4Une erreur s'est produite lors du claim du chunk !");
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Couldn't claim chunk for territory " + territoryName +  "§r" + e + Arrays.toString(e.getStackTrace()).replace(",", ",\n"));
            return;
        }
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
                return;
            } else {
                sender.sendMessage(main.prefix + "§cVous n'avez pas claim le chunk §e" + chunk.keySet().stream().findFirst().toString().replace("Optional[","").replace("]","") + "§a,§e " + chunk.values().stream().findFirst().toString().replace("Optional[","").replace("]",""));
            }

        } catch (Exception e) {
            sender.sendMessage(main.prefix + "§4Une erreur s'est produite lors du UNclaim du chunk !");
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Couldn't UNclaim chunk for territory " + territoryName +  "§r" + e + Arrays.toString(e.getStackTrace()).replace(",", ",\n"));
            return;

        }
    }

    public List<Map<?, ?>> getTerritoryChunks(String territoryName) {
        try {
            Team territory = getTerritoryTeam(territoryName);
            try {
                return config.getMapList("territories." + territory.getName() + ".claims");
            } catch (ClassCastException e) {
                Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Couldn't cast List<Map<?, ?>> for chunks of territory " + territoryName +  " : §r" + e + Arrays.toString(e.getStackTrace()).replace(",", ",\n"));
                return null;
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Couldn't get claimed chunks for territory " + territoryName +  " : §r" + e + Arrays.toString(e.getStackTrace()).replace(",", ",\n"));
            return null;

        }
    }

    public boolean chunkClaimed(Map<Integer,Integer> chunk) {
        try {
            List<Map<?,?>> globalClaims = config.getMapList("claims");
            return globalClaims.contains(chunk);
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Couldn't check if chunk was claimed §r" + e + Arrays.toString(e.getStackTrace()).replace(",", ",\n"));
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
//                    Bukkit.getConsoleSender().sendMessage("[DEBUG] Checking territory " + key);
                    List<Map<?, ?>> territoryClaims = getTerritoryChunks(key);
//                    Bukkit.getConsoleSender().sendMessage("[DEBUG] Chunks of territory are " + territoryClaims);
                    try {
                        for (Map<?, ?> territoryClaim : territoryClaims) {
//                            Bukkit.getConsoleSender().sendMessage("[DEBUG] Is territoryClaim : " + territoryClaim + " same as asked :" + chunk);
                            if(territoryClaim.equals(chunk)){
//                                Bukkit.getConsoleSender().sendMessage("[DEBUG] Found corresponding chunk ; asked : " +chunk + " ; found : " + territoryClaim);
                                return key;
                            }
                        }
                    } catch (Exception e) {
                        Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Couldn't check chunk owner §r" + e + Arrays.toString(e.getStackTrace()).replace(",", ",\n"));
                    }
                }
                return null;
            } catch (NullPointerException e) {
                Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Couldn't check chunk owner §r" + e + Arrays.toString(e.getStackTrace()).replace(",", ",\n"));
                return null;
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(main.prefix + "§4Couldn't check chunk owner §r" + e + Arrays.toString(e.getStackTrace()).replace(",", ",\n"));
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
}
