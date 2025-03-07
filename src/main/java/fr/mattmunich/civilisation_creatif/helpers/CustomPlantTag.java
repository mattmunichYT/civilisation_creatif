package fr.mattmunich.civilisation_creatif.helpers;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;

import java.util.EnumSet;
import java.util.Set;

public class CustomPlantTag {
    private static final Set<Material> CUSTOM_PLANT_TAG = EnumSet.of(
            // Moss & Leaves & Grass Block
            Material.MOSS_BLOCK, Material.MOSS_CARPET,
            Material.AZALEA, Material.FLOWERING_AZALEA,
            Material.VINE, Material.GRASS_BLOCK,

            // Mushrooms & Fungi
            Material.BROWN_MUSHROOM, Material.RED_MUSHROOM,
            Material.MUSHROOM_STEM, Material.WARPED_FUNGUS,
            Material.CRIMSON_FUNGUS, Material.NETHER_SPROUTS,
            Material.WARPED_ROOTS, Material.CRIMSON_ROOTS,

            // Grasses & Ferns
            Material.SHORT_GRASS, Material.TALL_GRASS,
            Material.FERN, Material.LARGE_FERN,
            Material.DEAD_BUSH, Material.SEAGRASS,
            Material.TALL_SEAGRASS,

            // Flowers & Plants
            Material.LILY_PAD, Material.SUNFLOWER,
            Material.SMALL_DRIPLEAF, Material.BIG_DRIPLEAF,
            Material.SPORE_BLOSSOM, Material.GLOW_LICHEN,
            Material.GLOW_BERRIES,

            // Cave Plants & Glow Plants
            Material.TWISTING_VINES, Material.WEEPING_VINES,
            Material.CAVE_VINES, Material.CAVE_VINES_PLANT,

            // Bamboo & Other Plants
            Material.BAMBOO, Material.BAMBOO_SAPLING,
            Material.SUGAR_CANE, Material.CACTUS,
            Material.KELP, Material.KELP_PLANT, Material.DRIED_KELP_BLOCK,

            // Sea Plants & Water Plants
            Material.SEA_PICKLE,

            // Amethyst & Other Blocks
            Material.AMETHYST_BLOCK, Material.BUDDING_AMETHYST,
            Material.SMALL_AMETHYST_BUD, Material.MEDIUM_AMETHYST_BUD,
            Material.LARGE_AMETHYST_BUD, Material.AMETHYST_CLUSTER,

            // Fruits & Crops
            Material.MELON, Material.PUMPKIN
    );

    public static boolean isCustomPlant(Material type) {
        return CUSTOM_PLANT_TAG.contains(type)
                || Tag.FLOWERS.isTagged(type)  // Includes all flowers
                || Tag.LEAVES.isTagged(type)   // Includes leaves
                || Tag.SAPLINGS.isTagged(type) // Includes saplings
                || Tag.CORALS.isTagged(type);  // Includes corals
    }
}
