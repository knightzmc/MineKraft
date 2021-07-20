/*
 * This file is part of the Krypton API, licensed under the MIT license.
 *
 * Copyright (C) 2021 KryptonMC and the contributors to the Krypton project.
 *
 * This project is licensed under the terms of the MIT license.
 * For more details, please reference the LICENSE file in the api top-level directory.
 */
package org.kryptonmc.api.event.entity

import org.kryptonmc.api.entity.Entity
import org.kryptonmc.api.event.GenericResult
import org.kryptonmc.api.event.ResultedEvent
import org.kryptonmc.api.world.World

/**
 * Called when the given [entity] is removed from the given [world].
 *
 * @param entity the entity that was removed
 * @param world the world the entity was removed from
 */
class EntityRemoveEvent(
    val entity: Entity,
    val world: World
) : ResultedEvent<GenericResult> {

    override var result = GenericResult.allowed()
}
