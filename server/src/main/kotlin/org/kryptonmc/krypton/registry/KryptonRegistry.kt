/*
 * This file is part of the Krypton project, licensed under the GNU General Public License v3.0
 *
 * Copyright (C) 2021 KryptonMC and the contributors of the Krypton project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.kryptonmc.krypton.registry

import com.google.common.collect.HashBiMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.key.Key
import org.kryptonmc.api.registry.Registry
import org.kryptonmc.api.registry.RegistryKey
import org.kryptonmc.krypton.util.IdMap
import org.kryptonmc.krypton.util.identityStrategy
import kotlin.math.max

open class KryptonRegistry<T : Any>(override val key: RegistryKey<out Registry<T>>) : Registry<T>, IdMap<T> {

    @Suppress("MagicNumber")
    private val byId = ObjectArrayList<T>(256)
    private val toId = Object2IntOpenCustomHashMap<T>(identityStrategy())
    private val storage = HashBiMap.create<Key, T>()
    private val keyStorage = HashBiMap.create<RegistryKey<T>, T>()

    private var nextId = 0

    override fun <V : T> register(key: RegistryKey<T>, value: V) = register(nextId, key, value)

    @Suppress("ReplacePutWithAssignment") // We want to use fastutil's specialised no unboxing put
    override fun <V : T> register(id: Int, key: RegistryKey<T>, value: V): V {
        byId.size(max(byId.size, id + 1))
        byId[id] = value
        toId.put(value, id)
        storage[key.location] = value
        keyStorage[key] = value
        if (nextId <= id) nextId = id + 1
        return value
    }

    override fun get(key: Key) = storage[key]

    override fun get(id: Int) = if (id in byId.indices) byId[id] else null

    override fun get(value: T) = storage.inverse()[value]

    override fun get(key: RegistryKey<T>) = keyStorage[key]

    override fun registryKey(value: T) = keyStorage.inverse()[value]

    override fun idOf(value: T) = toId.getInt(value)

    override fun contains(key: Key) = key in storage

    override fun containsKey(key: RegistryKey<T>) = key in keyStorage

    override fun containsValue(value: T) = storage.containsValue(value)

    override fun isEmpty() = storage.isEmpty()

    override fun iterator() = storage.values.iterator()

    override val keySet: Set<Key>
        get() = storage.keys

    override val entries: Set<Map.Entry<RegistryKey<T>, T>>
        get() = keyStorage.entries

    override val keys: Set<RegistryKey<T>>
        get() = keyStorage.keys

    override val values: Collection<T>
        get() = storage.values

    override val size: Int
        get() = storage.size
}
