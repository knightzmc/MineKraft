/*
 * This file is part of the Krypton API, licensed under the MIT license.
 *
 * Copyright (C) 2021 KryptonMC and the contributors to the Krypton project.
 *
 * This project is licensed under the terms of the MIT license.
 * For more details, please reference the LICENSE file in the api top-level directory.
 */
package org.kryptonmc.api.registry

import net.kyori.adventure.key.Key

/**
 * The manager for registries
 */
interface RegistryManager {

    /**
     * Registers a new entry to the given [registry], with the given [key] mapped to
     * the given [value].
     *
     * @param registry the registry to register to
     * @param key the key
     * @param value the value
     */
    fun <T : Any> register(registry: Registry<T>, key: String, value: T): T = register(registry, Key.key(key), value)

    /**
     * Registers a new entry to the given [registry], with the given [key] mapped to
     * the given [value].
     *
     * @param registry the registry to register to
     * @param key the key
     * @param value the value
     */
    fun <T : Any> register(registry: Registry<T>, key: Key, value: T): T

    /**
     * Registers a new entry to the given [registry], with the given [key] mapped to
     * the given [value].
     *
     * @param registry the registry to register to
     * @param id the ID of the entry in the registry
     * @param key the key
     * @param value the value
     */
    fun <T : Any> register(registry: Registry<T>, id: Int, key: String, value: T): T = register(registry, id, Key.key(key), value)

    /**
     * Registers a new entry to the given [registry], with the given [key] mapped to
     * the given [value].
     *
     * @param registry the registry to register to
     * @param id the ID of the entry in the registry
     * @param key the key
     * @param value the value
     */
    fun <T : Any> register(registry: Registry<T>, id: Int, key: Key, value: T): T

    /**
     * Creates a new registry with the given registry [key].
     *
     * @param key the registry key
     * @return a registry for the given [key]
     */
    fun <T : Any> create(key: RegistryKey<out Registry<T>>): Registry<T>

    /**
     * Creates a new registry with the given registry [key], with a [defaultKey].
     *
     *
     *
     * @param key the registry key
     * @param defaultKey the default key
     * @return a defaulted registry for the given [key]
     */
    fun <T : Any> createDefaulted(key: RegistryKey<out Registry<T>>, defaultKey: Key): DefaultedRegistry<T>
}
