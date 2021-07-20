/*
 * This file is part of the Krypton project, licensed under the GNU General Public License v3.0
 *
 * Copyright (C) 2021 KryptonMC and the contributors of the Krypton project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.kryptonmc.krypton.world.biome

import net.kyori.adventure.key.Key
import org.kryptonmc.api.registry.RegistryKey
import org.kryptonmc.api.registry.RegistryKeys

object BiomeKeys {

    val OCEAN = register("ocean")
    val PLAINS = register("plains")
    val DESERT = register("desert")
    val MOUNTAINS = register("mountains")
    val FOREST = register("forest")
    val TAIGA = register("taiga")
    val SWAMP = register("swamp")
    val RIVER = register("river")
    val NETHER_WASTES = register("nether_wastes")
    val THE_END = register("the_end")
    val FROZEN_OCEAN = register("frozen_ocean")
    val FROZEN_RIVER = register("frozen_river")
    val SNOWY_TUNDRA = register("snowy_tundra")
    val SNOWY_MOUNTAINS = register("snowy_mountains")
    val MUSHROOM_FIELDS = register("mushroom_fields")
    val MUSHROOM_FIELD_SHORE = register("mushroom_field_shore")
    val BEACH = register("beach")
    val DESERT_HILLS = register("desert_hills")
    val WOODED_HILLS = register("wooded_hills")
    val TAIGA_HILLS = register("taiga_hills")
    val MOUNTAIN_EDGE = register("mountain_edge")
    val JUNGLE = register("jungle")
    val JUNGLE_HILLS = register("jungle_hills")
    val JUNGLE_EDGE = register("jungle_edge")
    val DEEP_OCEAN = register("deep_ocean")
    val STONE_SHORE = register("stone_shore")
    val SNOWY_BEACH = register("snowy_beach")
    val BIRCH_FOREST = register("birch_forest")
    val BIRCH_FOREST_HILLS = register("birch_forest_hills")
    val DARK_FOREST = register("dark_forest")
    val SNOWY_TAIGA = register("snowy_taiga")
    val SNOWY_TAIGA_HILLS = register("snowy_taiga_hills")
    val GIANT_TREE_TAIGA = register("giant_tree_taiga")
    val GIANT_TREE_TAIGA_HILLS = register("giant_tree_taiga_hills")
    val WOODED_MOUNTAINS = register("wooded_mountains")
    val SAVANNA = register("savanna")
    val SAVANNA_PLATEAU = register("savanna_plateau")
    val BADLANDS = register("badlands")
    val WOODED_BADLANDS_PLATEAU = register("wooded_badlands_plateau")
    val BADLANDS_PLATEAU = register("badlands_plateau")
    val SMALL_END_ISLANDS = register("small_end_islands")
    val END_MIDLANDS = register("end_midlands")
    val END_HIGHLANDS = register("end_highlands")
    val END_BARRENS = register("end_barrens")
    val WARM_OCEAN = register("warm_ocean")
    val LUKEWARM_OCEAN = register("lukewarm_ocean")
    val COLD_OCEAN = register("cold_ocean")
    val DEEP_WARM_OCEAN = register("deep_warm_ocean")
    val DEEP_LUKEWARM_OCEAN = register("deep_lukewarm_ocean")
    val DEEP_COLD_OCEAN = register("deep_cold_ocean")
    val DEEP_FROZEN_OCEAN = register("deep_frozen_ocean")
    val THE_VOID = register("the_void")
    val SUNFLOWER_PLAINS = register("sunflower_plains")
    val DESERT_LAKES = register("desert_lakes")
    val GRAVELLY_MOUNTAINS = register("gravelly_mountains")
    val FLOWER_FOREST = register("flower_forest")
    val TAIGA_MOUNTAINS = register("taiga_mountains")
    val SWAMP_HILLS = register("swamp_hills")
    val ICE_SPIKES = register("ice_spikes")
    val MODIFIED_JUNGLE = register("modified_jungle")
    val MODIFIED_JUNGLE_EDGE = register("modified_jungle_edge")
    val TALL_BIRCH_FOREST = register("tall_birch_forest")
    val TALL_BIRCH_HILLS = register("tall_birch_hills")
    val DARK_FOREST_HILLS = register("dark_forest_hills")
    val SNOWY_TAIGA_MOUNTAINS = register("snowy_taiga_mountains")
    val GIANT_SPRUCE_TAIGA = register("giant_spruce_taiga")
    val GIANT_SPRUCE_TAIGA_HILLS = register("giant_spruce_taiga_hills")
    val MODIFIED_GRAVELLY_MOUNTAINS = register("modified_gravelly_mountains")
    val SHATTERED_SAVANNA = register("shattered_savanna")
    val SHATTERED_SAVANNA_PLATEAU = register("shattered_savanna_plateau")
    val ERODED_BADLANDS = register("eroded_badlands")
    val MODIFIED_WOODED_BADLANDS_PLATEAU = register("modified_wooded_badlands_plateau")
    val MODIFIED_BADLANDS_PLATEAU = register("modified_badlands_plateau")
    val BAMBOO_JUNGLE = register("bamboo_jungle")
    val BAMBOO_JUNGLE_HILLS = register("bamboo_jungle_hills")
    val SOUL_SAND_VALLEY = register("soul_sand_valley")
    val CRIMSON_FOREST = register("crimson_forest")
    val WARPED_FOREST = register("warped_forest")
    val BASALT_DELTAS = register("basalt_deltas")
    val DRIPSTONE_CAVES = register("dripstone_caves")
    val LUSH_CAVES = register("lush_caves")

    private fun register(key: String) = RegistryKey(RegistryKeys.BIOME, Key.key(key))
}
