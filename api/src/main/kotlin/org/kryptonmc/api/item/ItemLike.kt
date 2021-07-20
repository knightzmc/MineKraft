/*
 * This file is part of the Krypton API, licensed under the MIT license.
 *
 * Copyright (C) 2021 KryptonMC and the contributors to the Krypton project.
 *
 * This project is licensed under the terms of the MIT license.
 * For more details, please reference the LICENSE file in the api top-level directory.
 */
package org.kryptonmc.api.item

/**
 * Something that could be representable as an item.
 */
interface ItemLike {

    /**
     * Gets the item representation, or null if there is no item representation.
     */
    fun asItem(): ItemType?
}
