package fr.mattmunich.civilisation_creatif.territories;

import fr.mattmunich.civilisation_creatif.Main;
import fr.mattmunich.civilisation_creatif.helpers.*;
import fr.mattmunich.civilisation_creatif.helpers.Utility;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class TerritoryData {
    private final Plugin plugin;
    private final Main main;
    private final Inventories territoryInventories = new Inventories(this);
    private final WorkersData workersData = new WorkersData(this);
    private final ClaimsData claimsData = new ClaimsData(this);

    public TerritoryData(Plugin plugin, Main main) {
        this.plugin = plugin;
        this.main = main;
    }

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
                main.logError("Couldn't create territory data config file",ioe);
            }
        }

        config = YamlConfiguration.loadConfiguration(file);

    }

    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException ioe) {
            main.logError("Couldn't save territory data config file",ioe);
        }
    }

    public void createTerritory(Player chief, String territoryName, ChatColor territoryColor) {
        try {
            if (getTerritoriesList().contains(territoryName)) {
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

            PlayerData data = new PlayerData(chief);
            data.setTerritory(territoryName);

            boolean check = updateMemberInConfig(territoryName);
            if (!check) {
                Bukkit.getConsoleSender().sendMessage(main.prefix + "§c§oUne erreur NON fatale s'est produite lors de la création du territoire " + territoryName);
            }
            saveConfig();

            if (data.getRank().getId() < 4) {
                data.setRank(Grades.CHEF);
            }

            SidebarManager.updateScoreboard(chief);

            chief.sendMessage(main.prefix + "§2Le territoire §6" + territory.getColor() + territory.getName() + "§2 a été créé avec succès !");
            Bukkit.broadcastMessage(main.prefix + "§6" + chief.getName() + "§a a créé le territoire " + territory.getColor() + territory.getName() + " §a!");
        } catch (Exception e) {
            chief.sendMessage(main.prefix + "§4Impossible de créer le territoire, §cveuillez signaler cela à un membre du staff.");
            main.logError("Couldn't create territory " + territoryName, e);
        }
    }

    public void deleteTerritory(Player sender, String territoryName) {
        //if(getTerritoriesList().contains(territoryName)) {
        try {
            config.set("territories." + territoryName, null);
            for (String entry : getTerritoryTeam(territoryName).getEntries()) {
                Player p = Bukkit.getPlayer(entry);
                if (p != null) {
                    PlayerData pdata = new PlayerData(p);
                    pdata.setTerritory(null);
                    if (pdata.getRank().getId() < 4) {
                        pdata.setRank(Grades.VAGABOND);
                    }
                    if (p.isOnline()) {
                        SidebarManager.updateScoreboard(p);
                        if (pdata.getRank() == null) {
                            p.kickPlayer("§cVotre territoire a été supprimé \n§e et une erreur s'est produite lors de l'obtention de votre grade!");
                            return;
                        }
                        p.setDisplayName(main.hex(pdata.getRank().getPrefix()) + p.getName() + pdata.getRank().getSuffix());
                        if (!p.getName().equals(sender.getName())) {
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
            main.logError("Couldn't remove territory " + territoryName + " from list", e);
        }
//        } else {
//            sender.sendMessage(main.prefix + "§4Impossible de supprimer le territoire, §cterritoire non trouvé.");
//        }
    }

    public void leaveTerritory(Player sender) {
        Team sTeam = getTerritoryTeamOfPlayer(sender);
        if (isChief(sender, sTeam.getName())) {
            sender.sendMessage(main.prefix + "§cVous êtes le chef de ce territoire, §esi vous souhaitez le quitter, veuillez\n" +
                    "§4- §4§lSupprimer le territoire\n" +
                    "§r§eOU\n" +
                    "§a- §a§lDonner les permissions de chef à un autre joueur");
            return;
        }
        if (sTeam.getEntries().size() == 1) {
            sender.sendMessage(main.prefix + "§cVous êtes le seul membre de ce territoire, §eveuillez donc le supprimer pour le quitter !");
            return;
        }

        sTeam.removeEntry(sender.getName());
        List<String> membersUUID = getTerritoryMembersUUID(sTeam.getName());
        try {
            membersUUID.remove(sender.getUniqueId().toString());
            PlayerData pdata = new PlayerData(sender.getUniqueId());
            pdata.setTerritory(null);
            if (pdata.getRank().getId() < 4) {
                pdata.setRank(Grades.VAGABOND);
            }
            SidebarManager.updateScoreboard(sender);
            sendAnouncementToTerritory(sTeam.getName(), "§c" + sender.getName() + "§4 a quitté le territoire.");
        } catch (Exception ignored) {
        }
        config.set("territories." + sTeam.getName() + ".membersUUID", membersUUID);
        saveConfig();
        sender.sendMessage(main.prefix + "§eVous avez quitté votre territoire !");
    }

    public void joinTerritory(OfflinePlayer p, String territoryName) {
        if(p.getName()==null) { return; }
        Team territory = getTerritoryTeam(territoryName);
        territory.addEntry(p.getName());
        List<String> membersUUID = getTerritoryMembersUUID(territoryName);
        try {
            PlayerData pdata = new PlayerData(p);
            pdata.setTerritory(territoryName);
            if (pdata.getRank().getId() < 4) {
                pdata.setRank(Grades.MEMBRE);
            }
            if(p.getPlayer()!=null) { SidebarManager.updateScoreboard(p.getPlayer()); }
            membersUUID.add(p.getUniqueId().toString());
            sendAnouncementToTerritory(territoryName, "§a" + p.getName() + "§2 a rejoin le territoire !");
        } catch (Exception ignored) {
        }
        config.set("territories." + territory.getName() + ".membersUUID", membersUUID);
        saveConfig();
        if(p.getPlayer()!=null) { p.getPlayer().sendMessage(main.prefix + "§2Vous avez rejoint le territoire " + territory.getColor() + territory.getName() + "§2 !"); }
    }

    public List<String> getTerritoryMembersUUID(String territoryName) {
        return config.getStringList("territories." + territoryName + ".membersUUID");
    }

    public String getTerritoryChiefUUID(String territoryName) {
        return Objects.requireNonNull(config.getString("territories." + territoryName + ".chief.UUID"));
    }

    public List<String> getTerritoryOfficers(String territoryName) {
        if (config.get("territories." + territoryName + ".officers") == null) {
            return new ArrayList<>();
        } else {
            return config.getStringList("territories." + territoryName + ".officers");
        }
    }

    public void makeOfficer(OfflinePlayer target, Player sender) {
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(main.prefix + "§4Le joueur ne s'est jamais connecté !");
            return;
        }
        if (getPlayerTerritory(sender) == null) {
            sender.sendMessage(main.prefix + "§4Vous devez être dans un territoire pour faire cela !");
            return;
        }
        if (!isChief(sender, getPlayerTerritory(sender)) && !isOfficer(sender, getPlayerTerritory(sender))) {
            sender.sendMessage(main.prefix + "§4Vous devez être le chef/un officer de votre territoire pour faire cela !");
            return;
        }
        if (!getPlayerTerritory(target).equalsIgnoreCase(getPlayerTerritory(sender))) {
            sender.sendMessage(main.prefix + "§4Vous devez être devez être dans le même territoire que la cible pour faire cela !");
            return;
        }
        List<String> officers = getTerritoryOfficers(getPlayerTerritory(sender));
        if (officers.contains(target.getUniqueId().toString())) {
            sender.sendMessage(main.prefix + "§4La cible est déjà un officier dans votre territoire !");
            return;
        }
        if (target == sender) {
            sender.sendMessage(main.prefix + "§4Vous êtes déjà chef de votre territoire !");
            return;
        }
        officers.add(target.getUniqueId().toString());
        config.set("territories." + getPlayerTerritory(sender) + ".officers", officers);
        saveConfig();
        if (target.isOnline()) {
            SidebarManager.updateScoreboard(target.getPlayer());
        }
        sender.sendMessage(main.prefix + "§2Le joueur §a" + target.getName() + "§2 a été ajouté aux officiers de votre territoire !");
        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(main.prefix + "§2Vous êtes désormais officier dans le territoire §a" + getPlayerTerritory(sender) + "§2 !");
        }
    }

    public void ADMIN_leaveTerritory(OfflinePlayer sender) {
        if(sender.getPlayer() != null) {
            Team sTeam = getTerritoryTeamOfPlayer(sender.getPlayer());
            sTeam.removeEntry(sender.getPlayer().getName());
        }
        List<String> membersUUID = getTerritoryMembersUUID(getPlayerTerritory(sender));
        try {
            membersUUID.remove(sender.getUniqueId().toString());
            PlayerData pdata = new PlayerData(sender);
            pdata.setTerritory(null);
            if (pdata.getRank().getId() < 4) {
                pdata.setRank(Grades.VAGABOND);
            }
            if(sender.getPlayer() != null) { SidebarManager.updateScoreboard(sender.getPlayer()); }
        } catch (Exception ignored) {}
        config.set("territories." + getPlayerTerritory(sender) + ".membersUUID", membersUUID);
        saveConfig();
        if(sender.getPlayer() != null) { sender.getPlayer().sendMessage(main.prefix + "§eVous avez quitté votre territoire !"); }
    }

    public void ADMIN_makeOfficer(OfflinePlayer target, Player sender) {
        if (target.getName()==null || !target.hasPlayedBefore() || getPlayerTerritory(target)==null) {
            sender.sendMessage(main.prefix + "§4Joueur non trouvé.");
            return;
        }
        String territoryName=getPlayerTerritory(target);
        List<String> officers = getTerritoryOfficers(getPlayerTerritory(sender));
        if (officers.contains(target.getUniqueId().toString())) {
            sender.sendMessage(main.prefix + "§4La cible est déjà un officier dans le territoire !");
            return;
        }
        if (isChief(target,territoryName)) {
            sender.sendMessage(main.prefix + "§4La cible est déjà chef(fe) de son territoire !");
            return;
        }
        officers.add(target.getUniqueId().toString());
        config.set("territories." + getPlayerTerritory(sender) + ".officers", officers);
        saveConfig();
        if (target.isOnline()) {
            SidebarManager.updateScoreboard(target.getPlayer());
        }
        sender.sendMessage(main.prefix + "§2Le joueur §6" + target.getName() + "§2 a été ajouté aux officiers du territoire §6" + territoryName + "!");
        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(main.prefix + "§2Vous êtes désormais officier dans le territoire §6" + getPlayerTerritory(sender) + "§2 !");
        }
    }

    public void ADMIN_removeOfficer(OfflinePlayer target, Player sender) {
        if (target.getName()==null || !target.hasPlayedBefore() || getPlayerTerritory(target)==null) {
            sender.sendMessage(main.prefix + "§4Joueur non trouvé.");
            return;
        }
        String territoryName=getPlayerTerritory(target);
        List<String> officers = getTerritoryOfficers(getPlayerTerritory(sender));
        if (!officers.contains(target.getUniqueId().toString())) {
            sender.sendMessage(main.prefix + "§4La cible n'est pas un officier dans le territoire !");
            return;
        }
        if (isChief(target,territoryName)) {
            sender.sendMessage(main.prefix + "§4La cible est chef(fe) de son territoire !");
            return;
        }
        officers.remove(target.getUniqueId().toString());
        config.set("territories." + getPlayerTerritory(sender) + ".officers", officers);
        saveConfig();
        if (target.isOnline()) {
            SidebarManager.updateScoreboard(target.getPlayer());
        }
        sender.sendMessage(main.prefix + "§2Le joueur §6" + target.getName() + "§2 a été §cretiré§2 aux officiers du territoire §6" + territoryName + " §2!");
        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(main.prefix + "§cVous n'êtes désormais plus officier dans le territoire §6" + getPlayerTerritory(sender) + "§c !");
        }
    }

    public void removeOfficer(OfflinePlayer target, Player sender) {
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(main.prefix + "§4Le joueur ne s'est jamais connecté !");
            return;
        }
        if (getPlayerTerritory(sender) == null) {
            sender.sendMessage(main.prefix + "§4Vous devez être dans un territoire pour faire cela !");
            return;
        }
        if (!isChief(sender, getPlayerTerritory(sender)) || !isOfficer(sender, getPlayerTerritory(sender))) {
            sender.sendMessage(main.prefix + "§4Vous devez être le chef/un officer de votre territoire pour faire cela !");
            return;
        }
        if (!getPlayerTerritory(target).equalsIgnoreCase(getPlayerTerritory(sender))) {
            sender.sendMessage(main.prefix + "§4Vous devez être devez être dans le même territoire que la cible pour faire cela !");
            return;
        }
        List<String> officers = getTerritoryOfficers(getPlayerTerritory(sender));
        if (!officers.contains(target.getUniqueId().toString())) {
            sender.sendMessage(main.prefix + "§4La cible n'est pas un officier dans votre territoire !");
            return;
        }
        officers.remove(target.getUniqueId().toString());
        config.set("territories." + getPlayerTerritory(sender) + ".officers", officers);
        saveConfig();
        if (target.isOnline()) {
            SidebarManager.updateScoreboard(target.getPlayer());
        }
    }

    public boolean isNotInTerritory(OfflinePlayer player, String territoryName) {
        return !getPlayerTerritory(player).equals(territoryName);
    }

    public boolean hasTerritory(OfflinePlayer player) {
        return getPlayerTerritory(player) != null;
    }

    public boolean isOfficer(OfflinePlayer player, String territoryName) {
        if (!hasTerritory(player) || isNotInTerritory(player, territoryName)) {
            return false;
        }
        List<String> officers = getTerritoryOfficers(getPlayerTerritory(player));
        return officers.contains(player.getUniqueId().toString());
    }

    public boolean isChief(OfflinePlayer player, String territoryName) {
        if (!hasTerritory(player) || isNotInTerritory(player, territoryName)) {
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
        for (String memberUUID : membersUUID) {
            try {
                String memberName = fr.mattmunich.civilisation_creatif.helpers.Utility.getNameFromUUID(UUID.fromString(memberUUID));
                territory.addEntry(memberName);
            } catch (Exception ignored) {
            }
        }
    }

    public void setPlayerTerritoryTeam(Player player) {
        try {
            PlayerData data = new PlayerData(player.getUniqueId());
            Team territory = getTerritoryTeam(data.getTerritory());
            if (territory != null) {
                try {
                    territory.addEntry(player.getName());
                } catch (NullPointerException ignored) {
                }
            }
        } catch (Exception ignored) {
        }
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
            if (scMan == null) {
                return null;
            }
            Scoreboard sc = scMan.getMainScoreboard();
            if (it.getItemMeta() == null) {
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
            main.logError("Couldn't remove territory " + territoryName + " from list", e);
        }
        setTerritoriesList(territories);
        saveConfig();
    }

    public List<Team> getTerritoriesTeam() {
        List<String> territoriesNames = config.getStringList("list");
        List<Team> territories = new ArrayList<>();
        for (String territoryName : territoriesNames) {
            Team territory = getTerritoryTeam(territoryName);
            territories.add(territory);
        }
        return territories;
    }

    public void invitePlayer(Player sender, OfflinePlayer target) {
        if (target == null) {
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
                if (tdata.getInvitesToTerritory().stream().anyMatch(map -> map.containsKey(territory.getName()))) {
                    sender.sendMessage(main.prefix + "§cLe joueur est déjà invité à rejoindre votre territoire !");
                    return;
                }
            } catch (NullPointerException ignored) {
            }
            tdata.inviteToTerritory(territory.getName(), sender);
            if (target.getPlayer() != null) {
                target.getPlayer().sendMessage("\n\n" + main.prefix + "§2Vous avez été invité à rejoindre le territoire " + territory.getColor() + territory.getName() + "§2 !\n" + main.prefix + "§8§o/territoire §r§7pour rejoindre\n\n");
                target.getPlayer().playSound(target.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            }
            sender.sendMessage(main.prefix + "§2Le joueur a été invité à rejoindre votre territoire !");
        } catch (Exception e) {
            sender.sendMessage(main.prefix + "§4Une erreur s'est produite lors de l'invitation du joueur !");
            main.logError("Couldn't invite player to territory", e);

        }

    }

    public void removeInvite(String territoryName, OfflinePlayer target) {
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
            main.logError("Couldn't remove invite to territory", e);
        }
    }

    public void removeInviteMsgPlayer(Player sender, String territoryName, Player target) {
        if (target == null) {
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
            main.logError("Couldn't remove invite to territory", e);
        }
    }

    public void claimChunk(Player sender, String territoryName, Chunk chunk) {
        claimsData.claimChunk(sender, territoryName, chunk);
    }

    public int getChunkPrice(String territoryName) {
        return claimsData.getChunkPrice(territoryName);
    }

    public void setClaimedChunkCount(String territoryName, int claimedChunkCount) {
        claimsData.setClaimedChunkCount(territoryName, claimedChunkCount);
    }

    public void addOneClaimedChunkToCount(String territoryName) {
        claimsData.addOneClaimedChunkToCount(territoryName);
    }

    public void removeOneClaimedChunkFromCount(String territoryName) {
        claimsData.removeOneClaimedChunkFromCount(territoryName);
    }

    public int getClaimdChunkCount(String territoryName) {
        return claimsData.getClaimdChunkCount(territoryName);
    }

    public void unclaimChunk(Player sender, String territoryName, Chunk chunk) {
        claimsData.unclaimChunk(sender, territoryName, chunk);
    }

    public List<Chunk> getTerritoryChunks(String territoryName) {
        return claimsData.getTerritoryChunks(territoryName);
    }

    public boolean chunkClaimed(Chunk chunk) {
        return claimsData.chunkClaimed(chunk);
    }

    public String getChunkOwner(Chunk chunk) {
        return claimsData.getChunkOwner(chunk);
    }

    public Map<Integer, Integer> getChunkMap(Chunk chunk) {
        return claimsData.getChunkMap(chunk);
    }

    public Chunk getChunkFromMap(Map<Integer, Integer> chunkMap, World world) {
        return claimsData.getChunkFromMap(chunkMap, world);
    }

    public void showChunkBorder(Chunk chunk, ChatColor chatColor, Player player) {


        claimsData.showChunkBorder(chunk, chatColor, player);
    }

    public void sendAnouncementToTerritory(String territoryName, String message) {
        Team terr = getTerritoryTeam(territoryName);
        for (String eName : terr.getEntries()) {
            if (Bukkit.getPlayer(eName) != null) {
                Player p = Bukkit.getPlayer(eName);
                assert p != null;
                p.sendMessage(main.prefix + " §6-> §" + terr.getColor() + terr.getName() + " §8§l>>> §6" + message);
            }
        }
    }

    public void setTerritoryBanner(String territoryName, ItemStack banner) {
        config.set("territories." + territoryName + ".banner", banner);
        saveConfig();
    }

    public ItemStack getTerritoryBanner(String territoryName) {
        if (config.get("territories." + territoryName + ".banner") == null) {
            return new ItemStack(Material.WHITE_BANNER);
        } else {
            return config.getItemStack("territories." + territoryName + ".banner");
        }
    }

    public int getTerritoryXP(String territoryName) {
        return config.getInt("territories." + territoryName + ".xp");
    }

    public void setTerritoryXP(String territoryName, int xp) {
        config.set("territories." + territoryName + ".xp", xp);
        saveConfig();
    }

    public void addTerritoryXP(String territoryName, int xp) {
        setTerritoryXP(territoryName,getTerritoryXP(territoryName) + xp);
    }

    public void removeTerritoryXP(String territoryName, int xp) {
        setTerritoryXP(territoryName,getTerritoryXP(territoryName) - xp);
    }

    public void resetTerritoryXP(String territoryName) {
        setTerritoryXP(territoryName,0);
    }

    public int getTerritoryMoney(String territoryName) {
        return config.getInt("territories." + territoryName + ".money");
    }

    public void setTerritoryMoney(String territoryName, int money) {
        config.set("territories." + territoryName + ".money", money);
        saveConfig();
    }

    public void addTerritoryMoney(String territoryName, int money) {
        config.set("territories." + territoryName + ".money", config.getInt("territories." + territoryName + ".money") + money);
        saveConfig();
    }

    public void removeTerritoryMoney(String territoryName, int money) {
        config.set("territories." + territoryName + ".money", config.getInt("territories." + territoryName + ".money") - money);
        saveConfig();
    }

    public void resetTerritoryMoney(String territoryName) {
        config.set("territories." + territoryName + ".money", 0);
        saveConfig();
    }

    public void setTerritoryDescription(String territoryName, String description) {
        config.set("territories." + territoryName + ".description", description);
        saveConfig();
    }

    public String getTerritoryDescription(String territoryName) {
        return config.getString("territories." + territoryName + ".description");
    }

    public void renameTerritory(String oldTerritoryName, String newTerritoryName) {
        ConfigurationSection territoryData = config.getConfigurationSection("territories." + oldTerritoryName);
        if (territoryData == null) {
            throw new NullPointerException("Couldn't rename territory " + oldTerritoryName + " because it's data was null.");
        }

        for (String memberUUIDasString : getTerritoryMembersUUID(oldTerritoryName)) {
            UUID uuid = UUID.fromString(memberUUIDasString);
            try {
                PlayerData playerData = new PlayerData(uuid);
                playerData.setTerritory(newTerritoryName);
            } catch (Exception e) {
                main.logError("Couldn't set player with UUID " + uuid + " territory when renaming territory", e);
            }
        }

        config.set("territories." + oldTerritoryName, null);
        config.createSection("territories." + newTerritoryName, territoryData.getValues(true));
        saveConfig();
    }

    public int extractInventoryPageNumber(String title) {
        return territoryInventories.extractInventoryPageNumber(title);
    }

    public Inventory getTerritoryListInventory(Player p, int page) {
        return territoryInventories.getTerritoryListInventory(p, page);
    }

    public @Nullable List<String> splitDescription(String description, int maxLength) {
        return territoryInventories.splitDescription(description, maxLength);
    }

    public Inventory getTerrInv(Player p, Team territory) {
        return territoryInventories.getTerrInv(p, territory);
    }

    public void showTerritoryMembersInventory(Player p, String territoryName, int page) {
        territoryInventories.showTerritoryMembersInventory(p, territoryName, page);
    }

    //WORKERS

    public Inventory getTerritoryWorkersInventory(Player p, String territoryName, int page) {
        return territoryInventories.getTerritoryWorkersInventory(p, territoryName, page);
    }

    public List<String> getWorkerList() {
        return workersData.getWorkerList();
    }

    public List<String> getTerritoryWorkerList(String territoryName) {
        return workersData.getTerritoryWorkerList(territoryName);
    }

    public String formatType(String typeName) {

        return workersData.formatType(typeName);
    }

    public void openChooseTierInv(Player p, WorkerType type) {

        workersData.openChooseTierInv(p, type);
    }

    public void buyWorker(Player p, WorkerType type, int tier) {

        workersData.buyWorker(p, type, tier);
    }

    public void spawnWorker(Player p, SpawnEggMeta spawnEggMeta, Location spawnLocation, ItemStack it) {
        workersData.spawnWorker(p, spawnEggMeta, spawnLocation, it);
    }

    public void spawnWorker(Villager villager, Location spawnLocation) {
        workersData.spawnWorker(villager, spawnLocation);
    }

    public String getWorkerTerritory(Villager villager) {
        return workersData.getWorkerTerritory(villager);
    }

    public void runWorkerCheckup() {
        workersData.runWorkerCheckup();
    }

    public void programNextWorkerCheckup() {
        workersData.programNextWorkerCheckup();
    }

    public void showBuyWorkerInv(Player p) {

        workersData.showBuyWorkerInv(p);
    }

    public void removeAliveWorkerFromCount(String territoryName, WorkerType workerType, int count) {
        workersData.removeAliveWorkerFromCount(territoryName, workerType, count);
    }

    public void removeOneAliveWorkerFromCount(String territoryName, WorkerType workerType) {
        workersData.removeOneAliveWorkerFromCount(territoryName, workerType);
    }

    public int getAliveWorkerCount(String territoryName, WorkerType workerType) {
        return workersData.getAliveWorkerCount(territoryName, workerType);
    }

    //        |    ^
    // TOTAL  |    |
    //   |    | PER TYPE
    //   V    |

    public int getTotalAliveWorkerCount(String territoryName) {
        return workersData.getTotalAliveWorkerCount(territoryName);
    }

    public void showWorkerInventory(Player p, String workerUUID, String territoryName) {

        workersData.showWorkerInventory(p, workerUUID, territoryName);
    }

    public Main getMain() {
        return main;
    }

    public Inventories getTerritoryInventories() {
        return territoryInventories;
    }

    public BukkitTask getWorkerCheckupTask() {
        return workersData.workerCheckupTask;
    }
}