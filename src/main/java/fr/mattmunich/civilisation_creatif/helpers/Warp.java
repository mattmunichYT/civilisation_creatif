package fr.mattmunich.civilisation_creatif.helpers;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import fr.mattmunich.civilisation_creatif.Main;

public class Warp {


    private final Main main;
	private FileConfiguration config;
	private File file;


    public Warp(Main main) {
        this.main = main;
		initConfig();
	}


	public FileConfiguration getConfig() {
		return config;
	}

	public void saveConfig() {
		try {
			config.save(file);
		}catch(IOException ioe) { ioe.printStackTrace();}
	}

	File f = new File("plugins/AdminCmdsB");
	public void initConfig() {
		if(!f.exists()) {
			f.mkdirs();
		}
		file = new File(f,"warps.yml");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) { e.printStackTrace();}
		}
		new YamlConfiguration();
		config = YamlConfiguration.loadConfiguration(file);
	}

	public void setWarp(String warpName, Player player , String worldName, int x, int y, int z, float pitch, float yaw, int id) {

		getConfig().createSection("warps");
		if(!getConfig().isSet("warp.")) {

			MemorySection.createPath(Objects.requireNonNull(getConfig().getConfigurationSection("warps")), "warp");

			MemorySection.createPath(Objects.requireNonNull(getConfig().getConfigurationSection("warps")), "warp.list");
		}

		getConfig().set("warp." + warpName + ".world", worldName);
		getConfig().set("warp." + warpName + ".x", x);
		getConfig().set("warp." + warpName + ".y", y);
		getConfig().set("warp." + warpName + ".z", z);
		getConfig().set("warp." + warpName + ".pitch", pitch);
		getConfig().set("warp." + warpName + ".yaw", yaw);
		getConfig().set("warp." + warpName + ".id", id);
		if(getConfig().isSet("warp.count")) {
			getConfig().set("warp.count", getConfig().getInt("warp.count") + 1);
		}else{
			getConfig().set("warp.count", 1);
		}

		if(!getConfig().isSet("warp.list")) {
			MemorySection.createPath(Objects.requireNonNull(getConfig().getConfigurationSection("warps")), "warp.list");

			getConfig().set("warp.list", warpName + ",");
			player.sendMessage(main.prefix + "§2Le warp \"§6" + warpName + "§2\" à été défini à votre position !");
			saveConfig();
		}else {
			if(!Objects.requireNonNull(getConfig().get("warp.list")).toString().contains(warpName)) {
				getConfig().set("warp.list", getConfig().get("warp.list") + warpName + ",");
				player.sendMessage(main.prefix + "§2Le warp \"§6" + warpName + "§2\" à été défini à votre position !");
			}else {
				player.sendMessage(main.prefix + "§2Le warp \"§6" + warpName + "§2\" à été redéfini à votre position !");
			}
		}
		saveConfig();
    }

	public void warp(String warpName, Player player) {
		if(getConfig().contains("warp." + warpName + ".")) {
			World w = null;
			try {
				w = Bukkit.getServer().getWorld(Objects.requireNonNull(getConfig().getString("warp." + warpName + ".world")));
			} catch (Exception e) {
				Bukkit.getConsoleSender().sendMessage("Error while warp");
			}
			double x = getConfig().getDouble("warp."  + warpName + ".x");
			double y = getConfig().getDouble("warp."  + warpName + ".y");
			double z = getConfig().getDouble("warp."  + warpName + ".z");
			double pitch = getConfig().getDouble("warp."  + warpName + ".pitch");
			double yaw = getConfig().getDouble("warp."  + warpName + ".yaw");
			int id = getConfig().getInt("warp." + warpName + ".id");

			if(Grades.isInferior(player, id)) {
				player.sendMessage(main.prefix + "§4Vous n'avez pas la permission de vous téléporter à ce warp !");
            }else {
				if(w == null) {
					player.sendMessage(main.prefix + "§4Une erreur s'est produite lors de la téléportation !");
					return;
				}
				player.teleport(new Location(w, x, y, z, (float) yaw, (float) pitch));
				player.sendMessage(main.prefix + "§2Vous avez été téléporté au warp \"§6" + warpName + "§2\" !");
            }
        } else {
			player.sendMessage(main.prefix + "§4Le warp \"§6" + warpName + "§4\" n'existe pas !");
        }

	}

	public void delWarp(String warpName, Player player) {
		if(getConfig().contains("warp."  + warpName + ".")) {
			if(Objects.requireNonNull(getConfig().getString("warp.list")).contains(warpName)) {
				String result = Objects.requireNonNull(getConfig().getString("warp.list")).replace(warpName + ","  , "");

				getConfig().set("warp.list", result);
				saveConfig();
			}else {
				player.sendMessage(main.prefix + "Une erreur s'est produite lors de la suppression du warp. Annulation...");
				return;
			}
			getConfig().set("warp."  + warpName, null);
			getConfig().set("warp.count", getConfig().getInt("warp.count") - 1);

			player.sendMessage(main.prefix + "§2Le warp \"§6" + warpName + "§2"+ "§2\" a été supprimé !");
			saveConfig();
        } else {
			player.sendMessage(main.prefix + "§4Le warp \"§6" + warpName + "§4\" n'existe pas !");
        }
	}

	public void warpListSendMsg(Player player){
		if(!Objects.equals(Objects.requireNonNull(getConfig().get("warp.list")).toString(), "") || Objects.requireNonNull(getConfig().get("warp.list")).toString() != null) {
			String warps = Objects.requireNonNull(getConfig().get("warp.list")).toString();
			player.sendMessage("§e--------§2§lWarps§e--------\n§2Warp(s) défini(s) : §r\n§l§6" + warps.replace(",", ", "));
        }else {
			player.sendMessage("§e--------§2§lWarps§e--------\n§8§oAucun warp n'a été défini !\n§8§oUn §4Administrateur §8§o peut en définir un en utilisant la commande /setwarp <warpName>");
        }
	}
}
