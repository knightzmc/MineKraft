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
import org.kryptonmc.api.effect.particle.ParticleType
import org.kryptonmc.api.effect.sound.SoundEvent
import org.kryptonmc.api.effect.sound.SoundEvents
import org.kryptonmc.krypton.effect.Music
import org.kryptonmc.krypton.effect.Musics
import org.kryptonmc.krypton.util.clamp
import java.awt.Color

object BuiltInBiomes {

    fun ocean(key: Key, isDeep: Boolean) = ocean(key, isDeep, 4159204, 329011)

    fun coldOcean(key: Key, isDeep: Boolean) = ocean(key, isDeep, 4020182, 329011)

    fun lukewarmOcean(key: Key, isDeep: Boolean) = ocean(key, isDeep, 4566514, 267827)

    fun warmOcean(key: Key, isDeep: Boolean) = ocean(key, isDeep, 4445678, 270131)

    fun frozenOcean(key: Key, isDeep: Boolean) = KryptonBiome(
        key,
        ClimateSettings(if (isDeep) Precipitation.RAIN else Precipitation.SNOW, if (isDeep) 0.5F else 0F, 0.5F, TemperatureModifier.FROZEN),
        if (isDeep) -1.8F else -1F,
        0.1F,
        BiomeCategory.OCEAN,
        BiomeEffects(
            Color(12638463),
            Color(3750089),
            Color(329011),
            (if (isDeep) 0.5F else 0F).calculateSkyColor(),
            ambientMoodSettings = AmbientMoodSettings.CAVE
        )
    )

    fun ocean(key: Key, isDeep: Boolean, waterColor: Int, waterFogColor: Int) = KryptonBiome(
        key,
        ClimateSettings(Precipitation.RAIN, 0.5F, 0.5F),
        if (isDeep) -1.8F else -1F,
        0.1F,
        BiomeCategory.OCEAN,
        BiomeEffects(Color(12638463), Color(waterColor), Color(waterFogColor), 0.5F.calculateSkyColor(), ambientMoodSettings = AmbientMoodSettings.CAVE)
    )

    fun desert(key: Key, depth: Float, scale: Float) = KryptonBiome(
        key,
        ClimateSettings(Precipitation.NONE, 2F, 0F),
        depth,
        scale,
        BiomeCategory.DESERT,
        BiomeEffects(Color(12638463), Color(4159204), Color(329011), 2F.calculateSkyColor(), ambientMoodSettings = AmbientMoodSettings.CAVE)
    )

    fun mountain(key: Key, depth: Float, scale: Float) = KryptonBiome(
        key,
        ClimateSettings(Precipitation.RAIN, 0.2F, 0.3F),
        depth,
        scale,
        BiomeCategory.EXTREME_HILLS,
        BiomeEffects(Color(12638463), Color(4159204), Color(329011), 0.2F.calculateSkyColor(), ambientMoodSettings = AmbientMoodSettings.CAVE)
    )

    fun forest(key: Key, depth: Float, scale: Float) = KryptonBiome(
        key,
        ClimateSettings(Precipitation.RAIN, 0.7F, 0.8F),
        depth,
        scale,
        BiomeCategory.FOREST,
        BiomeEffects(Color(12638463), Color(4159204), Color(329011), 0.7F.calculateSkyColor(), ambientMoodSettings = AmbientMoodSettings.CAVE)
    )

    fun birchForest(key: Key, depth: Float, scale: Float) = KryptonBiome(
        key,
        ClimateSettings(Precipitation.RAIN, 0.6F, 0.6F),
        depth,
        scale,
        BiomeCategory.FOREST,
        BiomeEffects(
            Color(12638463),
            Color(4159204),
            Color(329011),
            0.6F.calculateSkyColor(),
            ambientMoodSettings = AmbientMoodSettings.CAVE
        )
    )

    fun flowerForest(key: Key) = forest(key, 0.1F, 0.4F)

    fun darkForest(key: Key, depth: Float, scale: Float) = KryptonBiome(
        key,
        ClimateSettings(Precipitation.RAIN, 0.7F, 0.8F),
        depth,
        scale,
        BiomeCategory.FOREST,
        BiomeEffects(
            Color(12638463),
            Color(4159204),
            Color(329011),
            0.7F.calculateSkyColor(),
            GrassColorModifier.DARK_FOREST,
            ambientMoodSettings = AmbientMoodSettings.CAVE
        )
    )

    fun taiga(key: Key, depth: Float, scale: Float, isSnowy: Boolean) = KryptonBiome(
        key,
        ClimateSettings(if (isSnowy) Precipitation.SNOW else Precipitation.RAIN, if (isSnowy) -0.5F else -0.25F, if (isSnowy) 0.4F else 0.8F),
        depth,
        scale,
        BiomeCategory.TAIGA,
        BiomeEffects(
            Color(12638463),
            Color(if (isSnowy) 4020182 else 4159204),
            Color(329011),
            (if (isSnowy) -0.5F else -0.25F).calculateSkyColor(),
            ambientMoodSettings = AmbientMoodSettings.CAVE
        )
    )

    fun giantTreeTaiga(key: Key, depth: Float, scale: Float, temperature: Float) = KryptonBiome(
        key,
        ClimateSettings(Precipitation.RAIN, temperature, 0.8F),
        depth,
        scale,
        BiomeCategory.TAIGA,
        BiomeEffects(
            Color(12638463),
            Color(4159204),
            Color(329011),
            temperature.calculateSkyColor(),
            ambientMoodSettings = AmbientMoodSettings.CAVE
        )
    )

    fun swamp(key: Key, depth: Float, scale: Float) = KryptonBiome(
        key,
        ClimateSettings(Precipitation.RAIN, 0.8F, 0.9F),
        depth,
        scale,
        BiomeCategory.SWAMP,
        BiomeEffects(
            Color(12638463),
            Color(6388580),
            Color(2302743),
            0.8F.calculateSkyColor(),
            GrassColorModifier.SWAMP,
            Color(6975545),
            ambientMoodSettings = AmbientMoodSettings.CAVE
        )
    )

    fun tundra(key: Key, depth: Float, scale: Float) = KryptonBiome(
        key,
        ClimateSettings(Precipitation.SNOW, 0F, 0.5F),
        depth,
        scale,
        BiomeCategory.ICY,
        BiomeEffects(
            Color(12638463),
            Color(4159204),
            Color(329011),
            0F.calculateSkyColor(),
            ambientMoodSettings = AmbientMoodSettings.CAVE
        )
    )

    fun river(key: Key, depth: Float, scale: Float, temperature: Float, waterColor: Int, isSnowy: Boolean) = KryptonBiome(
        key,
        ClimateSettings(if (isSnowy) Precipitation.SNOW else Precipitation.RAIN, temperature, 0.5F),
        depth,
        scale,
        BiomeCategory.RIVER,
        BiomeEffects(Color(12638463), Color(waterColor), Color(329011), temperature.calculateSkyColor(), ambientMoodSettings = AmbientMoodSettings.CAVE)
    )

    fun beach(key: Key, depth: Float, scale: Float, temperature: Float, downfall: Float, waterColor: Int, isSnowy: Boolean, isShore: Boolean) = KryptonBiome(
        key,
        ClimateSettings(if (isSnowy) Precipitation.SNOW else Precipitation.RAIN, temperature, downfall),
        depth,
        scale,
        if (isShore) BiomeCategory.NONE else BiomeCategory.BEACH,
        BiomeEffects(
            Color(12638463),
            Color(waterColor),
            Color(329011),
            temperature.calculateSkyColor(),
            ambientMoodSettings = AmbientMoodSettings.CAVE
        )
    )

    fun jungle(key: Key) = jungle(key, 0.1F, 0.2F)

    fun jungleEdge(key: Key) = jungle(key, 0.1F, 0.2F, 0.8F)

    fun modifiedJungle(key: Key) = jungle(key, 0.2F, 0.4F, 0.9F)

    fun modifiedJungleEdge(key: Key) = jungle(key, 0.2F, 0.4F, 0.8F)

    fun jungleHills(key: Key) = jungle(key, 0.45F, 0.3F)

    fun bambooJungle(key: Key) = jungle(key, 0.1F, 0.2F)

    fun bambooJungleHills(key: Key) = jungle(key, 0.45F, 0.3F)

    private fun jungle(key: Key, depth: Float, scale: Float) = jungle(key, depth, scale, 0.9F)

    private fun jungle(key: Key, depth: Float, scale: Float, downfall: Float) = KryptonBiome(
        key,
        ClimateSettings(Precipitation.RAIN, 0.95F, downfall),
        depth,
        scale,
        BiomeCategory.JUNGLE,
        BiomeEffects(
            Color(12638463),
            Color(4159204),
            Color(329011),
            0.95F.calculateSkyColor(),
            ambientMoodSettings = AmbientMoodSettings.CAVE
        )
    )

    fun plains(key: Key) = KryptonBiome(
        key,
        ClimateSettings(Precipitation.RAIN, 0.8F, 0.4F),
        0.125F,
        0.05F,
        BiomeCategory.PLAINS,
        BiomeEffects(
            Color(12638463),
            Color(4159204),
            Color(329011),
            0.8F.calculateSkyColor(),
            ambientMoodSettings = AmbientMoodSettings.CAVE
        )
    )

    fun end(key: Key) = KryptonBiome(
        key,
        ClimateSettings(Precipitation.NONE, 0.5F, 0.5F),
        0.1F,
        0.2F,
        BiomeCategory.THE_END,
        BiomeEffects(
            Color(10518688),
            Color(4159204),
            Color(329011),
            Color(0),
            ambientMoodSettings = AmbientMoodSettings.CAVE
        )
    )

    fun mushroomFields(key: Key, depth: Float, scale: Float) = KryptonBiome(
        key,
        ClimateSettings(Precipitation.RAIN, 0.9F, 1F),
        depth,
        scale,
        BiomeCategory.MUSHROOM,
        BiomeEffects(
            Color(12638463),
            Color(4159204),
            Color(329011),
            0.9F.calculateSkyColor(),
            ambientMoodSettings = AmbientMoodSettings.CAVE
        )
    )

    fun savanna(key: Key, depth: Float, scale: Float, temperature: Float) = KryptonBiome(
        key,
        ClimateSettings(Precipitation.NONE, temperature, 0F),
        depth,
        scale,
        BiomeCategory.SAVANNA,
        BiomeEffects(
            Color(12638463),
            Color(4159204),
            Color(329011),
            temperature.calculateSkyColor(),
            ambientMoodSettings = AmbientMoodSettings.CAVE
        )
    )

    fun savannaPlateau(key: Key) = savanna(key, 1.5F, 0.025F, 1F)

    fun badlands(key: Key, depth: Float, scale: Float) = KryptonBiome(
        key,
        ClimateSettings(Precipitation.NONE, 2F, 0F),
        depth,
        scale,
        BiomeCategory.MESA,
        BiomeEffects(
            Color(12638463),
            Color(4159204),
            Color(329011),
            2F.calculateSkyColor(),
            foliageColor = Color(10387789),
            grassColor = Color(9470285),
            ambientMoodSettings = AmbientMoodSettings.CAVE
        )
    )

    fun erodedBadlands(key: Key) = badlands(key, 0.1F, 0.2F)

    fun void(key: Key) = KryptonBiome(
        key,
        ClimateSettings(Precipitation.NONE, 0.5F, 0.5F),
        0.1F,
        0.2F,
        BiomeCategory.NONE,
        BiomeEffects(
            Color(12638463),
            Color(4159204),
            Color(329011),
            0.5F.calculateSkyColor(),
            ambientMoodSettings = AmbientMoodSettings.CAVE
        )
    )

    fun netherWastes(key: Key) = nether(
        key,
        3344392,
        329011,
        SoundEvents.AMBIENT_NETHER_WASTES_LOOP,
        SoundEvents.AMBIENT_NETHER_WASTES_MOOD,
        SoundEvents.AMBIENT_NETHER_WASTES_ADDITIONS,
        Musics.NETHER_WASTES
    )

    fun soulSandValley(key: Key) = nether(
        key,
        1787717,
        329011,
        SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP,
        SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD,
        SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS,
        Musics.SOUL_SAND_VALLEY,
        AmbientParticleSettings(ParticleType.ASH, 0.00625F)
    )

    fun basaltDeltas(key: Key) = nether(
        key,
        6840176,
        4341314,
        SoundEvents.AMBIENT_BASALT_DELTAS_LOOP,
        SoundEvents.AMBIENT_BASALT_DELTAS_MOOD,
        SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS,
        Musics.BASALT_DELTAS,
        AmbientParticleSettings(ParticleType.WHITE_ASH, 0.118093334F)
    )

    fun crimsonForest(key: Key) = nether(
        key,
        3343107,
        329011,
        SoundEvents.AMBIENT_CRIMSON_FOREST_LOOP,
        SoundEvents.AMBIENT_CRIMSON_FOREST_MOOD,
        SoundEvents.AMBIENT_CRIMSON_FOREST_ADDITIONS,
        Musics.CRIMSON_FOREST,
        AmbientParticleSettings(ParticleType.CRIMSON_SPORE, 0.025F)
    )

    fun warpedForest(key: Key) = nether(
        key,
        1705242,
        329011,
        SoundEvents.AMBIENT_WARPED_FOREST_LOOP,
        SoundEvents.AMBIENT_WARPED_FOREST_MOOD,
        SoundEvents.AMBIENT_WARPED_FOREST_ADDITIONS,
        Musics.WARPED_FOREST,
        AmbientParticleSettings(ParticleType.WARPED_SPORE, 0.01428F)
    )

    private fun nether(
        key: Key,
        fogColor: Int,
        waterFogColor: Int,
        loop: SoundEvent,
        mood: SoundEvent,
        additions: SoundEvent,
        music: Music,
        particles: AmbientParticleSettings? = null
    ) = KryptonBiome(
        key,
        ClimateSettings(Precipitation.NONE, 2F, 0F),
        0.1F,
        0.2F,
        BiomeCategory.NETHER,
        BiomeEffects(
            Color(fogColor),
            Color(4159204),
            Color(waterFogColor),
            2F.calculateSkyColor(),
            ambientParticleSettings = particles,
            ambientLoopSound = loop,
            ambientMoodSettings = AmbientMoodSettings(mood, 6000, 8, 2.0),
            ambientAdditionsSettings = AmbientAdditionsSettings(additions, 0.0111),
            backgroundMusic = music
        )
    )

    fun lushCaves(key: Key) = KryptonBiome(
        key,
        ClimateSettings(Precipitation.RAIN, 0.5F, 0.5F),
        0.1F,
        0.2F,
        BiomeCategory.UNDERGROUND,
        BiomeEffects(
            Color(12638463),
            Color(4159204),
            Color(329011),
            0.5F.calculateSkyColor()
        )
    )

    fun dripstoneCaves(key: Key) = KryptonBiome(
        key,
        ClimateSettings(Precipitation.RAIN, 0.8F, 0.4F),
        0.125F,
        0.05F,
        BiomeCategory.UNDERGROUND,
        BiomeEffects(
            Color(12638463),
            Color(4159204),
            Color(329011),
            0.8F.calculateSkyColor(),
            ambientMoodSettings = AmbientMoodSettings.CAVE
        )
    )
}

private fun Float.calculateSkyColor(): Color {
    val temp = (this / 3F).clamp(-1F, 1F)
    return Color.getHSBColor(0.62222224F - temp * 0.05F, 0.5F + temp * 0.1F, 1F)
}
