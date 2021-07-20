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
 * A holder for registry entries.
 */
interface Registry<T : Any> : Map<RegistryKey<T>, T> {

    /**
     * The registry key for this registry.
     */
    val key: RegistryKey<out Registry<T>>

    /**
     * The set of child keys.
     */
    val keySet: Set<Key>

    /**
     * Gets a value by its namespaced [key], or null if there is
     * no value associated with the given [key].
     *
     * @param key the key
     * @return the value, or null if not present
     */
    operator fun get(key: Key): T?

    /**
     * Gets a value by its [id], or null if there is no value
     * associated with the given key.
     *
     * @param id the ID
     * @return the value, or null if not present
     */
    operator fun get(id: Int): T?

    /**
     * Gets a namespaced [Key] by its [value], or null if there is
     * no key associated with the given [value].
     *
     * @param value the value
     * @return the key, or null if not present
     */
    operator fun get(value: T): Key?

    /**
     * Registers a new value to this registry with the given registry [key]
     * and value.
     *
     * @param key the registry key
     * @param value the value
     * @return the value
     */
    fun <V : T> register(key: RegistryKey<T>, value: V): V

    /**
     * Registers a new value to this registry with the given registry [key]
     * and value.
     *
     * @param id the ID of the entry in the registry
     * @param key the registry key
     * @param value the value
     * @return the value
     */
    fun <V : T> register(id: Int, key: RegistryKey<T>, value: V): V

    /**
     * Returns true if the given [key] is registered, false otherwise.
     *
     * @param key the key
     * @return true if registered, false otherwise
     */
    operator fun contains(key: Key): Boolean

    /**
     * Gets the [RegistryKey] for the given [value], or null if
     * there is no key associated with the given [value].
     *
     * @param value the value
     * @return the registry key, or null if not present
     */
    fun registryKey(value: T): RegistryKey<T>?

    /**
     * Gets the ID for the given [value], or returns -1 if the
     * given [value] is not registered.
     *
     * @param value the value
     * @return the ID, or -1 if the [value] is not registered
     */
    fun idOf(value: T): Int
}
