/*
 * This file is part of the Krypton API, licensed under the MIT license.
 *
 * Copyright (C) 2021 KryptonMC and the contributors to the Krypton project.
 *
 * This project is licensed under the terms of the MIT license.
 * For more details, please reference the LICENSE file in the api top-level directory.
 */
package org.kryptonmc.api.world.rule

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

/**
 * A rule dictating how a specific aspect of the game functions.
 *
 * @param name the name of the rule
 * @param default the default value of this rule
 * @param V the type of the value
 */
class GameRule<V : Any>(
    val name: String,
    val default: V
) : ComponentLike {

    /**
     * The translation for this gamerule.
     */
    val translation = Component.translatable("gamerule.$name")

    override fun asComponent() = translation
}
