/*
 * This file is part of the Krypton API, licensed under the MIT license.
 *
 * Copyright (C) 2021 KryptonMC and the contributors to the Krypton project.
 *
 * This project is licensed under the terms of the MIT license.
 * For more details, please reference the LICENSE file in the api top-level directory.
 */
package org.kryptonmc.api.adventure

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.flattener.ComponentFlattener

/**
 * Registers a type of component to be handled.
 *
 * Analogous with [ComponentFlattener.Builder.mapper], except this uses reified
 * types to improve quality of life as a Kotlin user.
 */
inline fun <reified T : Component> ComponentFlattener.Builder.mapper(noinline converter: (T) -> String) = mapper(T::class.java, converter)

/**
 * Register a type of component that needs to be flattened to an intermediate stage.
 *
 * Analogous with [ComponentFlattener.Builder.complexMapper], except this uses reified
 * types to improve quality of life as a Kotlin user.
 */
inline fun <reified T : Component> ComponentFlattener.Builder.complexMapper(noinline converter: (T, (Component) -> Unit) -> Unit) =
    complexMapper(T::class.java) { t, u -> converter(t) { u.accept(it) } }
