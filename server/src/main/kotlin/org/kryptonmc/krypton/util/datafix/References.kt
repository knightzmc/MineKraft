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

import com.mojang.datafixers.DSL

object References {

    val LEVEL = DSL.TypeReference { "level" }
    val PLAYER = DSL.TypeReference { "player" }
    val CHUNK = DSL.TypeReference { "chunk" }
    val HOTBAR = DSL.TypeReference { "hotbar" }
    val OPTIONS = DSL.TypeReference { "options" }
    val STRUCTURE = DSL.TypeReference { "structure" }
    val STATS = DSL.TypeReference { "stats" }
    val SAVED_DATA = DSL.TypeReference { "saved_data" }
    val ADVANCEMENTS = DSL.TypeReference { "advancements" }
    val POI_CHUNK = DSL.TypeReference { "poi_chunk" }
    val ENTITY_CHUNK = DSL.TypeReference { "entity_chunk" }
    val BLOCK_ENTITY = DSL.TypeReference { "block_entity" }
    val ITEM_STACK = DSL.TypeReference { "item_stack" }
    val BLOCK_STATE = DSL.TypeReference { "block_state" }
    val ENTITY_NAME = DSL.TypeReference { "entity_name" }
    val ENTITY_TREE = DSL.TypeReference { "entity_tree" }
    val ENTITY = DSL.TypeReference { "entity" }
    val BLOCK_NAME = DSL.TypeReference { "block_name" }
    val ITEM_NAME = DSL.TypeReference { "item_name" }
    val UNTAGGED_SPAWNER = DSL.TypeReference { "untagged_spawner" }
    val STRUCTURE_FEATURE = DSL.TypeReference { "structure_feature" }
    val OBJECTIVE = DSL.TypeReference { "objective" }
    val TEAM = DSL.TypeReference { "team" }
    val RECIPE = DSL.TypeReference { "recipe" }
    val BIOME = DSL.TypeReference { "biome" }
    val WORLD_GEN_SETTINGS = DSL.TypeReference { "world_gen_settings" }
}
