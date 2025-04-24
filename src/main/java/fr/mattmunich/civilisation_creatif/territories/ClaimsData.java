package fr.mattmunich.civilisation_creatif.territories;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class ClaimsData {
    private final TerritoryData territoryData;

    public ClaimsData(TerritoryData territoryData) {
        this.territoryData = territoryData;
    }

    public int baseChunkPrice = 300;

    public void claimChunk(Player sender, String territoryName, Chunk chunk) {
        try {
            Map<Integer, Integer> chunkMap = getChunkMap(chunk);
            Team territory = territoryData.getTerritoryTeam(territoryName);
            List<Map<?, ?>> terrClaims = territoryData.getConfig().getMapList("territories." + territory.getName() + ".claims." + chunk.getWorld().getName());
            List<Map<?, ?>> globalClaims = territoryData.getConfig().getMapList("claims." + chunk.getWorld().getName());
            if (globalClaims.contains(chunkMap)) {
                if (terrClaims.contains(chunkMap)) {
                    sender.sendMessage(territoryData.getMain().prefix + "§eVous avez déjà claim ce chunk !");
                } else {
                    sender.sendMessage(territoryData.getMain().prefix + "§cVous ne pouvez pas claim un chunk car il est §4déjà claim §cpar le territoire §4" + getChunkOwner(chunk) + " §c!");
                }
                return;
            }
            if (territoryData.getTerritoryMoney(territoryName) < getChunkPrice(territoryName)) {
                sender.sendMessage(territoryData.getMain().prefix + "§4Iln'y a pas assez d'argent dans la banque du territoire !");
                return;
            }
            terrClaims.add(chunkMap);
            globalClaims.add(chunkMap);
            territoryData.getConfig().set("territories." + territory.getName() + ".claims." + chunk.getWorld().getName(), terrClaims);
            territoryData.getConfig().set("claims." + chunk.getWorld().getName(), globalClaims);
            territoryData.saveConfig();
            territoryData.removeTerritoryMoney(territoryName, getChunkPrice(territoryName));
            sender.sendMessage(territoryData.getMain().prefix + "§aVous avez §2claim §ale chunk §e" + chunk.getX() + "§a,§e " + chunk.getZ() + " §apour §e" + getChunkPrice(territoryName) + territoryData.getMain().moneySign + "§a!");
            addOneClaimedChunkToCount(territoryName);
        } catch (Exception e) {
            sender.sendMessage(territoryData.getMain().prefix + "§4Une erreur s'est produite lors du claim du chunk !");
            territoryData.getMain().logError("Couldn't claim chunk for territory " + territoryName, e);
        }
    }

    public int getChunkPrice(String territoryName) {
        return baseChunkPrice + ((getClaimdChunkCount(territoryName)) * 50);
    }

    public void setClaimedChunkCount(String territoryName, int claimedChunkCount) {
        territoryData.getConfig().set("territories." + territoryName + ".claimedChunkCount", claimedChunkCount);
        territoryData.saveConfig();
    }

    public void addOneClaimedChunkToCount(String territoryName) {
        int currentClaimedChunkCount = getClaimdChunkCount(territoryName);
        setClaimedChunkCount(territoryName, currentClaimedChunkCount + 1);
    }

    public void removeOneClaimedChunkFromCount(String territoryName) {
        int currentClaimedChunkCount = getClaimdChunkCount(territoryName);
        setClaimedChunkCount(territoryName, currentClaimedChunkCount - 1);
    }

    public int getClaimdChunkCount(String territoryName) {
        return territoryData.getConfig().get("territories." + territoryName + ".claimedChunkCount") == null ? 0 : territoryData.getConfig().getInt("territories." + territoryName + ".claimedChunkCount");
    }

    public void unclaimChunk(Player sender, String territoryName, Chunk chunk) {
        try {
            Map<Integer, Integer> chunkMap = getChunkMap(chunk);
            Team territory = territoryData.getTerritoryTeam(territoryName);
            List<Map<?, ?>> claims = territoryData.getConfig().getMapList("territories." + territory.getName() + ".claims." + chunk.getWorld().getName());
            if (claims.contains(chunkMap)) {
                claims.remove(chunkMap);
                territoryData.getConfig().set("territories." + territory.getName() + ".claims." + chunk.getWorld().getName(), claims);

                List<Map<?, ?>> globalClaims = territoryData.getConfig().getMapList("claims." + chunk.getWorld().getName());
                globalClaims.remove(chunkMap);
                territoryData.getConfig().set("claims." + chunk.getWorld().getName(), globalClaims);
                territoryData.saveConfig();
                sender.sendMessage(territoryData.getMain().prefix + "§aVous avez §cunclaim §ale chunk §e" + chunk.getX() + "§a,§e " + chunk.getZ());
                removeOneClaimedChunkFromCount(territoryName);
            } else {
                sender.sendMessage(territoryData.getMain().prefix + "§cVous n'avez pas claim le chunk §e" + chunk.getX() + "§a,§e " + chunk.getZ());
            }
        } catch (Exception e) {
            sender.sendMessage(territoryData.getMain().prefix + "§4Une erreur s'est produite lors de l'unclaim du chunk !");
            territoryData.getMain().logError("Couldn't UNclaim chunk for territory " + territoryName, e);
        }
    }

    public List<Chunk> getTerritoryChunks(String territoryName) {
        try {
            Team territory = territoryData.getTerritoryTeam(territoryName);
            List<Chunk> territoryClaims = new ArrayList<Chunk>();
            try {
                for (World world : Bukkit.getWorlds()) {
                    List<Map<?, ?>> claims = territoryData.getConfig().getMapList("territories." + territory.getName() + ".claims." + world.getName());
                    for (Map<?, ?> chunk : claims) {
                        territoryClaims.add(getChunkFromMap((Map<Integer, Integer>) chunk, world));
                    }
                }
                return territoryClaims;
            } catch (ClassCastException e) {
                territoryData.getMain().logError("Couldn't cast Map<Integer, Integer> for chunks of territory " + territoryName, e);
                return null;
            }
        } catch (Exception e) {
            territoryData.getMain().logError("Couldn't get claimed chunks for territory " + territoryName, e);
            return null;

        }
    }

    public boolean chunkClaimed(Chunk chunk) {
        try {
            List<Map<?, ?>> globalClaims = territoryData.getConfig().getMapList("claims." + chunk.getWorld().getName());
            return globalClaims.contains(getChunkMap(chunk));
        } catch (Exception e) {
            territoryData.getMain().logError("Couldn't check if chunk was claimed", e);
            return false;

        }
    }

    public String getChunkOwner(Chunk chunk) {
        try {
            if (!chunkClaimed(chunk)) {
                return null;
            }
            try {
                for (String key : Objects.requireNonNull(territoryData.getConfig().getConfigurationSection("territories")).getKeys(false)) {
                    List<Chunk> territoryClaims = getTerritoryChunks(key);
                    try {
                        for (Chunk territoryClaim : territoryClaims) {
                            if (territoryClaim.equals(chunk)) {
                                return key;
                            }
                        }
                    } catch (Exception e) {
                        territoryData.getMain().logError("Couldn't check chunk owner", e);
                    }
                }
                return null;
            } catch (NullPointerException e) {
                territoryData.getMain().logError("Couldn't check chunk owner", e);
                return null;
            }
        } catch (Exception e) {
            territoryData.getMain().logError("Couldn't check chunk owner", e);
            return null;

        }
    }

    public Map<Integer, Integer> getChunkMap(Chunk chunk) {
        int x = chunk.getX();
        int z = chunk.getZ();
        Map<Integer, Integer> chunkMap = new HashMap<Integer, Integer>();
        chunkMap.put(x, z);
        return chunkMap;
    }

    public Chunk getChunkFromMap(Map<Integer, Integer> chunkMap, World world) {
        Map.Entry<Integer, Integer> entry = chunkMap.entrySet().iterator().next();
        int x = entry.getKey();
        int z = entry.getValue();
        return world.getChunkAt(x, z);
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


        if (!Objects.equals(getChunkOwner(north), getChunkOwner(chunk))) {
            for (int x = minX; x < minX + 16; x++) {
                player.spawnParticle(Particle.DUST, x, y, minZ, 1, dustOptions);
            }
        }

        Chunk south = world.getChunkAt(chunkX, chunkZ + 1);
        if (!Objects.equals(getChunkOwner(south), getChunkOwner(chunk))) {
            for (int x = minX; x < minX + 16; x++) {
                player.spawnParticle(Particle.DUST, x, y, minZ + 16, 1, dustOptions);
            }
        }

        Chunk west = world.getChunkAt(chunkX - 1, chunkZ);
        if (!Objects.equals(getChunkOwner(west), getChunkOwner(chunk))) {
            for (int z = minZ; z < minZ + 16; z++) {
                player.spawnParticle(Particle.DUST, minX, y, z, 1, dustOptions);
            }
        }

        Chunk east = world.getChunkAt(chunkX + 1, chunkZ);
        if (!Objects.equals(getChunkOwner(east), getChunkOwner(chunk))) {
            for (int z = minZ; z < minZ + 16; z++) {
                player.spawnParticle(Particle.DUST, minX + 16, y, z, 1, dustOptions);
            }
        }
    }

    static Particle.DustOptions getDustOptions(ChatColor chatColor) {
        Color color = Color.WHITE;
        return switch (chatColor) {
            case AQUA -> {
                color = Color.fromRGB(84, 255, 255);
                yield new Particle.DustOptions(color, 1);
            }
            case BLACK -> {
                color = Color.BLACK;
                yield new Particle.DustOptions(color, 1);
            }
            case BLUE -> {
                color = Color.fromRGB(85, 85, 255);
                yield new Particle.DustOptions(color, 1);
            }
            case DARK_AQUA -> {
                color = Color.fromRGB(0, 170, 170);
                yield new Particle.DustOptions(color, 1);
            }
            case DARK_BLUE -> {
                color = Color.fromRGB(2, 0, 170);
                yield new Particle.DustOptions(color, 1);
            }
            case GRAY -> {
                color = Color.fromRGB(170, 170, 170);
                yield new Particle.DustOptions(color, 1);
            }
            case DARK_GRAY -> {
                color = Color.fromRGB(85, 85, 85);
                yield new Particle.DustOptions(color, 1);
            }
            case DARK_GREEN -> {
                color = Color.fromRGB(2, 170, 1);
                yield new Particle.DustOptions(color, 1);
            }
            case GREEN -> {
                color = Color.fromRGB(86, 255, 84);
                yield new Particle.DustOptions(color, 1);
            }
            case DARK_PURPLE -> {
                color = Color.fromRGB(170, 1, 170);
                yield new Particle.DustOptions(color, 1);
            }
            case LIGHT_PURPLE -> {
                color = Color.fromRGB(255, 85, 255);
                yield new Particle.DustOptions(color, 1);
            }
            case DARK_RED -> {
                color = Color.fromRGB(170, 0, 1);
                yield new Particle.DustOptions(color, 1);
            }
            case RED -> {
                color = Color.fromRGB(255, 85, 85);
                yield new Particle.DustOptions(color, 1);
            }
            case YELLOW -> {
                color = Color.fromRGB(255, 255, 85);
                yield new Particle.DustOptions(color, 1);
            }
            case GOLD -> {
                color = Color.fromRGB(255, 170, 1);
                yield new Particle.DustOptions(color, 1);
            }
            case WHITE -> {
                color = Color.WHITE;
                yield new Particle.DustOptions(color, 1);
            }
            default -> new Particle.DustOptions(color, 1);
        };
    }
}