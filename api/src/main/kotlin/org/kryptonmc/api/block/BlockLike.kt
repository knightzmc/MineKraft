/*
 * This file is part of the Krypton API, licensed under the MIT license.
 *
 * Copyright (C) 2021 KryptonMC and the contributors to the Krypton project.
 *
 * This project is licensed under the terms of the MIT license.
 * For more details, please reference the LICENSE file in the api top-level directory.
 */
package org.kryptonmc.api.block

/**
 * Something that could be representable as a block.
 */
interface BlockLike {

    /**
     * Gets the block representation, or null if there is no item representation.
     */
    fun asBlock(): Block?
}
