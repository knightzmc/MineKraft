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

import org.kryptonmc.krypton.effect.Music
import org.kryptonmc.api.effect.sound.SoundEvent
import org.kryptonmc.api.util.StringSerializable
import java.awt.Color

data class BiomeEffects(
    val fogColor: Color,
    val waterColor: Color,
    val waterFogColor: Color,
    val skyColor: Color,
    val grassColorModifier: GrassColorModifier = GrassColorModifier.NONE,
    val foliageColor: Color? = null,
    val grassColor: Color? = null,
    val ambientParticleSettings: AmbientParticleSettings? = null,
    val ambientLoopSound: SoundEvent? = null,
    val ambientMoodSettings: AmbientMoodSettings? = null,
    val ambientAdditionsSettings: AmbientAdditionsSettings? = null,
    val backgroundMusic: Music? = null
)

enum class GrassColorModifier(override val serialized: String) : StringSerializable {

    NONE("none"),
    DARK_FOREST("dark_forest"),
    SWAMP("swamp");

    companion object {

        private val BY_NAME = values().associateBy { it.serialized }

        fun fromName(name: String) = BY_NAME.getValue(name)
    }
}
