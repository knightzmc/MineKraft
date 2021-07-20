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

import net.kyori.adventure.key.Key
import org.kryptonmc.api.registry.DefaultedRegistry
import org.kryptonmc.api.registry.Registry
import org.kryptonmc.api.registry.RegistryKey

class KryptonDefaultedRegistry<T : Any>(
    key: RegistryKey<out Registry<T>>,
    override val defaultKey: Key
) : KryptonRegistry<T>(key), DefaultedRegistry<T> {

    override lateinit var defaultValue: T

    override fun <V : T> register(id: Int, key: RegistryKey<T>, value: V): V {
        if (key.location == defaultKey) defaultValue = value
        return super.register(id, key, value)
    }

    override fun get(key: Key) = super.get(key) ?: defaultValue

    override fun get(id: Int) = super.get(id) ?: defaultValue

    override fun get(key: RegistryKey<T>) = super.get(key) ?: defaultValue

    override fun get(value: T) = super.get(value) ?: defaultKey
}
