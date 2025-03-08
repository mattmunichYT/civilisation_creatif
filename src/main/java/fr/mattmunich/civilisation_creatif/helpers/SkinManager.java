package fr.mattmunich.civilisation_creatif.helpers;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import fr.mattmunich.civilisation_creatif.Main;

import org.bukkit.Bukkit;

import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;


import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;


import com.google.gson.JsonObject;

public class SkinManager {

    private final Main main;

    public SkinManager(Main main) {
        this.main = main;
    }

    public void changeSkin(Player sender, Player target, String nameOfSkinHolder) {

        if(main.serverVersion != 21.4) {
            sender.sendMessage("§c---------------------------------------------------------------------------");
            sender.sendMessage(main.prefix + "§4Your server version is :§61." + main.serverVersion);
            sender.sendMessage(main.prefix + "§4This feature is not available in this version.");
            sender.sendMessage(main.prefix + "§4Please use 1.21.4 for it to work");
            sender.sendMessage("§e------------------------------------------");
            sender.sendMessage(main.prefix + "§4La version de votre serveur est : §61." + main.serverVersion);
            sender.sendMessage(main.prefix + "§4Cette fonctionnalité n'est pas disponible dans cette version.");
            sender.sendMessage(main.prefix + "§4Merci d'utiliser la version 1.21.4 pour qu'elle fonctionne.");
            sender.sendMessage("§c---------------------------------------------------------------------------");
            return;
        }

        GameProfile profile = ((CraftPlayer) target).getHandle().getBukkitEntity().getProfile();
        String[] textures = getSkin(nameOfSkinHolder);

        if(textures[0].equalsIgnoreCase("error")) {
            if (textures[1].equalsIgnoreCase("not_found")) {
                sender.sendMessage(main.prefix + "§4Skin non trouvé !§e Vérifiez le nom du skin.");
                return;
            }

            if (textures[1].equalsIgnoreCase("other_err")) {
                sender.sendMessage(main.prefix + "§4Une erreur s'est produite.");
                return;
            }
        }

        Bukkit.getOnlinePlayers().forEach(all -> all.hidePlayer(main, target));

        profile.getProperties().removeAll("textures");
        profile.getProperties().put("textures", new Property("textures",textures[0], textures[1]));

        Bukkit.getOnlinePlayers().forEach(all -> all.showPlayer(main, target));

        sender.sendMessage(main.prefix + "§2Le skin de §6" + target.getName() + "§2 a été changé au skin de §6" + nameOfSkinHolder + "§2 ! §8§o(Visible uniquement pour les autres joueurs)");
    }

    private String[] getSkin(String name) {
        try {
            URL url_0 = URI.create("https://api.mojang.com/users/profiles/minecraft/" + name).toURL();
            Reader reader_0 = new InputStreamReader(url_0.openStream());
            String uuid;
            try {
                uuid = JsonParser.parseReader(reader_0).getAsJsonObject().get("id").getAsString();
            } catch (JsonSyntaxException e) {
                return new String[] {"error","not_found"};
            }

            URL url_1 = URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false").toURL();
            Reader reader_1 = new InputStreamReader(url_1.openStream());
            JsonObject textureProperty = JsonParser.parseReader(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            String texture = textureProperty.get("value").getAsString();
            String signature = textureProperty.get("signature").getAsString();

            return new String[] {texture, signature};
        } catch (Exception e) {
            main.logError("Couldn't getSkin() for player name " + name,e);
            return new String[] {"error","other_err"};
        }
    }
}

