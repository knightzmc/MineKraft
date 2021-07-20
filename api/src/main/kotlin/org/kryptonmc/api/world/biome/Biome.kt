/*
 * This file is part of the Krypton API, licensed under the MIT license.
 *
 * Copyright (C) 2021 KryptonMC and the contributors to the Krypton project.
 *
 * This project is licensed under the terms of the MIT license.
 * For more details, please reference the LICENSE file in the api top-level directory.
 */
package org.kryptonmc.api.world.biome

import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed

/**
 * Represents a biome.
 */
interface Biome : Keyed {

    /**
     * The key of this biome.
     */
    val key: Key

    override fun key() = key
}
