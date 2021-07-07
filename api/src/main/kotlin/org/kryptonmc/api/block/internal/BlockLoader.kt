/*
 * This file is part of the Krypton API, licensed under the MIT license.
 *
 * Copyright (C) 2021 KryptonMC and the contributors to the Krypton project.
 *
 * This project is licensed under the terms of the MIT license.
 * For more details, please reference the LICENSE file in the api top-level directory.
 */
package org.kryptonmc.api.block.internal

import net.kyori.adventure.key.Key
import net.kyori.adventure.util.Services
import org.jetbrains.annotations.ApiStatus
import org.kryptonmc.api.block.Block

@ApiStatus.Internal
@Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")
interface BlockLoader {

    fun fromKey(key: Key): Block?

    fun fromKey(key: String): Block?
}

internal val BLOCK_LOADER = Services.service(BlockLoader::class.java).orElseThrow {
    IllegalStateException("No candidate for the block loader was found! If you are a server owner, contact the creator of your server software.")
}!!
