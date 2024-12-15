package fr.mattmunich.civilisation_creatif.helpers;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public final class PlayerData {

	private final Plugin plugin;

	private FileConfiguration config;
	private File file;

    public PlayerData(Plugin plugin) {
		this.plugin = plugin;
	}


	File f = new File("plugins/CivilisationCreatif/PlayerData");
	public PlayerData(UUID uuid) throws Exception {
		if(!f.exists()) {
			f.mkdirs();
		}
		file = new File(f, uuid.toString() + ".yml");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) { e.printStackTrace();}
		}
		new YamlConfiguration();
		config = YamlConfiguration.loadConfiguration(file);
		this.plugin = getPlugin();
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

	private void setXPScore(int setXP) {
		String name = config.getString("player.name");
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Score xp = scoreboard.getObjective("xp").getScore(name);
		xp.setScore(setXP);
	}

	private void setMoneyScore(int setMoney) {
		String name = config.getString("player.name");
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Score money = scoreboard.getObjective("money").getScore(name);
		money.setScore(setMoney);
	}

	public boolean exist() {
		return file.exists();
	}

	public final Plugin getPlugin(){
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
	public void setRank(String gradeName) {
		config.set("player.rank", gradeName.toLowerCase());
		saveConfig();
	}

	public int level() {
		//CHANGE THIS (LEVEL = 1000XP)
		return config.getInt("civilisation.xp")/1000;
	}

	public int XP() {
		return config.getInt("civilisation.xp");
	}

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

	public int Money() {
		return config.getInt("civilisation.money");
	}

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
		if(invites != null) {
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
		} catch(NullPointerException ignored) {}
		config.set("civilisation.territories.invites", invites);
		saveConfig();
	}

	public void setTerritory(String territoryName) {
		config.set("civilisation.territories.territory",territoryName);
		saveConfig();
	}

	public String getTerritory(){
		return config.getString("civilisation.territories.territory");
	}

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

	public void setUnTempbanned() {
		config.set("player.punishments.tempban", null);
		saveConfig();
	}

	public String getTempbannedReason() {
		return config.getString("player.punishments.tempban.reason");
	}
	public String getTempbannedFrom() {
		return config.getString("player.punishments.tempban.from");
	}

	public String getTempbanSanction() {
		return config.getString("player.punishments.tempban.sanction");
	}

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
		}catch(IOException ioe) { ioe.printStackTrace();}
	}

	public boolean haveHomes(){
		if(getConfig().getString("home.list") != null) {
			return true;
		}else {
			return false;
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
			config.get("player.ip");
		} catch (Exception e) {
			return false;
		}

		if(config.get("player.ip") == null) {
			return false;
		} else if(getStoredIP().equalsIgnoreCase(newIP) || Objects.equals(getStoredIP(), newIP)) {
			return false;
		} else {
			return true;
		}
	}

}
