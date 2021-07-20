/*
 * This file is part of the Krypton API, licensed under the MIT license.
 *
 * Copyright (C) 2021 KryptonMC and the contributors to the Krypton project.
 *
 * This project is licensed under the terms of the MIT license.
 * For more details, please reference the LICENSE file in the api top-level directory.
 */
package org.kryptonmc.api.world.biome

import net.kyori.adventure.key.Key
import org.kryptonmc.api.registry.Registries

/**
 * All the built-in biomes.
 */
object Biomes {

    // @formatter:off
    @JvmField val OCEAN = get("ocean")
    @JvmField val PLAINS = get("plains")
    @JvmField val DESERT = get("desert")
    @JvmField val MOUNTAINS = get("mountains")
    @JvmField val FOREST = get("forest")
    @JvmField val TAIGA = get("taiga")
    @JvmField val SWAMP = get("swamp")
    @JvmField val RIVER = get("river")
    @JvmField val NETHER_WASTES = get("nether_wastes")
    @JvmField val THE_END = get("the_end")
    @JvmField val FROZEN_OCEAN = get("frozen_ocean")
    @JvmField val FROZEN_RIVER = get("frozen_river")
    @JvmField val SNOWY_TUNDRA = get("snowy_tundra")
    @JvmField val SNOWY_MOUNTAINS = get("snowy_mountains")
    @JvmField val MUSHROOM_FIELDS = get("mushroom_fields")
    @JvmField val MUSHROOM_FIELD_SHORE = get("mushroom_field_shore")
    @JvmField val BEACH = get("beach")
    @JvmField val DESERT_HILLS = get("desert_hills")
    @JvmField val WOODED_HILLS = get("wooded_hills")
    @JvmField val TAIGA_HILLS = get("taiga_hills")
    @JvmField val MOUNTAIN_EDGE = get("mountain_edge")
    @JvmField val JUNGLE = get("jungle")
    @JvmField val JUNGLE_HILLS = get("jungle_hills")
    @JvmField val JUNGLE_EDGE = get("jungle_edge")
    @JvmField val DEEP_OCEAN = get("deep_ocean")
    @JvmField val STONE_SHORE = get("stone_shore")
    @JvmField val SNOWY_BEACH = get("snowy_beach")
    @JvmField val BIRCH_FOREST = get("birch_forest")
    @JvmField val BIRCH_FOREST_HILLS = get("birch_forest_hills")
    @JvmField val DARK_FOREST = get("dark_forest")
    @JvmField val SNOWY_TAIGA = get("snowy_taiga")
    @JvmField val SNOWY_TAIGA_HILLS = get("snowy_taiga_hills")
    @JvmField val GIANT_TREE_TAIGA = get("giant_tree_taiga")
    @JvmField val GIANT_TREE_TAIGA_HILLS = get("giant_tree_taiga_hills")
    @JvmField val WOODED_MOUNTAINS = get("wooded_mountains")
    @JvmField val SAVANNA = get("savanna")
    @JvmField val SAVANNA_PLATEAU = get("savanna_plateau")
    @JvmField val BADLANDS = get("badlands")
    @JvmField val WOODED_BADLANDS_PLATEAU = get("wooded_badlands_plateau")
    @JvmField val BADLANDS_PLATEAU = get("badlands_plateau")
    @JvmField val SMALL_END_ISLANDS = get("small_end_islands")
    @JvmField val END_MIDLANDS = get("end_midlands")
    @JvmField val END_HIGHLANDS = get("end_highlands")
    @JvmField val END_BARRENS = get("end_barrens")
    @JvmField val WARM_OCEAN = get("warm_ocean")
    @JvmField val LUKEWARM_OCEAN = get("lukewarm_ocean")
    @JvmField val COLD_OCEAN = get("cold_ocean")
    @JvmField val DEEP_WARM_OCEAN = get("deep_warm_ocean")
    @JvmField val DEEP_LUKEWARM_OCEAN = get("deep_lukewarm_ocean")
    @JvmField val DEEP_COLD_OCEAN = get("deep_cold_ocean")
    @JvmField val DEEP_FROZEN_OCEAN = get("deep_frozen_ocean")
    @JvmField val THE_VOID = get("the_void")
    @JvmField val SUNFLOWER_PLAINS = get("sunflower_plains")
    @JvmField val DESERT_LAKES = get("desert_lakes")
    @JvmField val GRAVELLY_MOUNTAINS = get("gravelly_mountains")
    @JvmField val FLOWER_FOREST = get("flower_forest")
    @JvmField val TAIGA_MOUNTAINS = get("taiga_mountains")
    @JvmField val SWAMP_HILLS = get("swamp_hills")
    @JvmField val ICE_SPIKES = get("ice_spikes")
    @JvmField val MODIFIED_JUNGLE = get("modified_jungle")
    @JvmField val MODIFIED_JUNGLE_EDGE = get("modified_jungle_edge")
    @JvmField val TALL_BIRCH_FOREST = get("tall_birch_forest")
    @JvmField val TALL_BIRCH_HILLS = get("tall_birch_hills")
    @JvmField val DARK_FOREST_HILLS = get("dark_forest_hills")
    @JvmField val SNOWY_TAIGA_MOUNTAINS = get("snowy_taiga_mountains")
    @JvmField val GIANT_SPRUCE_TAIGA = get("giant_spruce_taiga")
    @JvmField val GIANT_SPRUCE_TAIGA_HILLS = get("giant_spruce_taiga_hills")
    @JvmField val MODIFIED_GRAVELLY_MOUNTAINS = get("modified_gravelly_mountains")
    @JvmField val SHATTERED_SAVANNA = get("shattered_savanna")
    @JvmField val SHATTERED_SAVANNA_PLATEAU = get("shattered_savanna_plateau")
    @JvmField val ERODED_BADLANDS = get("eroded_badlands")
    @JvmField val MODIFIED_WOODED_BADLANDS_PLATEAU = get("modified_wooded_badlands_plateau")
    @JvmField val MODIFIED_BADLANDS_PLATEAU = get("modified_badlands_plateau")
    @JvmField val BAMBOO_JUNGLE = get("bamboo_jungle")
    @JvmField val BAMBOO_JUNGLE_HILLS = get("bamboo_jungle_hills")
    @JvmField val SOUL_SAND_VALLEY = get("soul_sand_valley")
    @JvmField val CRIMSON_FOREST = get("crimson_forest")
    @JvmField val WARPED_FOREST = get("warped_forest")
    @JvmField val BASALT_DELTAS = get("basalt_deltas")
    @JvmField val DRIPSTONE_CAVES = get("dripstone_caves")
    @JvmField val LUSH_CAVES = get("lush_caves")

    // @formatter:on
    private fun get(name: String) = Registries.BIOME[Key.key(name)]
}
