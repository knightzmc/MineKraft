/*
 * This file is part of the Krypton API, licensed under the MIT license.
 *
 * Copyright (C) 2021 KryptonMC and the contributors to the Krypton project.
 *
 * This project is licensed under the terms of the MIT license.
 * For more details, please reference the LICENSE file in the api top-level directory.
 */
package org.kryptonmc.api.entity

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component.translatable
import net.kyori.adventure.text.TranslatableComponent

/**
 * A type of entity.
 *
 * @param T the type of entity
 * @param key the key for this entity type
 * @param isSummonable if this entity type can be summoned, for example, with
 *        the /summon command or [org.kryptonmc.api.world.World.spawnEntity]
 * @param name the name of this entity type
 */
data class EntityType<T : Entity>(
    val key: Key,
    val isSummonable: Boolean,
    val name: TranslatableComponent = translatable("entity.${key.namespace()}.${key.value().replace("/", ".")}"),
)
