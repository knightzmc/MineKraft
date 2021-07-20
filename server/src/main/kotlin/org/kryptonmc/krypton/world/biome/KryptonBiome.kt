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
import org.kryptonmc.api.util.StringSerializable
import org.kryptonmc.api.world.biome.Biome

data class KryptonBiome(
    override val key: Key,
    val climate: ClimateSettings,
    val depth: Float,
    val scale: Float,
    val category: BiomeCategory,
    val effects: BiomeEffects
) : Biome

enum class BiomeCategory(override val serialized: String) : StringSerializable {

    NONE("none"),
    TAIGA("taiga"),
    EXTREME_HILLS("extreme_hills"),
    JUNGLE("jungle"),
    MESA("mesa"),
    PLAINS("plains"),
    SAVANNA("savanna"),
    ICY("icy"),
    THE_END("the_end"),
    BEACH("beach"),
    FOREST("forest"),
    OCEAN("ocean"),
    DESERT("desert"),
    RIVER("river"),
    SWAMP("swamp"),
    MUSHROOM("mushroom"),
    NETHER("nether"),
    UNDERGROUND("underground");

    companion object {

        private val BY_NAME = values().associateBy { it.serialized }

        fun fromName(name: String) = BY_NAME.getValue(name)
    }
}
