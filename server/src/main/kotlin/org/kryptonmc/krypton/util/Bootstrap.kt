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
package org.kryptonmc.krypton.util

import com.mojang.brigadier.exceptions.CommandSyntaxException
import org.kryptonmc.api.block.Blocks
import org.kryptonmc.api.effect.particle.ParticleType
import org.kryptonmc.api.effect.sound.SoundEvents
import org.kryptonmc.api.entity.EntityTypes
import org.kryptonmc.api.item.ItemTypes
import org.kryptonmc.api.item.meta.MetaKeys
import org.kryptonmc.api.registry.Registries
import org.kryptonmc.api.registry.RegistryKeys
import org.kryptonmc.api.registry.RegistryRoots
import org.kryptonmc.api.world.biome.Biomes
import org.kryptonmc.api.world.dimension.DimensionTypes
import org.kryptonmc.api.world.rule.GameRules
import org.kryptonmc.krypton.auth.requests.SessionService
import org.kryptonmc.krypton.command.BrigadierExceptions
import org.kryptonmc.krypton.command.argument.ArgumentTypes
import org.kryptonmc.krypton.entity.EntityFactory
import org.kryptonmc.krypton.entity.attribute.Attributes
import org.kryptonmc.krypton.entity.metadata.MetadataKeys
import org.kryptonmc.krypton.item.KryptonItemManager
import org.kryptonmc.krypton.item.meta.MetaFactory
import org.kryptonmc.krypton.locale.TranslationBootstrap
import org.kryptonmc.krypton.registry.FileRegistries
import org.kryptonmc.krypton.registry.InternalRegistries
import org.kryptonmc.krypton.registry.InternalRegistryKeys
import org.kryptonmc.krypton.tags.BlockTags
import org.kryptonmc.krypton.tags.EntityTypeTags
import org.kryptonmc.krypton.tags.FluidTags
import org.kryptonmc.krypton.tags.GameEventTags
import org.kryptonmc.krypton.tags.ItemTags
import org.kryptonmc.krypton.tags.KryptonTagManager
import org.kryptonmc.krypton.tags.TagTypes
import org.kryptonmc.krypton.util.encryption.Encryption
import org.kryptonmc.krypton.util.reports.CrashReport
import org.kryptonmc.krypton.world.block.BLOCK_LOADER
import org.kryptonmc.krypton.world.block.KryptonBlockManager
import org.kryptonmc.krypton.world.block.palette.GlobalPalette
import org.kryptonmc.krypton.world.event.GameEvents
import org.kryptonmc.krypton.world.fluid.Fluids
import java.util.TreeSet

object Bootstrap {

    private val LOGGER = logger<Bootstrap>()

    @Volatile
    private var bootstrapped = false

    // TODO: Rewrite the preloading
    fun preload() {
        if (bootstrapped) return
        bootstrapped = true
        CrashReport.preload()
        TranslationBootstrap.init()

        // Preload all the registry classes to ensure everything is properly registered
        RegistryRoots
        RegistryKeys
        Registries
        InternalRegistryKeys
        InternalRegistries
        BLOCK_LOADER
        Blocks
        GameEvents
        ParticleType
        SoundEvents
        EntityTypes
        ItemTypes
        Fluids
        Biomes
        DimensionTypes
        GameRules
        Attributes
        MetadataKeys
        TagTypes
        MetaKeys // Not technically a registry, but quite close to one

        // Preload tags (which use registries)
        KryptonTagManager
        BlockTags
        EntityTypeTags
        FluidTags
        GameEventTags
        ItemTags

        // Preload the old registry
        FileRegistries

        // Preload some other things that would otherwise load on first player join or some other time
        Encryption
        SessionService
        ArgumentTypes
        EntityFactory
        KryptonItemManager
        MetaFactory
        KryptonBlockManager
        GlobalPalette
        CommandSyntaxException.BUILT_IN_EXCEPTIONS = BrigadierExceptions
    }

    fun validate() {
        require(bootstrapped) { "Bootstrap wasn't ran!" }
        missingTranslations.forEach { LOGGER.warn("Missing translation: $it") }
    }

    private fun <T> Iterable<T>.checkTranslations(missing: MutableSet<String>, getter: (T) -> String) = forEach {
        val key = getter(it)
        if (!TranslationBootstrap.REGISTRY.contains(key)) missing.add(key)
    }

    private val missingTranslations: Set<String>
        get() {
            val missing = TreeSet<String>()
            InternalRegistries.ATTRIBUTE.values.checkTranslations(missing) { it.description }
            Registries.ENTITY_TYPE.values.checkTranslations(missing) { it.name.key() }
            Registries.BLOCK.values.checkTranslations(missing) { it.translation.key() }
            Registries.ITEM.values.checkTranslations(missing) { it.translation.key() }
            Registries.GAMERULES.values.checkTranslations(missing) { it.translation.key() }
            return missing
        }
}
