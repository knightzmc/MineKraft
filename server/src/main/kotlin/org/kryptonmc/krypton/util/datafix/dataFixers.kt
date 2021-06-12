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
package org.kryptonmc.krypton.util.datafix

import com.mojang.datafixers.DataFixerBuilder
import com.mojang.datafixers.schemas.Schema
import org.kryptonmc.krypton.ServerInfo
import org.kryptonmc.krypton.util.BOOTSTRAP_EXECUTOR
import org.kryptonmc.krypton.util.datafix.fix.AddChoices
import org.kryptonmc.krypton.util.datafix.fix.PickupArrowFix
import org.kryptonmc.krypton.util.datafix.fix.RenameBlockFix
import org.kryptonmc.krypton.util.datafix.fix.RenameBlockWithJigsawFix
import org.kryptonmc.krypton.util.datafix.fix.RenameCauldronFix
import org.kryptonmc.krypton.util.datafix.fix.RenameItemFix
import org.kryptonmc.krypton.util.datafix.fix.RenameStatsFix
import org.kryptonmc.krypton.util.datafix.fix.SavedDataPoolElementFeatureFix
import org.kryptonmc.krypton.util.datafix.schema.NamespacedSchema
import org.kryptonmc.krypton.util.datafix.schema.V2571
import org.kryptonmc.krypton.util.datafix.schema.V2684
import org.kryptonmc.krypton.util.datafix.schema.V2686
import org.kryptonmc.krypton.util.datafix.schema.V2688
import org.kryptonmc.krypton.util.datafix.schema.V2704
import org.kryptonmc.krypton.util.datafix.schema.V2707

private val SAME_NAMESPACED: (Int, Schema) -> Schema = ::NamespacedSchema

val DATA_FIXER = DataFixerBuilder(ServerInfo.WORLD_VERSION).apply {
    val v2571 = addSchema(2571, ::V2571)
    addFixer(AddChoices(v2571, "Added Goat", References.ENTITY))
    val v2679 = addSchema(2679, SAME_NAMESPACED)
    addFixer(RenameCauldronFix(v2679, false))
    val v2680 = addSchema(2680, SAME_NAMESPACED)
    addFixer(RenameItemFix(v2680, "Renamed grass path item to dirt path", rename("minecraft:grass_path", "minecraft:dirt_path")))
    addFixer(RenameBlockWithJigsawFix(v2680, "Renamed grass path block to dirt path", rename("minecraft:grass_path", "minecraft:dirt_path")))
    val v2684 = addSchema(2684, ::V2684)
    addFixer(AddChoices(v2684, "Added Sculk Sensor", References.BLOCK_ENTITY))
    val v2686 = addSchema(2686, ::V2686)
    addFixer(AddChoices(v2686, "Added Axolotl", References.ENTITY))
    val v2688 = addSchema(2688, ::V2688)
    addFixer(AddChoices(v2688, "Added Glow Squid", References.ENTITY))
    addFixer(AddChoices(v2688, "Added Glow Item Frame", References.ENTITY))
    val v2690 = addSchema(2690, SAME_NAMESPACED)
    val oxidizedTerms = mapOf(
        "minecraft:weathered_copper_block" to "minecraft:oxidized_copper_block",
        "minecraft:semi_weathered_copper_block" to "minecraft:weathered_copper_block",
        "minecraft:lightly_weathered_copper_block" to "minecraft:exposed_copper_block",
        "minecraft:weathered_cut_copper" to "minecraft:oxidized_cut_copper",
        "minecraft:semi_weathered_cut_copper" to "minecraft:weathered_cut_copper",
        "minecraft:lightly_weathered_cut_copper" to "minecraft:exposed_cut_copper",
        "minecraft:weathered_cut_copper_stairs" to "minecraft:oxidized_cut_copper_stairs",
        "minecraft:semi_weathered_cut_copper_stairs" to "minecraft:weathered_cut_copper_stairs",
        "minecraft:lightly_weathered_cut_copper_stairs" to "minecraft:exposed_cut_copper_stairs",
        "minecraft:weathered_cut_copper_slab" to "minecraft:oxidized_cut_copper_slab",
        "minecraft:semi_weathered_cut_copper_slab" to "minecraft:weathered_cut_copper_slab",
        "minecraft:lightly_weathered_cut_copper_slab" to "minecraft:exposed_cut_copper_slab",
        "minecraft:waxed_semi_weathered_copper" to "minecraft:waxed_weathered_copper",
        "minecraft:waxed_lightly_weathered_copper" to "minecraft:waxed_exposed_copper",
        "minecraft:waxed_semi_weathered_cut_copper" to "minecraft:waxed_weathered_cut_copper",
        "minecraft:waxed_lightly_weathered_cut_copper" to "minecraft:waxed_exposed_cut_copper",
        "minecraft:waxed_semi_weathered_cut_copper_stairs" to "minecraft:waxed_weathered_cut_copper_stairs",
        "minecraft:waxed_lightly_weathered_cut_copper_stairs" to "minecraft:waxed_exposed_cut_copper_stairs",
        "minecraft:waxed_semi_weathered_cut_copper_slab" to "minecraft:waxed_weathered_cut_copper_slab",
        "minecraft:waxed_lightly_weathered_cut_copper_slab" to "minecraft:waxed_exposed_cut_copper_slab"
    )
    addFixer(RenameItemFix(v2690, "Renamed copper block items to new oxidized terms", rename(oxidizedTerms)))
    addFixer(RenameBlockWithJigsawFix(v2690, "Renamed copper blocks to new oxidized terms", rename(oxidizedTerms)))
    val v2691 = addSchema(2691, SAME_NAMESPACED)
    val copperSuffixes = mapOf(
        "minecraft:waxed_copper" to "minecraft:waxed_copper_block",
        "minecraft:oxidized_copper_block" to "minecraft:oxidized_copper",
        "minecraft:weathered_copper_block" to "minecraft:weathered_copper",
        "minecraft:exposed_copper_block" to "minecraft:exposed_copper"
    )
    addFixer(RenameItemFix(v2691, "Rename copper item suffixes", rename(copperSuffixes)))
    addFixer(RenameBlockWithJigsawFix(v2691, "Rename copper blocks suffixes", rename(copperSuffixes)))
    val v2696 = addSchema(2696, SAME_NAMESPACED)
    val grimstoneToDeepslate = mapOf(
        "minecraft:grimstone" to "minecraft:deepslate",
        "minecraft:grimstone_slab" to "minecraft:cobbled_deepslate_slab",
        "minecraft:grimstone_stairs" to "minecraft:cobbled_deepslate_stairs",
        "minecraft:grimstone_wall" to "minecraft:cobbled_deepslate_wall",
        "minecraft:polished_grimstone" to "minecraft:polished_deepslate",
        "minecraft:polished_grimstone_slab" to "minecraft:polished_deepslate_slab",
        "minecraft:polished_grimstone_stairs" to "minecraft:polished_deepslate_stairs",
        "minecraft:polished_grimstone_wall" to "minecraft:polished_deepslate_wall",
        "minecraft:grimstone_tiles" to "minecraft:deepslate_tiles",
        "minecraft:grimstone_tile_slab" to "minecraft:deepslate_tile_slab",
        "minecraft:grimstone_tile_stairs" to "minecraft:deepslate_tile_stairs",
        "minecraft:grimstone_tile_wall" to "minecraft:deepslate_tile_wall",
        "minecraft:grimstone_bricks" to "minecraft:deepslate_bricks",
        "minecraft:grimstone_brick_slab" to "minecraft:deepslate_brick_slab",
        "minecraft:grimstone_brick_stairs" to "minecraft:deepslate_brick_stairs",
        "minecraft:grimstone_brick_wall" to "minecraft:deepslate_brick_wall",
        "minecraft:chiseled_grimstone" to "minecraft:chiseled_deepslate"
    )
    addFixer(RenameItemFix(v2696, "Renamed grimstone block items to deepslate", rename(grimstoneToDeepslate)))
    addFixer(RenameBlockWithJigsawFix(v2696, "Renamed grimstone blocks to deepslate", rename(grimstoneToDeepslate)))
    val v2700 = addSchema(2700, SAME_NAMESPACED)
    addFixer(RenameBlockWithJigsawFix(v2700, "Renamed cave vines blocks", rename(mapOf("minecraft:cave_vines_head" to "minecraft:cave_vines", "minecraft:cave_vines_body" to "minecraft:cave_vines_plant"))))
    val v2701 = addSchema(2701, SAME_NAMESPACED)
    addFixer(SavedDataPoolElementFeatureFix(v2701))
    val v2702 = addSchema(2702, SAME_NAMESPACED)
    addFixer(PickupArrowFix(v2702))
    val v2704 = addSchema(2704, ::V2704)
    addFixer(AddChoices(v2704, "Added Goat", References.ENTITY))
    val v2707 = addSchema(2707, ::V2707)
    addFixer(AddChoices(v2707, "Added Marker", References.ENTITY))
    val v2710 = addSchema(2710, SAME_NAMESPACED)
    addFixer(RenameStatsFix(v2710, "Renamed play_one_minute stat to play_time", mapOf("minecraft:play_one_minute" to "minecraft:play_time")))
    val v2717 = addSchema(2717, SAME_NAMESPACED)
    addFixer(RenameItemFix(v2717, "Rename azalea_leaves_flowers", rename(mapOf("minecraft:azalea_leaves_flowers" to "minecraft:flowering_azalea_leaves"))))
    addFixer(RenameBlockFix(v2717, "Rename azalea_leaves_flowers items", rename(mapOf("minecraft:azalea_leaves_flowers" to "minecraft:flowering_azalea_leaves"))))
}.build(BOOTSTRAP_EXECUTOR)

private fun rename(from: String, to: String): (String) -> String = { if (it == from) to else it }

private fun rename(map: Map<String, String>): (String) -> String = { map.getOrDefault(it, it) }
