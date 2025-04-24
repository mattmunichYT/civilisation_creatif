package fr.mattmunich.civilisation_creatif.territories;

import fr.mattmunich.civilisation_creatif.helpers.ItemBuilder;
import fr.mattmunich.civilisation_creatif.helpers.WorkerType;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class WorkersData {
    private final TerritoryData territoryData;

    public WorkersData(TerritoryData territoryData) {
        this.territoryData = territoryData;
    }

    public BukkitTask workerCheckupTask = null;

    public List<String> getWorkerList() {
        return territoryData.getConfig().getStringList("workerList");
    }

    public void setWorkerList(List<String> workerList) {
        territoryData.getConfig().set("workerList", workerList);
        territoryData.saveConfig();
    }

    public void addWorkerToList(UUID workerUUID) {
        List<String> workers = getWorkerList();
        workers.add(workerUUID.toString());
        setWorkerList(workers);
        territoryData.saveConfig();
    }

    public void removeWorkerFromList(UUID workerUUID) {
        List<String> workers = getWorkerList();
        try {
            workers.remove(workerUUID.toString());
        } catch (Exception e) {
            territoryData.getMain().logError("Couldn't remove worker " + workerUUID + " from workerList", e);
        }
        setWorkerList(workers);
        territoryData.saveConfig();
    }

    public List<String> getTerritoryWorkerList(String territoryName) {
        return territoryData.getConfig().getStringList("territories." + territoryName + ".workerList");
    }

    public void setTerritoryWorkerList(List<String> workerList, String territoryName) {
        territoryData.getConfig().set("territories." + territoryName + ".workerList", workerList);
        territoryData.saveConfig();
    }

    public void addWorkerToTerritoryList(UUID workerUUID, String territoryName) {
        List<String> workers = getTerritoryWorkerList(territoryName);
        workers.add(workerUUID.toString());
        setTerritoryWorkerList(workers, territoryName);
        territoryData.saveConfig();
    }

    public void removeWorkerFromTerritoryList(UUID workerUUID, String territoryName) {
        List<String> workers = getTerritoryWorkerList(territoryName);
        try {
            workers.remove(workerUUID.toString());
        } catch (Exception e) {
            territoryData.getMain().logError("Couldn't remove worker " + workerUUID + " from Territory workerList", e);
        }
        setTerritoryWorkerList(workers, territoryName);
        territoryData.saveConfig();
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

        Inventory chooseTierInv = Bukkit.createInventory(p, 27, "§6Choisir le tier du villageois");
        for (int slot = 0; slot < 27; slot++) {
            chooseTierInv.setItem(slot, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        }

        for (int tier = 0; tier <= 5; tier++) {
            int price = type.getPrice();
            int income = type.getIncome();
            ChatColor tierColor = ChatColor.DARK_GRAY;
            switch (tier) {
                case 0:
                    chooseTierInv.setItem(10, ItemBuilder.getItem(Material.COAL_BLOCK, tierColor + "Tier 0", Arrays.asList("§aPrix : §6" + price + territoryData.getMain().moneySign, "§aRevenus : §6" + income + territoryData.getMain().moneySign + "§a/mois")));
                    break;
                case 1:
                    price = (int) (price + (price * 0.1));//+10%
                    income = (int) (income + (income * 0.1));//+10%
                    tierColor = ChatColor.GRAY;
                    chooseTierInv.setItem(11, ItemBuilder.getItem(Material.IRON_BLOCK, tierColor + "Tier 1", Arrays.asList("§aPrix : §6" + price + territoryData.getMain().moneySign, "§aRevenus : §6" + income + territoryData.getMain().moneySign + "§a/mois")));
                    break;
                case 2:
                    price = (int) (price + (price * 0.25));//+25%
                    income = (int) (income + (income * 0.25));//+25%
                    tierColor = ChatColor.YELLOW;
                    chooseTierInv.setItem(12, ItemBuilder.getItem(Material.GOLD_BLOCK, tierColor + "Tier 2", Arrays.asList("§aPrix : §6" + price + territoryData.getMain().moneySign, "§aRevenus : §6" + income + territoryData.getMain().moneySign + "§a/mois")));
                    break;
                case 3:
                    price = (int) (price + (price * 0.45));//+45%
                    income = (int) (income + (income * 0.45));//+45%
                    tierColor = ChatColor.GREEN;
                    chooseTierInv.setItem(14, ItemBuilder.getItem(Material.EMERALD_BLOCK, tierColor + "§lTier 3", Arrays.asList("§aPrix : §6" + price + territoryData.getMain().moneySign, "§aRevenus : §6" + income + territoryData.getMain().moneySign + "§a/mois")));
                    break;
                case 4:
                    price = (int) (price + (price * 0.70));//+70%
                    income = (int) (income + (income * 0.70));//+70%
                    tierColor = ChatColor.AQUA;
                    chooseTierInv.setItem(15, ItemBuilder.getItem(Material.DIAMOND_BLOCK, tierColor + "§lTier 4", Arrays.asList("§aPrix : §6" + price + territoryData.getMain().moneySign, "§aRevenus : §6" + income + territoryData.getMain().moneySign + "§a/mois")));
                    break;
                case 5:
                    price = (int) (price + (price * 0.95));//+95%
                    income = (int) (income + (income * 0.95));//+95%
                    tierColor = ChatColor.BLACK;
                    chooseTierInv.setItem(16, ItemBuilder.getItem(Material.NETHERITE_BLOCK, tierColor + "§lTier 5", Arrays.asList("§aPrix : §6" + price + territoryData.getMain().moneySign, "§aRevenus : §6" + income + territoryData.getMain().moneySign + "§a/mois")));
                    break;
            }
        }
        chooseTierInv.setItem(13, ItemBuilder.getItem(Material.PAPER, "§aℹ Choisissez le tier de votre villageois " + formatType(type.name())));
        p.openInventory(chooseTierInv);
    }

    public void buyWorker(Player p, WorkerType type, int tier) {
        String territoryName = territoryData.getPlayerTerritory(p);
        int price = type.getPrice();
        ChatColor tierColor = ChatColor.DARK_GRAY;
        switch (tier) {
            case 0:
                break;
            case 1:
                price = (int) (price + (price * 0.1));//+10%
                tierColor = ChatColor.GRAY;
                break;
            case 2:
                price = (int) (price + (price * 0.25));//+25%
                tierColor = ChatColor.YELLOW;
                break;
            case 3:
                price = (int) (price + (price * 0.45));//+45%
                tierColor = ChatColor.GREEN;
                break;
            case 4:
                price = (int) (price + (price * 0.70));//+70%
                tierColor = ChatColor.AQUA;
                break;
            case 5:
                price = (int) (price + (price * 0.95));//+95%
                tierColor = ChatColor.BLACK;
                break;
        }
        if (territoryData.getPlayerTerritory(p) == null) {
            p.sendMessage(territoryData.getMain().prefix + "§4Vous devez être dans un territoire pour faire cela!");
            return;
        }
        if (!territoryData.isChief(p, territoryData.getPlayerTerritory(p)) && !territoryData.isOfficer(p, territoryData.getPlayerTerritory(p))) {
            p.sendMessage(territoryData.getMain().prefix + "§4Vous devez être le chef/un officier de votre territoire pour faire cela!");
            return;
        }

        if (territoryData.getTerritoryMoney(territoryName) < price) {
            p.sendMessage(territoryData.getMain().prefix + "§4Il n'y a pas assez d'argent dans la banque de votre territoire !");
            return;
        }

        Villager villager = (Villager) Bukkit.getWorld(p.getWorld().getName()).spawnEntity(p.getLocation(), EntityType.VILLAGER);
        UUID workerUUID = UUID.randomUUID();
        villager.addScoreboardTag("workerUUID=" + workerUUID);
        villager.addScoreboardTag("workerType=" + type.name().toLowerCase());
        villager.addScoreboardTag("workerTerritory=" + territoryData.getPlayerTerritory(p));
        villager.addScoreboardTag("tier=" + tier);
        String workerName = formatType(type.name());
        villager.setProfession(type.getProfession());
        villager.setCustomName(tierColor + workerName);
        villager.setCustomNameVisible(true);
        if (type.getLifespan() == -1) {
            villager.setInvulnerable(true);
        }
        ItemStack spawnEgg = new ItemStack(Material.VILLAGER_SPAWN_EGG);
        SpawnEggMeta meta = (SpawnEggMeta) spawnEgg.getItemMeta();
        assert meta != null;
        meta.setSpawnedEntity(Objects.requireNonNull(villager.createSnapshot()));
        meta.setDisplayName("§a" + type.name().substring(0, 1).toUpperCase() + type.name().substring(1).toLowerCase());
        villager.remove();
        spawnEgg.setItemMeta(meta);

        int lifespanIncrement = getAliveWorkerCount(territoryName, WorkerType.MEDECIN) * 5 + (type.equals(WorkerType.SOLDAT) || type.equals(WorkerType.POLICIER) ? getAliveWorkerCount(territoryName, WorkerType.INFIRMIER) * 3 : 0);
        if (lifespanIncrement != 0) {
            p.sendMessage(territoryData.getMain().prefix + "§2Votre villageois a gagné §6" + lifespanIncrement + " jours§2 de durée de vie supplémentaires grâce à " + (getAliveWorkerCount(territoryName, WorkerType.MEDECIN) != 0 ? (getAliveWorkerCount(territoryName, WorkerType.INFIRMIER) != 0 ? "votre/vos §5médecin(s)§2 et votre/vos §5infirmier(s)§2" : "votre/vos §5médecin(s)§2") : "votre/vos §5infirmier(s)§2") + " !");
        }
        territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".bought", System.currentTimeMillis());
        territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".daysToLive", type.getLifespan() + lifespanIncrement);
        territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".daysLived", 0);
        territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".alive", false);
        territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".hasEverBeenSpawned", false);
        territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".type", type.name().toLowerCase());
        territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".name", workerName);
        territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".villagerUUID", null);
        territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".tier", tier);
        territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".spawnEgg", spawnEgg);
        addWorkerToList(workerUUID);
        addWorkerToTerritoryList(workerUUID, territoryName);
        p.getInventory().addItem(spawnEgg);
        territoryData.removeTerritoryMoney(territoryName, price);
        p.playSound(p.getLocation(), type.getSound(), SoundCategory.NEUTRAL, 1, type.getSoundPitch());
        territoryData.sendAnouncementToTerritory(territoryName, "§6" + p.getName() + "§2 a acheté un villageois §6" + workerName + " §2de " + tierColor + (tier >= 3 ? "§lTier " : "tier ") + tier + " §2pour §6" + price + territoryData.getMain().moneySign + "§2 !");
        p.sendMessage(territoryData.getMain().prefix + "§2Vous avez acheté un employé §a" + type.name().toLowerCase() + "§2 pour §a" + price + territoryData.getMain().moneySign + "§2 !");
    }

    public void spawnWorker(Player p, SpawnEggMeta spawnEggMeta, Location spawnLocation, ItemStack it) {
        try {
            String territoryName = territoryData.getPlayerTerritory(p);
            if (!spawnLocation.getBlock().getType().equals(Material.AIR)) {
                spawnLocation.setY(spawnLocation.getY() + 1);
            }
            if (spawnEggMeta == null || spawnEggMeta.getSpawnedEntity() == null) {
                return;
            }
            Villager villager = (Villager) spawnEggMeta.getSpawnedEntity().createEntity(spawnLocation);
            UUID workerUUID = null;
            for (String tag : villager.getScoreboardTags()) {
                if (tag.contains("workerUUID=")) {
                    workerUUID = UUID.fromString(tag.replace("workerUUID=", ""));
                }
            }
            if (workerUUID == null || !getWorkerList().contains(workerUUID.toString())) {
                return;
            }
            if (territoryData.getConfig().getBoolean("territories." + territoryName + ".workers." + workerUUID + ".alive")) {
                villager.remove();
                p.sendMessage(territoryData.getMain().prefix + "§4L'employé existe déjà !");
                return;
            }
            WorkerType workerType;
            try {
                workerType = WorkerType.valueOf(territoryData.getConfig().getString("territories." + territoryName + ".workers." + workerUUID + ".type").toUpperCase());
            } catch (IllegalArgumentException | NullPointerException e) {
                p.sendMessage(territoryData.getMain().prefix + "§4Une erreur s'est produite.");
                territoryData.getMain().logError("Couldn't get workerType when spawning it", e);
                return;
            }
            int tier = territoryData.getConfig().getInt("territories." + territoryName + ".workers." + workerUUID + ".tier");
            territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".alive", true);
            territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".hasEverBeenSpawned", true);
            territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".villagerUUID", villager.getUniqueId().toString());
            territoryData.saveConfig();
            switch (tier) {
                case 0:
                    p.getWorld().playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, SoundCategory.NEUTRAL, 1, 1);
                    Objects.requireNonNull(villager.getLocation().getWorld()).spawnParticle(Particle.HAPPY_VILLAGER, villager.getLocation(), 10, 2, 2, 2);
                    villager.setVillagerLevel(1);
                    break;
                case 1:
                    Objects.requireNonNull(villager.getLocation().getWorld()).spawnParticle(Particle.SCRAPE, villager.getLocation(), 100, 1, 1, 1, 1);
                    p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.NEUTRAL, 1, 1);
                    villager.setVillagerLevel(2);
                    break;
                case 2:
                    Objects.requireNonNull(villager.getLocation().getWorld()).spawnParticle(Particle.FIREWORK, villager.getLocation(), 100, 1, 1, 1, 0.1);
                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.NEUTRAL, 1, 1);
                    villager.setVillagerLevel(3);
                    break;
                case 3:
                    Objects.requireNonNull(villager.getLocation().getWorld()).spawnParticle(Particle.FLASH, villager.getLocation(), 100, 1, 1, 1, 0.1);
                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.NEUTRAL, 1, 1);
                    villager.setVillagerLevel(4);
                    break;
                case 4:
                    Objects.requireNonNull(villager.getLocation().getWorld()).spawnParticle(Particle.POOF, villager.getLocation(), 100, 1, 1, 1, 0.1);
                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, SoundCategory.NEUTRAL, 1, 1);
                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, SoundCategory.NEUTRAL, 1, 0.1f);
                    villager.setVillagerLevel(5);
                    break;
                case 5:
                    Objects.requireNonNull(villager.getLocation().getWorld()).spawnParticle(Particle.EXPLOSION, villager.getLocation(), 100, 1, 1, 1, 0.1);
                    p.getWorld().playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, SoundCategory.NEUTRAL, 0.5f, 0.5f);
                    p.getWorld().playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.NEUTRAL, 1, 1);
                    villager.setVillagerLevel(5);
                    villager.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, PotionEffect.INFINITE_DURATION, 1, false, true));
                    break;
            }
            addAliveWorkerToCount(territoryName, workerType, 1 + tier);
            p.getInventory().remove(it);
            p.sendMessage(territoryData.getMain().prefix + "§2L'employé a bien été spawn !");
        } catch (Exception e) {
            p.sendMessage(territoryData.getMain().prefix + "§4Une erreur s'est produite lors du spawn de cet employé !");
            territoryData.getMain().logError("An error encourred while spawning a worker", e);
        }
    }

    public void spawnWorker(Villager villager, Location spawnLocation) {
        try {
            if (!spawnLocation.getBlock().getType().equals(Material.AIR)) {
                spawnLocation.setY(spawnLocation.getY() + 1);
            }
            if (villager == null) {
                return;
            }
            UUID workerUUID = null;
            for (String tag : villager.getScoreboardTags()) {
                if (tag.contains("workerUUID=")) {
                    workerUUID = UUID.fromString(tag.replace("workerUUID=", ""));
                }
            }
            String territoryName = getWorkerTerritory(villager);
            if (workerUUID == null || !getWorkerList().contains(workerUUID.toString()) || territoryName == null || spawnLocation.getWorld() == null) {
                return;
            }
            if (territoryData.getConfig().getBoolean("territories." + territoryName + ".workers." + workerUUID + ".alive")) {
                villager.remove();
                return;
            }
            villager.setHealth(20);
//            villager = (Villager) villager.createSnapshot().createEntity(spawnLocation); not needed
            territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".alive", true);
            territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".hasEverBeenSpawned", true);
            territoryData.getConfig().set("territories." + territoryName + ".workers." + workerUUID + ".villagerUUID", villager.getUniqueId().toString());
            territoryData.saveConfig();

            spawnLocation.getWorld().spawnParticle(Particle.ASH, villager.getLocation(), 100, 2, 2, 2);
            spawnLocation.getWorld().playSound(spawnLocation, Sound.BLOCK_FIRE_EXTINGUISH, 1, 1);
        } catch (Exception e) {
            territoryData.getMain().logError("An error encourred while spawning a worker", e);
        }
    }

    public String getWorkerTerritory(Villager villager) {
        String workerTerritory = null;
        for (String tag : villager.getScoreboardTags()) {
            if (tag.contains("workerTerritory=")) {
                workerTerritory = tag.replace("workerTerritory=", "");
            }
        }
        return workerTerritory;
    }

    public void runWorkerCheckup() {
        Bukkit.getConsoleSender().sendMessage(territoryData.getMain().prefix + "§eRunning daily worker checkup...");
        for (String territoryName : territoryData.getTerritoriesList()) {
            int territorySumMoney = 0;
            for (String workerUUID : getTerritoryWorkerList(territoryName)) {
                String pathToWorker = "territories." + territoryName + ".workers." + workerUUID;
                boolean workerAlive = territoryData.getConfig().getBoolean(pathToWorker + ".alive");
                int tier = territoryData.getConfig().getInt(pathToWorker + ".tier");
                WorkerType workerType = WorkerType.valueOf(Objects.requireNonNull(territoryData.getConfig().getString(pathToWorker + ".type")).toUpperCase());
                if (!workerAlive) {
                    continue;
                }
                if (!(Bukkit.getEntity(UUID.fromString(Objects.requireNonNull(territoryData.getConfig().getString(pathToWorker + ".villagerUUID")))) instanceof Villager worker)) {
                    continue;
                }
                int daysToLive = territoryData.getConfig().getInt(pathToWorker + ".daysToLive");
                if (daysToLive != -1) {
                    daysToLive = daysToLive - 1;
                    territoryData.getConfig().set(pathToWorker + ".daysToLive", daysToLive);
                }
                int daysLived = territoryData.getConfig().getInt(pathToWorker + ".daysLived") + 1;
                territoryData.getConfig().set(pathToWorker + ".daysLived", daysLived);
                if (Math.floor((double) daysLived / 30) == (double) daysLived / 30) {
                    int income = workerType.getIncome();
                    switch (tier) {
                        case 0:
                            break;
                        case 1:
                            income = (int) (income + (income * 0.1));//+10%
                            break;
                        case 2:
                            income = (int) (income + (income * 0.25));//+25%
                            break;
                        case 3:
                            income = (int) (income + (income * 0.45));//+45%
                            break;
                        case 4:
                            income = (int) (income + (income * 0.70));//+70%
                            break;
                        case 5:
                            income = (int) (income + (income * 0.95));//+95%
                            break;
                    }
                    //INCOME DECREMENT BECAUSE OF VILLAGERS "ROLES"
                    //POISSONIER
                    if (workerType == WorkerType.POISSONNIER) {
                        if (getAliveWorkerCount(territoryName, WorkerType.PECHEUR) < getAliveWorkerCount(territoryName, WorkerType.POISSONNIER)) {
                            income = (int) (income * 0.70); //-30%
                        }
                    }
                    //BOUCHER
                    if (workerType == WorkerType.BOUCHER) {
                        if (getAliveWorkerCount(territoryName, WorkerType.ELEVEUR) < getAliveWorkerCount(territoryName, WorkerType.BOUCHER)) {
                            income = (int) (income * 0.70); //-30%
                        }
                    }
                    //BOULANGER
                    if (workerType == WorkerType.BOULANGER) {
                        if (getAliveWorkerCount(territoryName, WorkerType.AGRICULTEUR) < getAliveWorkerCount(territoryName, WorkerType.BOULANGER)) {
                            income = (int) (income * 0.70); //-30%
                        }
                    }
                    //
                    territoryData.addTerritoryMoney(territoryName, income);
                    territorySumMoney = income;
                }
//                addTerritoryMoney(territoryName,workerType.getIncome()); // FOR TESTING
                if (daysToLive == 0) {
                    territoryData.sendAnouncementToTerritory(territoryName, "§eUn de vos villageois §c" + formatType(workerType.toString()) + "§e est mort de viellesse !");
                    worker.remove();
                    removeWorkerFromTerritoryList(UUID.fromString(workerUUID), territoryName);
                    removeWorkerFromList(UUID.fromString(workerUUID));
                    removeOneAliveWorkerFromCount(territoryName, workerType);
                    territoryData.getConfig().set(pathToWorker, null);
                }
                territoryData.saveConfig();
            }
            if (territorySumMoney != 0) {
                territoryData.sendAnouncementToTerritory(territoryName, "§2Vous avez gagné §6" + territorySumMoney + territoryData.getMain().moneySign + " grâce à vos villageois ce mois-ci");
            }
        }
        programNextWorkerCheckup();
        Bukkit.getConsoleSender().sendMessage(territoryData.getMain().prefix + "§aDone running daily worker checkup !");
    }

    public void programNextWorkerCheckup() {
        // Cancel any existing scheduled task before scheduling a new one
        if (workerCheckupTask != null && !workerCheckupTask.isCancelled()) {
            Bukkit.getConsoleSender().sendMessage(territoryData.getMain().prefix + "Duplicate WorkerCheckup detected! Cancelling previous one.");
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
            workerCheckupTask =Bukkit.getScheduler().runTaskLater(territoryData.getPlugin(), () -> {
                runWorkerCheckup();
                workerCheckupTask = null; // Clear reference after execution
            }, ticks);
        } catch (Exception e) {
            territoryData.getMain().logError("§4Couldn't schedule next WorkerCheckup", e);
        }
    }

    public String getWorkerTerritory(String workerUUID) {
        try {
            if (!getWorkerList().contains(workerUUID)) {
                return null;
            }
            try {
                for (String key : territoryData.getConfig().getConfigurationSection("territories").getKeys(false)) {
                    List<String> territoryWorkerList = getTerritoryWorkerList(key);
                    try {
                        for (String worker : territoryWorkerList) {
                            if (worker.equals(workerUUID)) {
                                return key;
                            }
                        }
                    } catch (Exception e) {
                        territoryData.getMain().logError("Couldn't check worker territory", e);
                    }
                }
                return null;
            } catch (NullPointerException e) {
                territoryData.getMain().logError("Couldn't check worker territory", e);
                return null;
            }
        } catch (Exception e) {
            territoryData.getMain().logError("Couldn't check worker territory", e);
            return null;

        }
    }

    public void showBuyWorkerInv(Player p) {
        Inventory inv = Bukkit.createInventory(p, 45, "§6Acheter un villegeois");
        for (int slot = 0; slot < 45; slot++) {
            inv.setItem(slot, ItemBuilder.getItem(Material.GRAY_STAINED_GLASS_PANE, ""));
        }

        int[] slots = {11, 12, 13, 14, 15, 19, 20, 21, 22, 23, 24, 25, 29, 30, 31, 32, 33};
        for (int slot : slots) {
            inv.setItem(slot, null);
        }

        for (WorkerType workerType : WorkerType.values()) {
            Material item = workerType.getItem();
            ChatColor typeColor = workerType.getColor();
            String typeName = formatType(workerType.toString());
            String price = "§aPrix : " + typeColor + workerType.getPrice() + territoryData.getMain().moneySign;
            String income = "§aRevenus : §6" + workerType.getIncome() + territoryData.getMain().moneySign + "§a/mois";
            String lifespan = "§aDurée de vie : " + (workerType.getLifespan() == -1 ? "§b§oInvincible" : "§6" + workerType.getLifespan() / 30 + "§a mois");
            inv.addItem(ItemBuilder.getItem(item, typeColor + typeName, false, false, price, income, lifespan));
        }
        inv.setItem(44, ItemBuilder.getItem(Material.BARRIER, "§c❌ Fermer le menu"));

        p.openInventory(inv);
    }

    public void setAliveWorkerCount(String territoryName, int workerCount, WorkerType workerType) {
        territoryData.getConfig().set("territories." + territoryName + ".aliveWorkerCount." + workerType.name().toLowerCase(), workerCount);
        territoryData.saveConfig();
    }

    public void addOneAliveWorkerToCount(String territoryName, WorkerType workerType) {
        int currentWorkerCount = getAliveWorkerCount(territoryName, workerType);
        setAliveWorkerCount(territoryName, currentWorkerCount + 1, workerType);
    }

    public void addAliveWorkerToCount(String territoryName, WorkerType workerType, int count) {
        int currentWorkerCount = getAliveWorkerCount(territoryName, workerType);
        setAliveWorkerCount(territoryName, currentWorkerCount + count, workerType);
    }

    public void removeAliveWorkerFromCount(String territoryName, WorkerType workerType, int count) {
        int currentWorkerCount = getAliveWorkerCount(territoryName, workerType);
        setAliveWorkerCount(territoryName, currentWorkerCount - count, workerType);
    }

    public void removeOneAliveWorkerFromCount(String territoryName, WorkerType workerType) {
        int currentWorkerCount = getAliveWorkerCount(territoryName, workerType);
        setAliveWorkerCount(territoryName, currentWorkerCount - 1, workerType);
    }

    public int getAliveWorkerCount(String territoryName, WorkerType workerType) {
        String path = "territories." + territoryName + ".aliveWorkerCount." + workerType.name().toLowerCase();
        return (territoryData.getConfig().get(path) == null || territoryData.getConfig().getInt(path) < 0) ? 0 : territoryData.getConfig().getInt(path);
    }

    public void setTotalAliveWorkerCount(String territoryName, int workerCount) {
        territoryData.getConfig().set("territories." + territoryName + ".aliveWorkerCount.total", workerCount);
        territoryData.saveConfig();
    }

    public void addOneTotalAliveWorkerToCount(String territoryName) {
        int currentWorkerCount = getTotalAliveWorkerCount(territoryName);
        setTotalAliveWorkerCount(territoryName, currentWorkerCount + 1);
    }

    public void removeOneTotalAliveWorkerFromCount(String territoryName) {
        int currentWorkerCount = getTotalAliveWorkerCount(territoryName);
        setTotalAliveWorkerCount(territoryName, currentWorkerCount - 1);
    }

    public int getTotalAliveWorkerCount(String territoryName) {
        return (territoryData.getConfig().get("territories." + territoryName + ".aliveWorkerCount.total") == null || territoryData.getConfig().getInt("territories." + territoryName + ".aliveWorkerCount.total") < 0) ? 0 : territoryData.getConfig().getInt("territories." + territoryName + ".aliveWorkerCount.total");
    }

    public void showWorkerInventory(Player p, String workerUUID, String territoryName) {
        Inventory workerInv = Bukkit.createInventory(p, 9, "§bGérer le villageois");
        String pathToWorker = "territories." + territoryName + ".workers." + workerUUID;
        boolean workerAlive = territoryData.getConfig().getBoolean(pathToWorker + ".alive");
        int tier = territoryData.getConfig().getInt(pathToWorker + ".tier");
        WorkerType workerType = WorkerType.valueOf(Objects.requireNonNull(territoryData.getConfig().getString(pathToWorker + ".type")).toUpperCase());
        int daysToLive = territoryData.getConfig().getInt(pathToWorker + ".daysToLive");
        int daysLived = territoryData.getConfig().getInt(pathToWorker + ".daysLived") + 1;
        territoryData.getConfig().set(pathToWorker + ".daysLived", daysLived);
        int income = workerType.getIncome();
        ChatColor tierColor = ChatColor.DARK_GRAY;
        switch (tier) {
            case 0:
                break;
            case 1:
                tierColor = ChatColor.GRAY;
                income = (int) (income + (income * 0.1));//+10%
                break;
            case 2:
                tierColor = ChatColor.YELLOW;
                income = (int) (income + (income * 0.25));//+25%
                break;
            case 3:
                tierColor = ChatColor.GREEN;
                income = (int) (income + (income * 0.45));//+45%
                break;
            case 4:
                tierColor = ChatColor.AQUA;
                income = (int) (income + (income * 0.70));//+70%
                break;
            case 5:
                tierColor = ChatColor.BLACK;
                income = (int) (income + (income * 0.95));//+95%
                break;
        }

        Material workerItemType = workerType.getItem();
        String typeName = formatType(workerType.toString());
        String workerAliveString = "§aEn vie/activité : " + (workerAlive ? "§2Oui" : "§cNon");
        String incomeString = "§aRevenus : §6" + income + territoryData.getMain().moneySign + "§a/mois";
        String lifespan = "§aDurée de vie restante : " + (workerType.getLifespan() == -1 ? "§b§oInvincible" : (daysToLive < 10 ? "§4" : daysToLive < 30 ? "§c" : daysToLive < 45 ? "§e" : daysToLive < 90 ? "§6" : "§1") + daysToLive + "§a jours");
        String tierString = "§aTier : §6" + tier;

        ItemStack workerItem = new ItemStack(workerItemType);
        ItemMeta workerItemMeta = workerItem.getItemMeta();
        assert workerItemMeta != null;
        PersistentDataContainer data = workerItemMeta.getPersistentDataContainer();
        data.set(new NamespacedKey(territoryData.getPlugin(), "workerUUID"), PersistentDataType.STRING, workerUUID);
        workerItemMeta.setDisplayName(tierColor + typeName);
        workerItemMeta.setLore(Arrays.asList(workerAliveString, incomeString, lifespan, tierString));
        workerItem.setItemMeta(workerItemMeta);
        workerInv.setItem(4, workerItem);
        if (!workerAlive) {
            workerInv.setItem(0, ItemBuilder.getItem(Material.VILLAGER_SPAWN_EGG, "§a🥚 Obtenir l'œuf d'appaition du villageois"));
        } else {
            workerInv.setItem(0, ItemBuilder.getItem(Material.IRON_SWORD, "§c\uD83D\uDDE1 Faire disparaitre le villageois"));
        }
        workerInv.setItem(8, ItemBuilder.getItem(Material.BARRIER, "§c❌ Fermer le menu"));
        p.openInventory(workerInv);
    }
}