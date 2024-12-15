package fr.mattmunich.civilisation_creatif.helpers;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Utility {

	@SuppressWarnings("deprecation")
	public static UUID getUUIDFromName(String name) {

		Player player = Bukkit.getPlayer(name);

		if(player != null) {
			return player.getUniqueId();
		} else {

			OfflinePlayer oplayer = Bukkit.getOfflinePlayer(name);

            return oplayer.getUniqueId();
        }
    }

	public static String getNameFromUUID(UUID UUID) {

		Player player = Bukkit.getPlayer(UUID);

		if(player != null) {
			return player.getName();
		} else {

			OfflinePlayer oplayer = Bukkit.getOfflinePlayer(UUID);

            return oplayer.getName();
        }
    }
}
