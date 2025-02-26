package fr.mattmunich.civilisation_creatif.helpers;

import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.mattmunich.civilisation_creatif.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public final class PlayerData {

	private final Plugin plugin;

	private Main main;

	private FileConfiguration config;
	private File file;

	private Player p = null;

    public PlayerData(Plugin plugin, Main main) {
		this.plugin = plugin;
		this.main = main;
	}

	private void logError(String message, Exception error){
		main.logError(message, error);
	}



	File f = new File("plugins/CivilisationCreatif/PlayerData");
	public PlayerData(UUID uuid) throws Exception{
        if(!f.exists()) {
			f.mkdirs();
		}
		file = new File(f, uuid.toString() + ".yml");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) { logError("Couldn't create PlayerData file", e); }
		}
		new YamlConfiguration();
		config = YamlConfiguration.loadConfiguration(file);
		this.plugin = getPlugin();
		this.p=Bukkit.getPlayer(uuid);
		if(Utility.getNameFromUUID(uuid) == null) {
			throw new Exception("Error while getting player with UUID");
		}

		String pName = Utility.getNameFromUUID(uuid);
		config.set("player.name", pName);
		if(config.getString("player.rank") == null) {
			config.set("player.rank", "membre");
		}
		saveConfig();
	}

	public PlayerData(Player p){
		if(!f.exists()) {
			f.mkdirs();
		}
		file = new File(f, p.getUniqueId() + ".yml");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) { logError("Couldn't create PlayerData file", e); }
		}
		new YamlConfiguration();
		config = YamlConfiguration.loadConfiguration(file);
		this.plugin = getPlugin();

		config.set("player.name", p.getName());
		if(config.getString("player.rank") == null) {
			config.set("player.rank", "membre");
		}
		this.p = p;
		saveConfig();
	}

	private void setXPScore(int setXP) {
		String name = config.getString("player.name");
		Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
        assert name != null;
        Score xp = Objects.requireNonNull(scoreboard.getObjective("xp")).getScore(name);
		xp.setScore(setXP);
		if(p!=null) {SidebarManager.updateScoreboard(p);}
	}

	private void setMoneyScore(int setMoney) {
		String name = config.getString("player.name");
		Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
        assert name != null;
        Score money = Objects.requireNonNull(scoreboard.getObjective("money")).getScore(name);
		money.setScore(setMoney);
		if(p!=null) {SidebarManager.updateScoreboard(p);}
	}

	public int getXPScore() {
		String name = config.getString("player.name");
		Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
		assert name != null;
		Score xp = Objects.requireNonNull(scoreboard.getObjective("xp")).getScore(name);
		return xp.getScore();
	}

	public int getMoneyScore() {
		String name = config.getString("player.name");
		Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
		assert name != null;
		Score money = Objects.requireNonNull(scoreboard.getObjective("money")).getScore(name);
		return money.getScore();
	}

	public Plugin getPlugin(){
		return plugin;
	}

	public FileConfiguration getConfig() {
		return config;
	}

	public Grades getRank() {
		String rank = config.getString("player.rank");
		for(Grades grades : Grades.values()) {
            assert rank != null;
            if(Objects.equals(grades.getName().toLowerCase(), rank.toLowerCase())) {
				return grades;
			}
		}
		return null;
	}

	public void setRank(Grades grade) {
		config.set("player.rank", grade.getName().toLowerCase());
		saveConfig();
	}

	@SuppressWarnings("unused")
    public int level() {
		//CHANGE THIS (LEVEL = 1000XP)
		return config.getInt("civilisation.xp")/1000;
	}

	public int xp() { return config.getInt("civilisation.xp");}

	public void setXP(int xp) {
		config.set("civilisation.xp", xp);
		setXPScore(xp);
		saveConfig();
	}

	public void addXP(int xp) {
		int xpNow = config.getInt("civilisation.xp") + xp;
		setXPScore(xpNow);
		config.set("civilisation.xp", xpNow);
		saveConfig();
	}

	public void removeXP(int xp) {
		int xpNow = config.getInt("civilisation.xp") - xp;
		setXPScore(xpNow);
		config.set("civilisation.xp", xpNow);
		saveConfig();
	}

	public void resetXP() {
		config.set("civilisation.xp", 0);
		setXPScore(0);
		saveConfig();
	}

	public int money() { return config.getInt("civilisation.money");}

	public void setMoney(int money) {
		config.set("civilisation.money", money);
		setMoneyScore(money);
		saveConfig();
	}

	public void addMoney(int money) {
		int moneyNow = config.getInt("civilisation.money") + money;
		setMoneyScore(moneyNow);
		config.set("civilisation.money", moneyNow);
		saveConfig();
	}

	public void removeMoney(int money) {
		int moneyNow = config.getInt("civilisation.money") - money;
		setMoneyScore(moneyNow);
		config.set("civilisation.money", moneyNow);
		saveConfig();
	}

	public void resetMoney() {
		config.set("civilisation.money", 0);
		setMoneyScore(0);
		saveConfig();
	}

	public void inviteToTerritory(String territoryName, Player sender){
		List<Map<?, ?>> invites = config.getMapList("civilisation.territories.invites");
		if(config.get("civilisation.territories.invites") != null) {
			Map <String,String> invite = new HashMap<>();
			invite.put(territoryName, sender.getUniqueId().toString());
			invites.add(invite);
		} else {
			Map <String,String> invite = new HashMap<>();
			invite.put(territoryName, sender.getUniqueId().toString());
			invites = new ArrayList<>();
			invites.add(invite);
		}

		config.set("civilisation.territories.invites", invites);
		saveConfig();
	}

	public List<Map<?, ?>> getInvitesToTerritory(){
		return config.getMapList("civilisation.territories.invites");
	}

	public void removeInviteToTerritory(String territoryName){
		List<Map<?, ?>> invites = config.getMapList("civilisation.territories.invites");
		try {
			invites.removeIf(map -> map.containsKey(territoryName));
		} catch(NullPointerException e) {
			logError("Couldn't remove invite to territory in PlayerData", e);
		}
		config.set("civilisation.territories.invites", invites);
		saveConfig();
	}

	public void setTerritory(String territoryName) {
		config.set("civilisation.territories.territory",territoryName);
		if(p!=null) {SidebarManager.updateScoreboard(p);}
		saveConfig();
	}

	public String getTerritory(){
		return config.getString("civilisation.territories.territory");
	}

	@SuppressWarnings("unused")
    public void setTempbanned(String from, String reason, long duration, String sanction) {
		config.set("player.punishments.tempban.istempbanned", true);
		config.set("player.punishments.tempban.from", from);
		config.set("player.punishments.tempban.reason", reason);
		config.set("player.punishments.tempban.sanction", sanction);
		config.set("player.punishments.tempban.duration", duration + System.currentTimeMillis());
		config.set("player.punishments.tempban.end", new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(duration + System.currentTimeMillis()));
		config.set("player.punishments.tempban.timestamp", new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(duration));

		saveConfig();
	}

	@SuppressWarnings("unused")
    public void setUnTempbanned() {
		config.set("player.punishments.tempban", null);
		saveConfig();
	}

	@SuppressWarnings("unused")
    public String getTempbannedReason() {
		return config.getString("player.punishments.tempban.reason");
	}
	@SuppressWarnings("unused")
    public String getTempbannedFrom() {
		return config.getString("player.punishments.tempban.from");
	}

	@SuppressWarnings("unused")
    public String getTempbanSanction() {
		return config.getString("player.punishments.tempban.sanction");
	}

	@SuppressWarnings("unused")
    public long getTempbanMilliseconds() {
		return config.getLong("player.punishments.tempban.duration");
	}

	public String getTempbanEnd() {
		return config.getString("player.punishments.tempban.end");
	}

	public String getTempbanTimestamp() {
		return config.getString("player.punishments.tempban.timestamp");
	}

	public boolean isTempbanned() {
		return config.contains("player.punishments.tempban");
	}
	public void setTempmuted(String from, String reason, long time, String sanction) {
		config.set("player.punishments.tempmuted.isTempmuted", true);
		config.set("player.punishments.tempmuted.from", from);
		config.set("player.punishments.tempmuted.reason", reason);
		config.set("player.punishments.tempmuted.sanction", sanction);
		config.set("player.punishments.tempmuted.duration", time + System.currentTimeMillis());
		config.set("player.punishments.tempmuted.end", new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(time + System.currentTimeMillis()));
		config.set("player.punishments.tempmuted.timestamp", new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(time));

		saveConfig();
	}

	public void setUnTempmuted() {
		config.set("player.punishments.tempmuted", null);
		saveConfig();
	}

	public String getTempmutedReason() {
		return config.getString("player.punishments.tempmuted.reason");
	}
	public String getTempmutedFrom() {
		return config.getString("player.punishments.tempmuted.from");
	}

	public String getTempmuteSanction() {
		return config.getString("player.punishments.tempmuted.sanction");
	}

	public long getTempmuteMilliseconds() {
		return config.getLong("player.punishments.tempmuted.duration");
	}

	public String getTempmuteEnd() {
		return config.getString("player.punishments.tempmuted.end");
	}

	public String getTempmuteTimestamp() {
		return config.getString("player.punishments.tempmuted.timestamp");
	}

	public boolean isTempmuted() {
		return config.contains("player.punishments.tempmuted");
	}


	public void setBanned(String from, String reason) {
		config.set("player.punishments.ban.isbanned", true);
		config.set("player.punishments.ban.from", from);
		config.set("player.punishments.ban.reason", reason);
		saveConfig();
	}

	public void setUnBanned() {
		config.set("player.punishments.ban", null);
		saveConfig();
	}

	public String getBannedReason() {
		return config.getString("player.punishments.ban.reason");
	}
	public String getBannedFrom() {
		return config.getString("player.punishments.ban.from");
	}

	public boolean isBanned() {
		return config.contains("player.punishments.ban");
	}

	public boolean isMuted() {
		return getConfig().getBoolean("player.punishments.muted");
	}

	public void setMuted(boolean muted) {
		config.set("player.punishments.muted", muted);
		saveConfig();
	}

	public void saveConfig() {
		try {
			getConfig().save(file);
		}catch(Exception e) { logError("Couldn't save PlayerData file", e);}
	}

	public boolean haveHomes(){
        return getConfig().getString("home.list") != null;
	}

	public String getHomes(){
		if(getConfig().getString("home.list") != null) {
            return Objects.requireNonNull(getConfig().get("home.list")).toString();
		}else {
			return "";
		}
	}
	public void setIP(String ip) {
		config.set("player.ip", ip);
		saveConfig();
	}

	public String getStoredIP() {
		return config.getString("player.ip");
	}

	public boolean changedIP(String newIP) {

		try {
			if(config.get("player.ip") == null) {
				return false;
			} else return !Objects.equals(getStoredIP(), newIP);
		} catch (Exception ignored) {
			return false;
		}
	}

	public String getSkin() {
		try {
			String name = config.getString("player.name");
			URL url_0 = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
			InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());
			String uuid = new JsonParser().parse(reader_0).getAsJsonObject().get("id").getAsString();

			URL url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
			InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());
			JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
			String texture = textureProperty.get("value").getAsString();
//			String signature = textureProperty.get("signature").getAsString();

//			return new String[] {texture, signature};
//			Bukkit.getConsoleSender().sendMessage("texture = " + texture + "\n signature = " + signature);
			byte[] bytedecoded = Base64.getDecoder().decode(texture);
			String decoded = new String(bytedecoded);
			JsonObject jsonObject = new JsonParser().parse(decoded).getAsJsonObject();
//			Bukkit.getConsoleSender().sendMessage(jsonObject.toString());

			String textureUrl = jsonObject.get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
//			String capeUrl = jsonObject.get("textures").getAsJsonObject().get("CAPE").getAsJsonObject().get("url").getAsString();

			return textureUrl;


		} catch (Exception e) {
			logError("Couldn't get player skin",e);
			return "";
		}
	}

	public ItemStack getSkull(OfflinePlayer player, String lore) {
		UUID uuid = player.getUniqueId();

        PlayerProfile playerProfile = null;
        try {
            playerProfile = Bukkit.getServer().createPlayerProfile(uuid);
            PlayerTextures textures = playerProfile.getTextures();
            textures.setSkin(new URL(getSkin()));
            playerProfile.setTextures(textures);
        } catch (MalformedURLException e) { logError("Couldn't get player Skull",e);}

		ItemStack pHead = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta phm = (SkullMeta) pHead.getItemMeta();
		assert phm != null;
		phm.setOwnerProfile(playerProfile);
		phm.setDisplayName(player.getName());
		phm.setLore(Collections.singletonList(lore));
		pHead.setItemMeta(phm);
		return  pHead;
	}

}
