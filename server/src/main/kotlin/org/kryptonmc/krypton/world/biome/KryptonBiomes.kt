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
import org.kryptonmc.api.registry.Registries

object KryptonBiomes {

    val OCEAN = register("ocean") { BuiltInBiomes.ocean(it, false) }
    val PLAINS = register("plains", BuiltInBiomes::plains)
    val DESERT = register("desert") { BuiltInBiomes.desert(it, 0.125F, 0.05F) }
    val MOUNTAINS = register("mountains") { BuiltInBiomes.mountain(it, 1F, 0.5F) }
    val FOREST = register("forest") { BuiltInBiomes.forest(it, 0.1F, 0.2F) }
    val TAIGA = register("taiga") { BuiltInBiomes.taiga(it, 0.2F, 0.2F, false) }
    val SWAMP = register("swamp") { BuiltInBiomes.swamp(it, -0.2F, 0.1F) }
    val RIVER = register("river") { BuiltInBiomes.river(it, -0.5F, 0F, 0.5F, 4159204, false) }
    val NETHER_WASTES = register("nether_wastes", BuiltInBiomes::netherWastes)
    val THE_END = register("the_end", BuiltInBiomes::end)
    val FROZEN_OCEAN = register("frozen_ocean") { BuiltInBiomes.frozenOcean(it, false) }
    val FROZEN_RIVER = register("frozen_river") { BuiltInBiomes.river(it, -0.5F, 0F, 0F, 3750089, true) }
    val SNOWY_TUNDRA = register("snowy_tundra") { BuiltInBiomes.tundra(it, 0.125F, 0.05F) }
    val SNOWY_MOUNTAINS = register("snowy_mountains") { BuiltInBiomes.tundra(it, 0.45F, 0.3F) }
    val MUSHROOM_FIELDS = register("mushroom_fields") { BuiltInBiomes.mushroomFields(it, 0.2F, 0.3F) }
    val MUSHROOM_FIELD_SHORE = register("mushroom_field_shore") { BuiltInBiomes.mushroomFields(it, 0F, 0.025F) }
    val BEACH = register("beach") { BuiltInBiomes.beach(it, 0F, 0.025F, 0.8F, 0.4F, 4159204, false, false) }
    val DESERT_HILLS = register("desert_hills") { BuiltInBiomes.desert(it, 0.45F, 0.3F) }
    val WOODED_HILLS = register("wooded_hills") { BuiltInBiomes.forest(it, 0.45F, 0.3F) }
    val TAIGA_HILLS = register("taiga_hills") { BuiltInBiomes.taiga(it, 0.45F, 0.3F, false) }
    val MOUNTAIN_EDGE = register("mountain_edge") { BuiltInBiomes.mountain(it, 0.8F, 0.3F) }
    val JUNGLE = register("jungle", BuiltInBiomes::jungle)
    val JUNGLE_HILLS = register("jungle_hills", BuiltInBiomes::jungleHills)
    val JUNGLE_EDGE = register("jungle_edge", BuiltInBiomes::jungleEdge)
    val DEEP_OCEAN = register("deep_ocean") { BuiltInBiomes.ocean(it, true) }
    val STONE_SHORE = register("stone_shore") { BuiltInBiomes.beach(it, 0.1F, 0.8F, 0.2F, 0.3F, 4159204, false, true) }
    val SNOWY_BEACH = register("snowy_beach") { BuiltInBiomes.beach(it, 0F, 0.025F, 0.05F, 0.3F, 4020182, true, false) }
    val BIRCH_FOREST = register("birch_forest") { BuiltInBiomes.birchForest(it, 0.1F, 0.2F) }
    val BIRCH_FOREST_HILLS = register("birch_forest_hills") { BuiltInBiomes.birchForest(it, 0.45F, 0.3F) }
    val DARK_FOREST = register("dark_forest") { BuiltInBiomes.darkForest(it, 0.1F, 0.2F) }
    val SNOWY_TAIGA = register("snowy_taiga") { BuiltInBiomes.taiga(it, 0.2F, 0.2F, true) }
    val SNOWY_TAIGA_HILLS = register("snowy_taiga_hills") { BuiltInBiomes.taiga(it, 0.45F, 0.3F, true) }
    val GIANT_TREE_TAIGA = register("giant_tree_taiga") { BuiltInBiomes.giantTreeTaiga(it, 0.2F, 0.2F, 0.3F) }
    val GIANT_TREE_TAIGA_HILLS = register("giant_tree_taiga_hills") { BuiltInBiomes.giantTreeTaiga(it, 0.45F, 0.3F, 0.3F) }
    val WOODED_MOUNTAINS = register("wooded_mountains") { BuiltInBiomes.mountain(it, 1F, 0.5F) }
    val SAVANNA = register("savanna") { BuiltInBiomes.savanna(it, 0.125F, 0.05F, 1.2F) }
    val SAVANNA_PLATEAU = register("savanna_plateau", BuiltInBiomes::savannaPlateau)
    val BADLANDS = register("badlands") { BuiltInBiomes.badlands(it, 0.1F, 0.2F) }
    val WOODED_BADLANDS = register("wooded_badlands") { BuiltInBiomes.badlands(it, 1.5F, 0.025F) }
    val BADLANDS_PLATEAU = register("badlands_plateau") { BuiltInBiomes.badlands(it, 1.5F, 0.025F) }
    val SMALL_END_ISLANDS = register("small_end_islands", BuiltInBiomes::end)
    val END_MIDLANDS = register("end_midlands", BuiltInBiomes::end)
    val END_HIGHLANDS = register("end_highlands", BuiltInBiomes::end)
    val END_BARRENS = register("end_barrens", BuiltInBiomes::end)
    val WARM_OCEAN = register("warm_ocean") { BuiltInBiomes.warmOcean(it, false) }
    val LUKEWARM_OCEAN = register("lukewarm_ocean") { BuiltInBiomes.lukewarmOcean(it, false) }
    val COLD_OCEAN = register("cold_ocean") { BuiltInBiomes.coldOcean(it, false) }
    val DEEP_WARM_OCEAN = register("deep_warm_ocean") { BuiltInBiomes.warmOcean(it, true) }
    val DEEP_LUKEWARM_OCEAN = register("deep_lukewarm_ocean") { BuiltInBiomes.lukewarmOcean(it, true) }
    val DEEP_COLD_OCEAN = register("deep_cold_ocean") { BuiltInBiomes.coldOcean(it, true) }
    val DEEP_FROZEN_OCEAN = register("deep_frozen_ocean") { BuiltInBiomes.frozenOcean(it, true) }
    val THE_VOID = register("the_void", 127, BuiltInBiomes::void)
    val SUNFLOWER_PLAINS = register("sunflower_plains", 129, BuiltInBiomes::plains)
    val DESERT_LAKES = register("desert_lakes") { BuiltInBiomes.desert(it, 0.225F, 0.25F) }
    val GRAVELLY_MOUNTAINS = register("gravelly_mountains") { BuiltInBiomes.mountain(it, 1F, 0.5F) }
    val FLOWER_FOREST = register("flower_forest", BuiltInBiomes::flowerForest)
    val TAIGA_MOUNTAINS = register("taiga_mountains") { BuiltInBiomes.taiga(it, 0.3F, 0.4F, false) }
    val SWAMP_HILLS = register("swamp_hills") { BuiltInBiomes.swamp(it, -0.1F, 0.3F) }
    val ICE_SPIKES = register("ice_spikes", 140) { BuiltInBiomes.tundra(it, 0.425F, 0.45000002F) }
    val MODIFIED_JUNGLE = register("modified_jungle", 149, BuiltInBiomes::modifiedJungle)
    val MODIFIED_JUNGLE_EDGE = register("modified_jungle_edge", 151, BuiltInBiomes::modifiedJungleEdge)
    val TALL_BIRCH_FOREST = register("tall_birch_forest", 155) { BuiltInBiomes.birchForest(it, 0.2F, 0.4F) }
    val TALL_BIRCH_HILLS = register("tall_birch_hills") { BuiltInBiomes.birchForest(it, 0.55F, 0.5F) }
    val DARK_FOREST_HILLS = register("dark_forest_hills") { BuiltInBiomes.darkForest(it, 0.2F, 0.4F) }
    val SNOWY_TAIGA_MOUNTAINS = register("snowy_taiga_mountains") { BuiltInBiomes.taiga(it, 0.3F, 0.4F, true) }
    val GIANT_SPRUCE_TAIGA = register("giant_spruce_taiga", 160) { BuiltInBiomes.giantTreeTaiga(it, 0.2F, 0.2F, 0.25F) }
    val GIANT_SPRUCE_TAIGA_HILLS = register("giant_spruce_taiga_hills") { BuiltInBiomes.giantTreeTaiga(it, 0.2F, 0.2F, 0.25F) }
    val MODIFIED_GRAVELLY_MOUNTAINS = register("modified_gravelly_mountains") { BuiltInBiomes.mountain(it, 1F, 0.5F) }
    val SHATTERED_SAVANNA = register("shattered_savanna") { BuiltInBiomes.savanna(it, 0.3625F, 1.225F, 1.1F) }
    val SHATTERED_SAVANNA_PLATEAU = register("shattered_savanna_plateau") { BuiltInBiomes.savanna(it, 1.05F, 1.2125001F, 1F) }
    val ERODED_BADLANDS = register("eroded_badlands", BuiltInBiomes::erodedBadlands)
    val MODIFIED_WOODED_BADLANDS_PLATEAU = register("modified_wooded_badlands_plateau") { BuiltInBiomes.badlands(it, 0.45F, 0.3F) }
    val MODIFIED_BADLANDS_PLATEAU = register("modified_badlands_plateau") { BuiltInBiomes.badlands(it, 0.45F, 0.3F) }
    val BAMBOO_JUNGLE = register("bamboo_jungle", BuiltInBiomes::bambooJungle)
    val BAMBOO_JUNGLE_HILLS = register("bamboo_jungle_hills", BuiltInBiomes::bambooJungleHills)
    val SOUL_SAND_VALLEY = register("soul_sand_valley", BuiltInBiomes::soulSandValley)
    val CRIMSON_FOREST = register("crimson_forest", BuiltInBiomes::crimsonForest)
    val WARPED_FOREST = register("warped_forest", BuiltInBiomes::warpedForest)
    val BASALT_DELTAS = register("basalt_deltas", BuiltInBiomes::basaltDeltas)
    val DRIPSTONE_CAVES = register("dripstone_caves", BuiltInBiomes::dripstoneCaves)
    val LUSH_CAVES = register("lush_caves", BuiltInBiomes::lushCaves)

    private fun register(name: String, id: Int, biome: (Key) -> KryptonBiome): KryptonBiome {
        val key = Key.key(name)
        return Registries.register(Registries.BIOME, id, key, biome(key)) as KryptonBiome
    }

    private fun register(name: String, biome: (Key) -> KryptonBiome): KryptonBiome {
        val key = Key.key(name)
        return Registries.register(Registries.BIOME, key, biome(key)) as KryptonBiome
    }
}
