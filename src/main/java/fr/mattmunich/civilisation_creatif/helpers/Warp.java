package fr.mattmunich.civilisation_creatif.helpers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import fr.mattmunich.civilisation_creatif.Main;

import javax.annotation.Nullable;

public class Warp {


    private final Main main;
	private FileConfiguration config;
	private File file;


    public Warp(Main main) {
        this.main = main;
		initConfig();
	}


	public void saveConfig() {
		try {
			config.save(file);
		}catch(IOException ioe) { ioe.printStackTrace();}
	}

	File f = new File("plugins/CivilisationCreatif");
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

	public void defineWarp(String warpName, Player player , String worldName, double x, double y, double z, float pitch, float yaw, int minGradeId) {
		//Remove warp if already defined to redefine it properly
		if(getWarpList() !=null && getWarpList().contains(warpName)) {
			player.sendMessage(main.prefix + "§eUn Warp avec ce nom a été détecté. §a§oRedéfinition du Warp en cours...");
			config.set("warp." + warpName, null);
			removeWarpFromList(warpName);
		}

		config.set("warp." + warpName + ".world", worldName);
		config.set("warp." + warpName + ".x", x);
		config.set("warp." + warpName + ".y", y);
		config.set("warp." + warpName + ".z", z);
		config.set("warp." + warpName + ".pitch", pitch);
		config.set("warp." + warpName + ".yaw", yaw);
		config.set("warp." + warpName + ".minGradeId", minGradeId);
		saveConfig();
		addWarpToList(warpName);
		player.sendMessage(main.prefix + "§2Le warp \"§6" + warpName + "§2\" à été défini à votre position !");
    }

	public @Nullable List<String> getWarpList(){
		return config.getStringList("warps");
	}

	private void addWarpToList(String warp) {
		if(getWarpList()==null) {
			List<String> warps = new ArrayList<>();
			warps.add(warp);
			setWarpList(warps);
		} else {
			List<String> warps = getWarpList();
			warps.add(warp);
			setWarpList(warps);
		}
	}

	private void removeWarpFromList(String warp){
		if(getWarpList()!=null){
            try {
                getWarpList().remove(warp);
            } catch (Exception ignored) {}
        }
	}

	private void setWarpList(List<String> warpList){
		config.set("warps", warpList);
		saveConfig();
	}

	public void tpToWarp(String warpName, Player player) {
		if(config.contains("warp." + warpName + ".")) {
			World w = null;
			try {
				w = Bukkit.getServer().getWorld(Objects.requireNonNull(config.getString("warp." + warpName + ".world")));
			} catch (Exception e) {
				Bukkit.getConsoleSender().sendMessage("Error while warp");
			}
			double x = config.getDouble("warp."  + warpName + ".x");
			double y = config.getDouble("warp."  + warpName + ".y");
			double z = config.getDouble("warp."  + warpName + ".z");
			double pitch = config.getDouble("warp."  + warpName + ".pitch");
			double yaw = config.getDouble("warp."  + warpName + ".yaw");
			int minGradeId = config.getInt("warp." + warpName + ".minGradeId");

			if(Grades.isInferior(player, minGradeId)) {
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
		if(getWarpList() !=null && getWarpList().contains(warpName)) {
			removeWarpFromList(warpName);
			config.set("warp."  + warpName, null);

			player.sendMessage(main.prefix + "§2Le warp \"§6" + warpName + "§2"+ "§2\" a été supprimé !");
			saveConfig();
        } else {
			player.sendMessage(main.prefix + "§4Le warp \"§6" + warpName + "§4\" n'existe pas !");
        }
	}

	public void sendWarpListMsg(Player player){
		if(config.get("warp.list")==null){
			player.sendMessage("§e--------§2§lWarps§e--------\n§8§oAucun warp n'a été défini !\n§8§oUn §4Administrateur §8§o peut en définir un en utilisant la commande /setwarp <warpName>");
		} else {
			String warps = config.getString("warp.list");
			player.sendMessage("§e--------§2§lWarps§e--------\n§2Warp(s) défini(s) : §r\n§l§6" + warps.replace(",", ", "));
		}
	}
}
