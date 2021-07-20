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
package org.kryptonmc.krypton.entity

import net.kyori.adventure.key.Key
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.kryptonmc.api.entity.Entity
import org.kryptonmc.api.entity.EntityType
import org.kryptonmc.api.entity.EntityTypes
import org.kryptonmc.api.registry.Registries
import org.kryptonmc.krypton.entity.monster.KryptonZombie
import org.kryptonmc.krypton.util.logger
import org.kryptonmc.krypton.world.KryptonWorld

object EntityFactory {

    private val LOGGER = logger<EntityFactory>()
    private val TYPE_MAP = mapOf<EntityType<out Entity>, (KryptonWorld) -> KryptonEntity>(
        EntityTypes.AREA_EFFECT_CLOUD to ::KryptonAreaEffectCloud,
        EntityTypes.ZOMBIE to ::KryptonZombie
    )

    @Suppress("UNCHECKED_CAST")
    fun <T : Entity> create(type: EntityType<T>, world: KryptonWorld): T? = TYPE_MAP[type]?.invoke(world) as? T

    fun create(type: EntityType<out Entity>, world: KryptonWorld): KryptonEntity? = TYPE_MAP[type]?.invoke(world)

    fun create(
        world: KryptonWorld,
        nbt: NBTCompound
    ): KryptonEntity? = try {
        create(Registries.ENTITY_TYPE[Key.key(nbt.getString("id"))], world)?.apply { load(nbt) } ?: run {
            LOGGER.warn("No entity found with ID ${nbt.getString("id")}")
            return null
        }
    } catch (exception: RuntimeException) {
        LOGGER.warn("Exception loading entity", exception)
        null
    }
}
